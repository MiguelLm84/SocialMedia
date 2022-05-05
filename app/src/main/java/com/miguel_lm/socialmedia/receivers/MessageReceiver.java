package com.miguel_lm.socialmedia.receivers;

import static com.miguel_lm.socialmedia.services.MyFirebaseMessagingClient.NOTIFICATION_REPLAY;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.miguel_lm.socialmedia.model.FCMBody;
import com.miguel_lm.socialmedia.model.FCMResponse;
import com.miguel_lm.socialmedia.model.Message;
import com.miguel_lm.socialmedia.provaiders.MessageProvider;
import com.miguel_lm.socialmedia.provaiders.NotificationProvider;
import com.miguel_lm.socialmedia.provaiders.TokenProvider;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MessageReceiver extends BroadcastReceiver{

    String extraIdSender, extraIdReceiver, extraIdChat,
            extraUsernameSender, extraUsernameReceiver,
            extraImageSender, extraImageReceiver;
    int extraIdNotification;
    MessageProvider messageProvider;
    TokenProvider tokenProvider;
    NotificationProvider notificationProvider;

    @Override
    public void onReceive(Context context, Intent intent) {

        extraIdSender = intent.getExtras().getString("idSender");
        extraIdReceiver = intent.getExtras().getString("idReceiver");
        extraIdChat = intent.getExtras().getString("idChat");
        extraUsernameSender = intent.getExtras().getString("usernameSender");
        extraUsernameReceiver = intent.getExtras().getString("usernameReceiver");
        extraImageSender = intent.getExtras().getString("imageSender");
        extraImageReceiver = intent.getExtras().getString("imageReceiver");

        extraIdNotification = intent.getExtras().getInt("idNotification");

        tokenProvider = new TokenProvider();
        notificationProvider = new NotificationProvider();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(extraIdNotification);

        String msg = Objects.requireNonNull(getMessageText(intent)).toString();
        sendMessage(msg);
    }

    private void sendMessage(String messageText) {

        final Message message = new Message();
        message.setIdChat(extraIdChat);
        message.setIdSender(extraIdReceiver);
        message.setIdReceiver(extraIdSender);
        message.setTimestamp(new Date().getTime());
        message.setViewed(false);
        message.setIdChat(extraIdChat);
        message.setMessage(messageText);

        messageProvider = new MessageProvider();

        messageProvider.create(message).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                getToken(message);
            }
        });
    }

    private void getToken(final Message message){

        tokenProvider.getToken(extraIdSender).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("token")){
                    String token = documentSnapshot.getString("token");
                    Gson gson = new Gson();
                    ArrayList<Message> messagesArray = new ArrayList<>();
                    messagesArray.add(message);
                    String messages = gson.toJson(messagesArray);
                    sendNotification(token, messages, message);
                }
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message){

        final Map<String, String> data = new HashMap<>();
        data.put("title", "NUEVO MENSAJE");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(extraIdNotification));
        data.put("messages", messages);
        data.put("usernameSender", extraUsernameSender);
        data.put("usernameReceiver", extraUsernameReceiver);
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());
        data.put("imageSender", extraImageReceiver);
        data.put("imageReceiver", extraImageSender);

        FCMBody body = new FCMBody(token, "high", "4500s", data);

        notificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {

            }

            @Override
            public void onFailure(@NonNull Call<FCMResponse> call, @NonNull Throwable t) {
                Log.d("ERROR","El error fue: " + t.getMessage());
            }
        });
    }

    private CharSequence getMessageText(Intent intent){

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if(remoteInput != null){
            return remoteInput.getCharSequence(NOTIFICATION_REPLAY);
        }

        return null;
    }
}
