package ph.lig.chatapp.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.User;
import ph.lig.chatapp.R;
import ph.lig.chatapp.databinding.ChatFragmentBinding;
import ph.lig.chatapp.models.Message;
import ph.lig.chatapp.ui.common.RealmTaskManager;
import ph.lig.chatapp.ui.main.MainViewModel;

public class ChatFragment extends Fragment {
    private ChatViewModel mViewModel;
    private RecyclerView mMessages;
    private MessageAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        ChatFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment, container, false);
        binding.setViewModel(mViewModel);
        binding.setHandlers(this);
        View view = binding.getRoot();
        mMessages = view.findViewById(R.id.message_list);
        initData(mainViewModel);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setStackFromEnd(true);
        mMessages.setLayoutManager(layoutManager);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mainViewModel.close();
                requireActivity().supportFinishAfterTransition();
            }
        });
        return view;
    }

    public void onClickSend(@SuppressWarnings("unused") View view) {
        mViewModel.sendMessage();
        mViewModel.setNewMessage(null);
        mMessages.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private void initData(MainViewModel mainViewModel) {
        mainViewModel.initRealmIfNeededAsync(new Realm.Callback() {
            @Override
            public void onSuccess(@NotNull Realm realm) {
                final App realmApp = mainViewModel.getRealmApp();
                mViewModel.setMainViewModel(mainViewModel);
                mViewModel.initMessages();
                User currentUser = Objects.requireNonNull(realmApp.currentUser());
                RealmResults<Message> messages = mViewModel.getMessages();
                mAdapter = new MessageAdapter(messages, currentUser.getId(), true);
                messages.addChangeListener(changes -> mMessages.scrollToPosition(mAdapter.getItemCount() - 1));
                requireActivity().runOnUiThread(() -> mMessages.setAdapter(mAdapter));
            }
        });
    }
}
