package ph.lig.chatapp.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ph.lig.chatapp.R;
import ph.lig.chatapp.models.Message;
import ph.lig.chatapp.models.Profile;

public class MessageAdapter extends RealmRecyclerViewAdapter<Message, MessageAdapter.MessageViewHolder> {

    private String mCurrentUserId;

    public MessageAdapter(@Nullable OrderedRealmCollection<Message> messages, String currentUserId, boolean autoUpdate) {
        super(messages, autoUpdate);
        mCurrentUserId = currentUserId;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private ConstraintLayout mLayout;
        private TextView mMessage;
        private TextView mSender;

        public MessageViewHolder(ConstraintLayout v) {
            super(v);
            mLayout = v;
            mSender = v.findViewById(R.id.sender);
            mMessage = v.findViewById(R.id.message);
        }

        public void setIncoming(boolean incoming) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(mLayout);

            if(incoming) {
                constraintSet.setVisibility(R.id.tail_right, View.GONE);
                constraintSet.setVisibility(R.id.tail_left, View.VISIBLE);
                constraintSet.clear(R.id.message, 2);
                constraintSet.connect(R.id.message, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                constraintSet.clear(R.id.sender, 2);
                constraintSet.connect(R.id.sender, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            } else {
                constraintSet.setVisibility(R.id.tail_right, View.VISIBLE);
                constraintSet.setVisibility(R.id.tail_left, View.GONE);
                constraintSet.clear(R.id.message, 1);
                constraintSet.connect(R.id.message, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                constraintSet.clear(R.id.sender, 1);
                constraintSet.connect(R.id.sender, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            }

            constraintSet.applyTo(mLayout);
        }

        public TextView getMessage() {
            return mMessage;
        }

        public void setMessage(String message) {
            mMessage.setText(message);
        }

        public TextView getSender() {
            return mSender;
        }

        public void setSender(String sender) {
            mSender.setText(sender);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_row, parent, false);
        return new MessageViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = Objects.requireNonNull(getItem(position));
        holder.setMessage(message.getValue());
        Profile profile = message.getSender();
        if(profile != null) {
            holder.setSender(profile.getName());
            holder.setIncoming( ! profile.getId().equals(mCurrentUserId));
        }
    }
}
