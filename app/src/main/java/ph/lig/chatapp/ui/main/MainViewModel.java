package ph.lig.chatapp.ui.main;

import androidx.databinding.Bindable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AuthenticationListener;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.UserIdentity;
import io.realm.mongodb.sync.SyncConfiguration;
import ph.lig.chatapp.BR;
import ph.lig.chatapp.models.Message;
import ph.lig.chatapp.ui.common.ObservableViewModel;
import ph.lig.chatapp.ui.common.RealmTaskManager;

import static java.util.Objects.requireNonNull;

public class MainViewModel extends ObservableViewModel implements AuthenticationListener {
    private RealmTaskManager t = new RealmTaskManager();
    private boolean mTitleBarShown;
    private App mRealmApp;
    private Realm mRealm;

    @Bindable
    public boolean isTitleBarShown() {
        return mTitleBarShown;
    }

    public void setTitleBarShown(boolean titleBarShown) {
        if(mTitleBarShown != titleBarShown) {
            mTitleBarShown = titleBarShown;
            notifyPropertyChanged(BR.titleBarShown);
        }
    }

    @Bindable
    public boolean isLoggedIn() {
        User currentUser = mRealmApp.currentUser();
        return currentUser != null
                && ! isAnon(currentUser);
    }

    private boolean isAnon(User user) {
        if(user == null) {
            return false;
        }

        List<UserIdentity> identities = user.getIdentities();
        if(identities == null) {
            return false;
        }

        if(identities.size() == 0) {
            return false;
        }

        for (UserIdentity identity : identities) {
            if(Credentials.IdentityProvider.ANONYMOUS.equals(identity.getProvider())) {
                return true;
            }
        }

        return false;
    }

    public App getRealmApp() {
        return mRealmApp;
    }

    public void setRealmApp(App realmApp) {
        mRealmApp = realmApp;
        mRealmApp.addAuthenticationListener(this);
    }

    public void initRealmIfNeededAsync(Realm.Callback callback) {
        if(mRealm != null) {
            callback.onSuccess(mRealm);
            return;
        }

        User currentUser = requireNonNull(mRealmApp.currentUser(), "Cannot get realm while logged out.");
        requireNonNull(callback, "Callback parameter is required.");

        String partitionValue = "myPartition";
        SyncConfiguration config = new SyncConfiguration.Builder(currentUser, partitionValue)
                .waitForInitialRemoteData()
                .build();

        t.add(Realm.getInstanceAsync(config, new Realm.Callback() {
            @Override
            public void onSuccess(@NotNull Realm r) {
                mRealm = r;
                callback.onSuccess(r);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                callback.onError(exception);
            }
        }));
    }

    public void logOutAsync(App.Callback<User> callback) {
        User currentUser = mRealmApp.currentUser();
        if(currentUser == null || isAnon(currentUser)) {
            return;
        }

        t.add(currentUser.logOutAsync(callback));
    }

    @Override
    public void loggedIn(User user) {
        notifyPropertyChanged(BR.loggedIn);
    }

    @Override
    public void loggedOut(User user) {
        notifyPropertyChanged(BR.loggedIn);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        close();
    }

    public void close() {
        if(mRealm != null) {
            mRealm.close();
        }

        t.cancelAllRunning();
    }

    public Realm getRealm() {
        return mRealm;
    }
}
