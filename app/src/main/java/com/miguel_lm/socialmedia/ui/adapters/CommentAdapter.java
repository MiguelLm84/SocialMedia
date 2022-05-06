package com.miguel_lm.socialmedia.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Comment;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.viewHolder> {

    Context context;
    UserProvider userProvider;

    public CommentAdapter(@NonNull FirestoreRecyclerOptions<Comment> options, Context context) {
        super(options);
        this.context = context;
        userProvider = new UserProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull viewHolder holder, int position, @NonNull Comment comment) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        //final String commentId = document.getId();
        String idUser = document.getString("idUser");

        holder.tv_descriptionComment.setText(comment.getComment());
        getUserInfo(idUser, holder);
    }

    private void getUserInfo(String idUser, final viewHolder holder){

        userProvider.getUser(idUser).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("username")){
                    String username = documentSnapshot.getString("username");
                    holder.tv_usernameComment.setText(username);
                }
                if(documentSnapshot.contains("image_profile")){
                    String imageProfile = documentSnapshot.getString("image_profile");
                    if(imageProfile != null){
                        if(!imageProfile.isEmpty()){
                            Picasso.with(context).load(imageProfile).into(holder.circleImageComment);
                        }
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comment, parent, false);

        return new viewHolder(view);
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        CircleImageView circleImageComment;
        TextView tv_usernameComment, tv_descriptionComment;
        View viewHolder;

        public viewHolder(View view){
            super(view);

            circleImageComment = view.findViewById(R.id.circleImageComment);
            tv_usernameComment = view.findViewById(R.id.tv_usernameComment);
            tv_descriptionComment = view.findViewById(R.id.tv_descriptionComment);
            viewHolder = view;
        }
    }
}
