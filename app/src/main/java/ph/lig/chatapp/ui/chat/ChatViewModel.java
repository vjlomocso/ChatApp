package ph.lig.chatapp.ui.chat;

import androidx.databinding.Bindable;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Objects;

import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.mongodb.User;
import ph.lig.chatapp.BR;
import ph.lig.chatapp.models.Message;
import ph.lig.chatapp.models.Profile;
import ph.lig.chatapp.ui.common.ObservableViewModel;
import ph.lig.chatapp.ui.main.MainViewModel;

public class ChatViewModel extends ObservableViewModel {
    private MainViewModel mMainViewModel;

    private String mNewMessage;
    private RealmResults<Message> mMessages;

    @Bindable
    public String getNewMessage() {
        return mNewMessage;
    }

    public void setNewMessage(String newMessage) {
        if(!Objects.equals(mNewMessage, newMessage)) {
            mNewMessage = newMessage;
            notifyPropertyChanged(BR.newMessage);
        }
    }

    public void setMainViewModel(MainViewModel mainViewModel) {
        mMainViewModel = mainViewModel;
    }

    public void initMessages() {
        Objects.requireNonNull(mMainViewModel, "Set MainViewModel first before calling this method.");
        mMessages = mMainViewModel.getRealm().where(Message.class)
                .sort("mDate", Sort.ASCENDING)
                .findAllAsync();
    }

    public RealmResults<Message> getMessages() {
        return mMessages;
    }

    public void sendMessage() {
        Objects.requireNonNull(mMainViewModel, "Set MainViewModel first before calling this method.");
        mMainViewModel.getRealm().executeTransaction(realm -> {
            User currentUser = Objects.requireNonNull(mMainViewModel.getRealmApp().currentUser(), "You must be logged in to send messages.");
            String currentUserId = currentUser.getId();
            Profile profile = realm.where(Profile.class).equalTo("_id", currentUserId).findFirst();
            Message message = realm.createObject(Message.class, new ObjectId());
            message.setSender(profile);
            message.setValue(getNewMessage());
            message.setDate(new Date());
        });
    }
}