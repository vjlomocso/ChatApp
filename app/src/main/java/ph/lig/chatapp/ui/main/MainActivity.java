package ph.lig.chatapp.ui.main;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import ph.lig.chatapp.BuildConfig;
import ph.lig.chatapp.R;
import ph.lig.chatapp.databinding.MainActivityBinding;
import ph.lig.chatapp.ui.auth.AuthType;

import static ph.lig.chatapp.ui.chat.ChatFragmentDirections.ChatToAuthAction;
import static ph.lig.chatapp.ui.chat.ChatFragmentDirections.chatToAuthAction;

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {

    private MainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        initBinding(mViewModel);
        initRealm(mViewModel);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(this);

        if(mViewModel.isLoggedIn()) {
            navController.navigate(R.id.indexToAuthAction);
            navController.navigate(R.id.authToChatAction);
        }
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        if(destination.getId() == R.id.indexFragment) {
            mViewModel.setTitleBarShown(false);
        } else {
            mViewModel.setTitleBarShown(true);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void onClickLogOut(View view) {
        mViewModel.logOutAsync(result -> {
            ChatToAuthAction action = chatToAuthAction(AuthType.SIGN_UP);
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(action);
        });
    }

    private void initBinding(MainViewModel viewModel) {
        MainActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        binding.setViewModel(viewModel);
    }

    static App appCache = null;
    private void initRealm(MainViewModel viewModel) {
        if(isFinishing()) {
            return;
        }

        if(viewModel.getRealmApp() != null) {
            return;
        }

        if(appCache != null) {
            viewModel.setRealmApp(appCache);
            return;
        }

        Realm.init(this);
        String appName = getResources().getString(R.string.app_name);
        appCache = new App(new AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
                .appName(appName)
                .requestTimeout(30, TimeUnit.SECONDS)
                .build());
        viewModel.setRealmApp(appCache);
    }
}