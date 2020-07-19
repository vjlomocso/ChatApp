package ph.lig.chatapp.ui.auth;

import androidx.core.util.Supplier;
import androidx.databinding.Bindable;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.auth.EmailPasswordAuth;
import ph.lig.chatapp.BR;
import ph.lig.chatapp.models.Profile;
import ph.lig.chatapp.ui.common.ObservableViewModel;
import ph.lig.chatapp.ui.common.RealmTaskManager;
import ph.lig.chatapp.ui.main.MainViewModel;

public class AuthViewModel extends ObservableViewModel {
    private RealmTaskManager t = new RealmTaskManager();
    private String mUserName;
    private String mUserNameError;
    private String mPassword;
    private String mPasswordError;
    private AuthType mAuthType;
    private boolean mLoading;

    private MainViewModel mMainViewModel;

    @Bindable
    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        if(!Objects.equals(mUserName, userName)) {
            mUserName = userName;
            notifyPropertyChanged(BR.userName);
        }
    }

    @Bindable
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        if(!Objects.equals(mPassword, password)) {
            mPassword = password;
            notifyPropertyChanged(BR.password);
        }
    }

    @Bindable
    public String getUserNameError() {
        return mUserNameError;
    }

    public void setUserNameError(String userNameError) {
        if( ! Objects.equals(mUserNameError, userNameError)) {
            mUserNameError = userNameError;
            notifyPropertyChanged(BR.userNameError);
        }
    }

    @Bindable
    public String getPasswordError() {
        return mPasswordError;
    }

    public void setPasswordError(String passwordError) {
        if(!Objects.equals(mPasswordError, passwordError)) {
            mPasswordError = passwordError;
            notifyPropertyChanged(BR.passwordError);
        }
    }

    @Bindable
    public AuthType getAuthType() {
        return mAuthType;
    }

    public void setAuthType(AuthType authType) {
        if(!Objects.equals(mAuthType, authType)) {
            mAuthType = authType;
            notifyPropertyChanged(BR.authType);
        }
    }

    public void toggleAuthType() {
        clearAll();
        if(getAuthType() == AuthType.SIGN_UP) {
            setAuthType(AuthType.LOGIN);
        } else {
            setAuthType(AuthType.SIGN_UP);
        }
    }

    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean loading) {
        if(mLoading != loading) {
            mLoading = loading;
            notifyPropertyChanged(BR.loading);
        }
    }

    public void setMainViewModel(MainViewModel mainViewModel) {
        mMainViewModel = mainViewModel;
    }

    public void clearAll() {
        setUserName(null);
        setUserNameError(null);
        setPassword(null);
        setPasswordError(null);
    }

    public boolean validate(Supplier<String> messageSupplier) {
        return validateUserName(messageSupplier) & validatePassword(messageSupplier); // Used & operator for non-short-circuit condition
    }

    private boolean validateUserName(Supplier<String> messageSupplier) {
        if(isValid(getUserName())) {
            return true;
        }

        setUserNameError(messageSupplier.get());
        return false;
    }

    private boolean validatePassword(Supplier<String> messageSupplier) {
        if(isValid(getPassword())) {
            return true;
        }

        setPasswordError(messageSupplier.get());
        return false;
    }

    public void showErrorOnAllFields(Supplier<String> messageSupplier) {
        setUserNameError(messageSupplier.get());
        setPasswordError(messageSupplier.get());
    }

    private boolean isValid(String value) {
        return value != null
                && value.length() >= 8
                && value.length() <= 16;
    }

    public void executeActionAsync(final App.Callback<Void> callback) {
        setLoading(true);
        if(getAuthType() == AuthType.SIGN_UP) {
            t.add(signUpAsync(signUpResult -> handleSignUpResult(signUpResult, callback)));
        } else {
            t.add(loginAsync(loginResult -> handleLoginResult(loginResult, callback)));
        }
    }

    private void handleSignUpResult(App.Result<Void> signUpResult, App.Callback<Void> callback) {
        if(signUpResult.isSuccess()) {
            t.add(loginAsync(loginResult -> handleLoginResult(loginResult, callback)));
        } else {
            callback.onResult(App.Result.withError(signUpResult.getError()));
            setLoading(false);
        }
    }

    private void handleLoginResult(App.Result<Void> result, App.Callback<Void> callback) {
        callback.onResult(result);
        setLoading(false);
    }

    public RealmAsyncTask signUpAsync(App.Callback<Void> callback) {
        Objects.requireNonNull(callback, "Callback parameter is required.");
        Objects.requireNonNull(mMainViewModel, "Set MainViewModel first before calling this method.");
        final App realmApp = Objects.requireNonNull(mMainViewModel.getRealmApp(), "Set realm app first before calling this method.");
        EmailPasswordAuth auth = realmApp.getEmailPasswordAuth();
        return auth.registerUserAsync(getUserName(), getPassword(), callback);
    }

    public RealmAsyncTask loginAsync(App.Callback<Void> callback) {
        Objects.requireNonNull(callback, "Callback parameter is required.");
        Objects.requireNonNull(mMainViewModel, "Set MainViewModel first before calling this method.");
        final App realmApp = Objects.requireNonNull(mMainViewModel.getRealmApp(), "Set realm app first before calling this method.");
        Credentials credentials = Credentials.emailPassword(getUserName(), getPassword());
        return realmApp.loginAsync(credentials, userResult -> {
            if( ! userResult.isSuccess()) {
                callback.onResult(handleLoginResult(userResult));
                return;
            }

            mMainViewModel.initRealmIfNeededAsync(new Realm.Callback() {
                @Override
                public void onSuccess(@NotNull Realm realm) {
                    App.Result<Void> result = handleLoginResult(userResult);
                    callback.onResult(result);
                }
            });
        });
    }

    private void insertUser(User user) {
        Objects.requireNonNull(mMainViewModel, "Set MainViewModel first before calling this method.");
        Realm realm = Objects.requireNonNull(mMainViewModel.getRealm(), "Set realm first before calling this method.");

        Profile existingProfile = realm.where(Profile.class).equalTo("_id", user.getId()).findFirst();
        if(existingProfile != null) {
            return;
        }

        realm.executeTransaction(r -> {
            Profile profile = r.createObject(Profile.class, user.getId());
            profile.setName(user.getEmail());
        });
    }

    private App.Result<Void> handleLoginResult(App.Result<User> userResult) {
        if(userResult.isSuccess()) {
            insertUser(userResult.get());
            return App.Result.success();
        } else {
            return App.Result.withError(userResult.getError());
        }
    }

    @Override
    protected void onCleared() {
        t.cancelAllRunning();
    }
}
