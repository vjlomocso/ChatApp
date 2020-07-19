package ph.lig.chatapp.models;

import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Message extends RealmObject {
    @PrimaryKey
    ObjectId _id;
    private Profile mSender;
    @Required
    private String mValue;
    @Required
    private Date mDate;

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public Profile getSender() {
        return mSender;
    }

    public void setSender(Profile sender) {
        mSender = sender;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
