package ph.lig.chatapp.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Profile extends RealmObject {
    @PrimaryKey
    private String _id;
    @Required
    private String mName;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }
}
