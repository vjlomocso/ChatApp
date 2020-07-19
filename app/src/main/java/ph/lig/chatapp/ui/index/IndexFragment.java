package ph.lig.chatapp.ui.index;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ph.lig.chatapp.R;
import ph.lig.chatapp.databinding.IndexFragmentBinding;
import ph.lig.chatapp.ui.auth.AuthFragment;
import ph.lig.chatapp.ui.auth.AuthType;
import ph.lig.chatapp.ui.index.IndexFragmentDirections.IndexToAuthAction;

public class IndexFragment extends Fragment {

    private IndexViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        IndexFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.index_fragment, container, false);
        binding.setHandlers(this);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(IndexViewModel.class);
        // TODO: Use the ViewModel
    }

    public void onClickSignUp(View view) {
        navToAuth(AuthType.SIGN_UP);
    }

    public void onClickLogin(View view) {
        navToAuth(AuthType.LOGIN);
    }

    private void navToAuth(AuthType authType) {
        IndexToAuthAction action = IndexFragmentDirections.indexToAuthAction(authType);
        NavHostFragment.findNavController(this).navigate(action);
    }
}