package com.miguel_lm.socialmedia.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Chat;
import com.miguel_lm.socialmedia.model.FCMBody;
import com.miguel_lm.socialmedia.model.FCMResponse;
import com.miguel_lm.socialmedia.model.Message;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.ChatsProvider;
import com.miguel_lm.socialmedia.provaiders.MessageProvider;
import com.miguel_lm.socialmedia.provaiders.NotificationProvider;
import com.miguel_lm.socialmedia.provaiders.TokenProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.adapters.MessageAdapter;
import com.miguel_lm.socialmedia.utils.RelativeTime;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends AppCompatActivity {

    String extraIdUser1, extraIdUser2, extraIdChat, myUsername, usernameChat;
    String idUserInfo = "", imageReceiver = "", imageSender = "";
    //String idSender = "";
    String idUser = "";
    ChatsProvider chatsProvider;
    MessageProvider messageProvider;
    UserProvider userProvider;
    AuthProvider authProvider;
    View actionBarView;
    EditText ed_message;
    ImageView iv_sendMessage, iv_back;
    CircleImageView circleProfile, circle_cenected;
    TextView tv_username, tv_relativeTime;
    RecyclerView recyclerMessages;
    MessageAdapter messageAdapter;
    LinearLayoutManager linearLayoutManager;
    ListenerRegistration listenerRegistration;
    NotificationProvider notificationProvider;
    TokenProvider tokenProvider;
    long mIdNotificationChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        init();
        showCustomToolbar(R.layout.custom_chat_toolbar);
        getMyInfoUser();
        events();
        checkIfChatExist();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(messageAdapter != null){
            messageAdapter.startListening();
            getMessageChat();
        }
        ViewedMessageHelper.updateOnline(true, ChatActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, ChatActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        messageAdapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null){
            listenerRegistration.remove();
        }
    }

    private void init() {

        chatsProvider = new ChatsProvider();
        messageProvider = new MessageProvider();
        authProvider = new AuthProvider();
        userProvider = new UserProvider();
        notificationProvider = new NotificationProvider();
        tokenProvider = new TokenProvider();

        ed_message = findViewById(R.id.ed_message);
        iv_sendMessage = findViewById(R.id.iv_sendMessage);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(linearLayoutManager);

        extraIdUser1 = getIntent().getStringExtra("idUser1");
        extraIdUser2 = getIntent().getStringExtra("idUser2");
        extraIdChat = getIntent().getStringExtra("idChat");
    }

    private void events() {

        iv_sendMessage.setOnClickListener(v -> sendMessage());
    }

    private void getToken(final Message message) {

        if(authProvider.getUid().equals(extraIdUser1)){
            idUser = extraIdUser2;

        } else {
            idUser = extraIdUser1;
        }

        tokenProvider.getToken(idUser).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("token")){
                    String token = documentSnapshot.getString("token");

                    getLastThreeMessages(message, token);

                    ArrayList<Message> messagesArrayList = new ArrayList<>();
                    messagesArrayList.add(message);
                    Gson gson = new Gson();
                    String messages = gson.toJson(messagesArrayList);

                    sendNotification(token, messages, message);
                }

            } else {
                Toast.makeText(ChatActivity.this, "El token no existe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message){

        final Map<String, String> data = new HashMap<>();
        data.put("title", "NUEVO MENSAJE");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mIdNotificationChat));
        data.put("messages", messages);
        data.put("usernameSender", myUsername);
        data.put("usernameReceiver", usernameChat);
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());

        if(imageSender.equals("")){
            imageSender = "IMAGEN NO VÁLIDA";
        }

        if(imageReceiver.equals("")){
            imageReceiver = "IMAGEN NO VÁLIDA";
        }

        data.put("imageSender", imageSender);
        data.put("imageReceiver", imageReceiver);

        String idSender;
        if(authProvider.getUid().equals(extraIdUser1)) {
            idSender = extraIdUser2;

        } else {
            idSender = extraIdUser1;
        }

        messageProvider.getLastMessagerSender(extraIdChat, idSender).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                   int size =  queryDocumentSnapshots.size();
                   String lastMessage;

                   if(size > 0){
                       lastMessage = queryDocumentSnapshots.getDocuments().get(0).getString("message");
                       data.put("lastMessage", lastMessage);
                   }

                    FCMBody body = new FCMBody(token, "high", "4500s", data);

                    notificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {

                            if(response.body() != null) {

                                if(response.body().getSuccess() == 1) {
                                    //Toast.makeText(ChatActivity.this, "La notificación se envió correctamente", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(ChatActivity.this, "La notificación no se ha podido enviar", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(ChatActivity.this, "La notificación no se ha podido enviar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FCMResponse> call, @NonNull Throwable t) {

                        }
                    });
        });
    }

    private void getLastThreeMessages(final Message msg, final String token) {

        messageProvider.getLastThreeMessageByChatAndSender(extraIdChat, authProvider.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Message> messagesArrayList = new ArrayList<>();

                    for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                        if(d.exists()){
                            Message message = d.toObject(Message.class);
                            messagesArrayList.add(message);
                        }
                    }

                    if(messagesArrayList.size() == 0){
                        messagesArrayList.add(msg);
                    }

                    Collections.reverse(messagesArrayList);

                    Gson gson = new Gson();
                    String messages = gson.toJson(messagesArrayList);

                    sendNotification(token, messages, msg);
                });
    }

    private void getMessageChat() {

        Query query = messageProvider.getMessageByChat(extraIdChat);
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class).build();

        messageAdapter = new MessageAdapter(options, ChatActivity.this);
        recyclerMessages.setAdapter(messageAdapter);
        messageAdapter.startListening();
        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {

                super.onItemRangeInserted(positionStart, itemCount);
                updateViewed();
                int numberMessage = messageAdapter.getItemCount();
                int lastMessagePosition = linearLayoutManager.findLastVisibleItemPosition();

                if(lastMessagePosition == -1 || (positionStart >= (numberMessage -1)
                        && lastMessagePosition == (positionStart -1))){
                    recyclerMessages.scrollToPosition(positionStart);
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendMessage() {

        String textMessage = ed_message.getText().toString();

        if(!textMessage.isEmpty()){
            final Message message = new Message();
            message.setIdChat(extraIdChat);
            if(authProvider.getUid().equals(extraIdUser1)){
                message.setIdSender(extraIdUser1);
                message.setIdReceiver(extraIdUser2);

            } else {
                message.setIdSender(extraIdUser2);
                message.setIdReceiver(extraIdUser1);
            }
            message.setTimestamp(new Date().getTime());
            message.setViewed(false);
            message.setIdChat(extraIdChat);
            message.setMessage(textMessage);

            messageProvider.create(message).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    ed_message.setText("");
                    messageAdapter.notifyDataSetChanged();
                    getToken(message);

                } else {
                    Toast.makeText(ChatActivity.this, "El mensaje no se ha podido crear", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkIfChatExist(){

        chatsProvider.getChatUser1AndUser2(extraIdUser1, extraIdUser2).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int size = queryDocumentSnapshots.size();
            if(size == 0){
                createChat();

            } else {
                extraIdChat = queryDocumentSnapshots.getDocuments().get(0).getId();
                mIdNotificationChat = Objects.requireNonNull(queryDocumentSnapshots.getDocuments().get(0).getLong("idNotification"));
                getMessageChat();
                updateViewed();
            }
        });
    }

    private void updateViewed() {

        String idSender;
        if(authProvider.getUid().equals(extraIdUser1)){
            idSender = extraIdUser2;

        } else {
            idSender = extraIdUser1;
        }

        messageProvider.getMessageByChatAndSender(extraIdChat, idSender)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                        messageProvider.updateViewed(document.getId(), true);
                    }
        });
    }

    private void createChat(){

        Chat chat = new Chat();
        chat.setIdUser1(extraIdUser1);
        chat.setIdUser2(extraIdUser2);
        chat.setWriting(false);
        chat.setTimestamp(new Date().getTime());
        chat.setId(extraIdUser1 + extraIdUser2);
        Random random = new Random();
        int n = random.nextInt(1000000);
        chat.setIdNotification(n);
        mIdNotificationChat = n;

        ArrayList<String> ids = new  ArrayList<>();
        ids.add(extraIdUser1);
        ids.add(extraIdUser2);
        chat.setIds(ids);
        chatsProvider.create(chat);
        extraIdChat = chat.getId();
        getMessageChat();
    }

    private void showCustomToolbar(int resource) {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        Objects.requireNonNull(actionBar).setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarView = inflater.inflate(resource, null);
        actionBar.setCustomView(actionBarView);

        circle_cenected = actionBarView.findViewById(R.id.circle_cenected);
        circleProfile = actionBarView.findViewById(R.id.circleProfile);
        tv_username = actionBarView.findViewById(R.id.tv_username);
        tv_relativeTime = actionBarView.findViewById(R.id.tv_relativeTime);
        iv_back = actionBarView.findViewById(R.id.iv_back);

        iv_back.setOnClickListener(v -> finish());

        getUserInfo();
    }

    @SuppressLint("SetTextI18n")
    private void getUserInfo() {

        //String idUserInfo = "";

        if(authProvider.getUid().equals(extraIdUser1)){
            idUserInfo = extraIdUser2;

        } else {
            idUserInfo = extraIdUser1;
        }
        listenerRegistration = userProvider.getUserRealtime(idUserInfo).addSnapshotListener((documentSnapshot, error) -> {
            if(documentSnapshot != null && documentSnapshot.exists()){
                if(documentSnapshot.contains("username")){
                    usernameChat = documentSnapshot.getString("username");
                    tv_username.setText(usernameChat);
                }

                if(documentSnapshot.contains("online")){
                    boolean online = Objects.requireNonNull(documentSnapshot.getBoolean("online"));
                    if(online){
                        tv_relativeTime.setText("En línea");
                        circle_cenected.setVisibility(View.VISIBLE);

                    } else if(documentSnapshot.contains("lastConnect")){
                        long lastConnect = Objects.requireNonNull(documentSnapshot.getLong("lastConnect"));
                        String relativeTime = RelativeTime.getTimeAgo(lastConnect, ChatActivity.this);
                        tv_relativeTime.setText(relativeTime);
                        circle_cenected.setVisibility(View.GONE);
                    }
                }

                if(documentSnapshot.contains("image_profile")){
                    imageReceiver = documentSnapshot.getString("image_profile");
                    if(imageReceiver != null){
                        if(!imageReceiver.isEmpty()){
                            Picasso.with(ChatActivity.this).load(imageReceiver).into(circleProfile);
                        }
                    }
                }
            }
        });
    }

    private void getMyInfoUser(){

        userProvider.getUser(authProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){

                if(documentSnapshot.contains("username")){
                    myUsername = documentSnapshot.getString("username");
                }

                if(documentSnapshot.contains("image_profile")){
                    imageSender = documentSnapshot.getString("image_profile");
                }
            }
        });
    }
}