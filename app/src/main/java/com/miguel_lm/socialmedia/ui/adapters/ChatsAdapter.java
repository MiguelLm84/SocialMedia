package com.miguel_lm.socialmedia.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Chat;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.ChatsProvider;
import com.miguel_lm.socialmedia.provaiders.MessageProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.activities.ChatActivity;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsAdapter extends FirestoreRecyclerAdapter<Chat, ChatsAdapter.viewHolder> {

    Context context;
    UserProvider userProvider;
    AuthProvider authProvider;
    ChatsProvider chatsProvider;
    MessageProvider messageProvider;
    ListenerRegistration listenerRegistration, listenerRegistrationInfo;
    ListenerRegistration listenerRegistrationLastMessage;

    public ChatsAdapter(@NonNull FirestoreRecyclerOptions<Chat> options, Context context) {
        super(options);
        this.context = context;
        userProvider = new UserProvider();
        authProvider = new AuthProvider();
        messageProvider = new MessageProvider();
        chatsProvider = new ChatsProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull viewHolder holder, int position, @NonNull Chat chat) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String chatId = document.getId();

        if(authProvider.getUid().equals(chat.getIdUser1())){
            getUserInfo(chat.getIdUser2(), holder);

        } else {
            getUserInfo(chat.getIdUser1(), holder);
        }
        holder.viewHolder.setOnClickListener(v -> goToChatActivity(chatId, chat.getIdUser1(), chat.getIdUser2()));
        getLastMessage(chatId, holder.tv_lastMessageChat);

        String idSender;
        if(authProvider.getUid().equals(chat.getIdUser1())) {
            idSender = chat.getIdUser2();

        } else {
            idSender = chat.getIdUser1();
        }
        getMessageNoRead(chatId,idSender, holder.tv_messageNoRead, holder.frameLayoutNoRead);
    }

    public ListenerRegistration getListener(){
        return listenerRegistration;
    }

    public ListenerRegistration getListenerInfo(){
        return listenerRegistrationInfo;
    }

    public ListenerRegistration getListenerLastMessage(){
        return listenerRegistrationLastMessage;
    }

    private void getMessageNoRead(String chatId, String idSender, TextView tv_messageNoRead, FrameLayout frameLayoutNoRead) {

        listenerRegistration = messageProvider.getMessageByChatAndSender(chatId, idSender).addSnapshotListener((value, error) -> {
            if(value != null){
                int size = value.size();

                if(size > 0){
                    frameLayoutNoRead.setVisibility(View.VISIBLE);
                    tv_messageNoRead.setText(String.valueOf(size));

                } else {
                    frameLayoutNoRead.setVisibility(View.GONE);
                }
           }
       });
    }

    private void getLastMessage(String chatId, final TextView tv_lastMessageChat) {

        listenerRegistrationLastMessage = messageProvider.getLastMessager(chatId).addSnapshotListener((value, error) -> {
            if(value != null){
                int size = value.size();
                if(size > 0){
                    String lastMessage = value.getDocuments().get(0).getString("message");
                    tv_lastMessageChat.setText(lastMessage);
                }
            }
        });
    }

    private void goToChatActivity(String chatId, String idUser1, String idUser2) {

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("idChat", chatId);
        intent.putExtra("idUser1", idUser1);
        intent.putExtra("idUser2", idUser2);
        context.startActivity(intent);
    }

    private void getUserInfo(String idUser, final viewHolder holder){

        listenerRegistrationInfo = userProvider.getUserRealtime(idUser).addSnapshotListener((documentSnapshot, error) -> {

            if(documentSnapshot != null && documentSnapshot.exists()){
                if(documentSnapshot.contains("username")){
                    String username = documentSnapshot.getString("username");
                    holder.tv_usernameChat.setText(username);
                }
                if(documentSnapshot.contains("online")) {
                    boolean online = Objects.requireNonNull(documentSnapshot.getBoolean("online"));
                    if (online) {
                        holder.circle_cenectedOnline.setVisibility(View.VISIBLE);

                    } else {
                        holder.circle_cenectedOnline.setVisibility(View.GONE);
                    }
                }
                if(documentSnapshot.contains("image_profile")){
                    String imageProfile = documentSnapshot.getString("image_profile");
                    if(imageProfile != null){
                        if(!imageProfile.isEmpty()){
                            Picasso.with(context).load(imageProfile).into(holder.circleImageChat);
                        }
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_chat, parent, false);

        return new viewHolder(view);
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        CircleImageView circleImageChat, circle_cenectedOnline;
        TextView tv_usernameChat, tv_lastMessageChat, tv_messageNoRead;
        FrameLayout frameLayoutNoRead;
        View viewHolder;

        public viewHolder(View view){
            super(view);

            circle_cenectedOnline = view.findViewById(R.id.circle_cenectedOnline);
            circleImageChat = view.findViewById(R.id.circleImageChat);
            tv_usernameChat = view.findViewById(R.id.tv_usernameChat);
            tv_lastMessageChat = view.findViewById(R.id.tv_lastMessageChat);
            tv_messageNoRead = view.findViewById(R.id.tv_messageNoRead);
            frameLayoutNoRead = view.findViewById(R.id.frameLayoutNoRead);
            viewHolder = view;
        }
    }
}
