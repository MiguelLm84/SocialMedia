package com.miguel_lm.socialmedia.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Message;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.utils.RelativeTime;


public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.viewHolder> {

    Context context;
    UserProvider userProvider;
    AuthProvider authProvider;

    public MessageAdapter(@NonNull FirestoreRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
        userProvider = new UserProvider();
        authProvider = new AuthProvider();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onBindViewHolder(@NonNull viewHolder holder, int position, @NonNull final Message message) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String messageId = document.getId();
        holder.tv_message.setText(message.getMessage());

        String relativeTime = RelativeTime.timeFormatAMPM(message.getTimestamp(), context);
        holder.tv_dateMessage.setText(relativeTime);

        if(message.getIdSender().equals(authProvider.getUid())){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(150, 0, 0, 0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(30, 20, 0, 20);
            holder.linearLayoutMessage.setBackground(context.getResources()
                    .getDrawable(R.drawable.rounded_linear_layout));
            holder.iv_viewedMessage.setVisibility(View.VISIBLE);
            holder.tv_message.setTextColor(Color.WHITE);
            holder.tv_dateMessage.setTextColor(Color.LTGRAY);

        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(0, 0, 150, 0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(30, 20, 30, 20);
            holder.linearLayoutMessage.setBackground(context.getResources()
                    .getDrawable(R.drawable.rounded_linear_layout_gray));
            holder.iv_viewedMessage.setVisibility(View.GONE);
            holder.tv_message.setTextColor(Color.DKGRAY);
            holder.tv_dateMessage.setTextColor(Color.LTGRAY);
        }

        if(message.isViewed()){
            holder.iv_viewedMessage.setImageResource(R.drawable.icon_check_blue);

        } else {
            holder.iv_viewedMessage.setImageResource(R.drawable.icon_check_grey);
        }
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_message, parent, false);

        return new viewHolder(view);
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        ImageView iv_viewedMessage;
        TextView tv_message, tv_dateMessage;
        LinearLayout linearLayoutMessage;
        View viewHolder;

        public viewHolder(View view){
            super(view);

            iv_viewedMessage = view.findViewById(R.id.iv_viewedMessage);
            tv_message = view.findViewById(R.id.tv_message);
            tv_dateMessage = view.findViewById(R.id.tv_dateMessage);
            linearLayoutMessage = view.findViewById(R.id.linearLayoutMessage);
            viewHolder = view;
        }
    }
}
