package com.miguel_lm.socialmedia.provaiders;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.miguel_lm.socialmedia.model.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageProvider {

    CollectionReference mCollection;

    public MessageProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("message");
    }

    public Task<Void> create(Message message){

        DocumentReference document = mCollection.document();
        message.setId(document.getId());
        return document.set(message);
    }

    public Query getMessageByChat(String idChat){
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.ASCENDING);
    }

    public Query getMessageByChatAndSender(String idChat, String idSender){
        return mCollection.whereEqualTo("idChat", idChat)
                .whereEqualTo("idSender", idSender)
                .whereEqualTo("viewed", false);
    }

    public Query getLastThreeMessageByChatAndSender(String idChat, String idSender){
        return mCollection.whereEqualTo("idChat", idChat)
                .whereEqualTo("idSender", idSender)
                .whereEqualTo("viewed", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3);
    }

    public Query getLastMessager(String idChat){
        return mCollection.whereEqualTo("idChat", idChat)
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }

    public Query getLastMessagerSender(String idChat, String idSender){
        return mCollection.whereEqualTo("idChat", idChat)
                .whereEqualTo("idSender", idSender)
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }

    public Task<Void> updateViewed(String idDocument, boolean state){

        Map<String, Object> map = new HashMap<>();
        map.put("viewed",state);
        return mCollection.document(idDocument).update(map);
    }
}
