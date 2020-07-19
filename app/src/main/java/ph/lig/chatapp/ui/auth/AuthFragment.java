package ph.lig.chatapp.ui.auth;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import io.realm.mongodb.App;
import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;
import ph.lig.chatapp.R;
import ph.lig.chatapp.databinding.AuthFragmentBinding;
import ph.lig.chatapp.ui.common.RealmTaskManager;
import ph.lig.chatapp.ui.main.MainViewModel;

public class AuthFragment extends Fragment {
    private RealmTaskManager t = new RealmTaskManager();
    private AuthViewModel mViewModel;
    private String mCommonError;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        mViewModel.setMainViewModel(mainViewModel);
        AuthFragmentBinding binding = initBinding(inflater, container);
        View view = binding.getRoot();
        initLinkUnderline(view);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mainViewModel.close();
                requireActivity().supportFinishAfterTransition();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle arguments = Objects.requireNonNull(getArguments(), "Navigation arguments required");
        AuthType authType = AuthFragmentArgs.fromBundle(arguments).getAuthType();
        mViewModel.setAuthType(authType);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        t.cancelAllRunning();
    }

    private AuthFragmentBinding initBinding(LayoutInflater inflater, ViewGroup container) {
        AuthFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.auth_fragment, container, false);
        binding.setViewModel(mViewModel);
        binding.setHandlers(this);
        return binding;
    }

    private void initLinkUnderline(View view) {
        TextView switchLink = view.findViewById(R.id.switch_link);
        switchLink.setPaintFlags(switchLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public void onClickAction(@SuppressWarnings("unused") View view) {
        if( ! mViewModel.validate(this::getFieldError) || mViewModel.isLoading()) {
            return;
        }

        mViewModel.executeActionAsync(this::handleActionResult);
    }

    public void onClickSwitch(@SuppressWarnings("unused") View view) {
        if(mViewModel.isLoading()) {
            return;
        }

        mViewModel.toggleAuthType();
    }

    private void handleActionResult(App.Result<Void> result) {
        if(result.isSuccess()) {
            handleSuccess();
        } else {
            handleError(result.getError());
        }
    }

    private void handleSuccess() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.authToChatAction);
    }

    private void handleError(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        switch(errorCode.getType()) {
            case ErrorCode.Type.AUTH:
            case ErrorCode.Type.SERVICE:
                mViewModel.showErrorOnAllFields(this::getFieldError);
                break;
            case ErrorCode.Type.CONNECTION:
            case ErrorCode.Type.JAVA:
                Snackbar.make(requireView(), R.string.connection_error, Snackbar.LENGTH_LONG)
                    .show();
                break;
            default:
                Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG)
                    .show();
                break;
        }
    }

    private String getFieldError() {
        if(mCommonError == null) {
            mCommonError = getResources().getString(R.string.field_error);
        }

        return mCommonError;
    }
}
