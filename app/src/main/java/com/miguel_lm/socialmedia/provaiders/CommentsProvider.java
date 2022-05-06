package com.miguel_lm.socialmedia.provaiders;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.miguel_lm.socialmedia.model.Comment;

public class CommentsProvider {

    CollectionReference mCollection;

    public CommentsProvider(){

        mCollection = FirebaseFirestore.getInstance().collection("comments");
    }

    public Task<Void> create(Comment comment){
       return mCollection.document().set(comment);
    }

    public Query getCommentByPost(String idPost){
        return mCollection.whereEqualTo("idPost", idPost).orderBy("timestamp", Query.Direction.DESCENDING);
    }
}
