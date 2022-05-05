package com.miguel_lm.socialmedia.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Like;
import com.miguel_lm.socialmedia.model.Post;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.LikesProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.activities.PostDetailActivity;
import com.squareup.picasso.Picasso;
import java.util.Date;


public class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.viewHolder> {

    Context context;
    UserProvider userProvider;
    LikesProvider likesProvider;
    AuthProvider authProvider;
    TextView tv_numberFilter;
    ListenerRegistration listenerRegistration;

    public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        userProvider = new UserProvider();
        likesProvider = new LikesProvider();
        authProvider = new AuthProvider();
    }

    public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options, Context context, TextView tv_numFilter) {
        super(options);
        this.context = context;
        userProvider = new UserProvider();
        likesProvider = new LikesProvider();
        authProvider = new AuthProvider();
        tv_numberFilter = tv_numFilter;
    }

    @Override
    protected void onBindViewHolder(@NonNull final viewHolder holder, int position, @NonNull final Post post) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();

        if(tv_numberFilter != null){
            int numberFilter = getSnapshots().size();
            tv_numberFilter.setText(String.valueOf(numberFilter));
        }

        holder.tv_titlePostCard.setText(post.getTitle().toUpperCase());
        holder.tv_descriptionPostCard.setText(post.getDescription());

        if(post.getImage1() != null){
            if(!post.getImage1().isEmpty()){
                Picasso.with(context).load(post.getImage1()).into(holder.iv_postCard);
            }
        }

        holder.viewHolder.setOnClickListener(v -> {
            Intent i = new Intent(context, PostDetailActivity.class);
            i.putExtra("id", postId);
            context.startActivity(i);
        });

        holder.iv_like.setOnClickListener(v -> {
            Like like = new Like();
            like.setIdUser(authProvider.getUid());
            like.setIdPost(postId);
            like.setTimestamp(new Date().getTime());
            like(like, holder);
        });

        getUserInfo(post.getIdUser(), holder);
        getNumberLikesByPost(postId, holder);
        checkIfExistLike(postId, authProvider.getUid(), holder);
    }

    @SuppressLint("SetTextI18n")
    public void getNumberLikesByPost(String idPost, final viewHolder holder){

        listenerRegistration = likesProvider.getLikesByPost(idPost).addSnapshotListener((value, error) -> {
            if(value != null){
                int numberLikes = value.size();
                holder.tv_likes.setText(numberLikes + " Me gusta");
            }
        });
    }

    private void like(final Like like, final viewHolder holder) {

        likesProvider.getLikePostAndUser(like.getIdPost(),
                authProvider.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {

                    int numberDocument = queryDocumentSnapshots.size();

                    if(numberDocument > 0){
                        String idLike = queryDocumentSnapshots.getDocuments().get(0).getId();
                        holder.iv_like.setImageResource(R.drawable.ic_like_gray);
                        likesProvider.delete(idLike);

                    } else {
                        holder.iv_like.setImageResource(R.drawable.ic_like_blue);
                        likesProvider.create(like);
                    }
        });
    }

    private void checkIfExistLike(String idPost, String idUser, final viewHolder holder) {

        likesProvider.getLikePostAndUser(idPost, idUser).get().addOnSuccessListener(queryDocumentSnapshots -> {

            int numberDocument = queryDocumentSnapshots.size();

            if(numberDocument > 0){
                holder.iv_like.setImageResource(R.drawable.ic_like_blue);

            } else {
                holder.iv_like.setImageResource(R.drawable.ic_like_gray);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getUserInfo(String idUser, final viewHolder holder) {

        userProvider.getUser(idUser).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    String username = documentSnapshot.getString("username");
                    holder.tv_usernamePostCard.setText("By " + (username));
                }
            }
        });
    }

    public ListenerRegistration getListener(){
        return listenerRegistration;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_post, parent, false);

        return new viewHolder(view);
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        ImageView iv_postCard, iv_like;
        TextView tv_titlePostCard, tv_descriptionPostCard, tv_usernamePostCard, tv_likes;
        View viewHolder;

        public viewHolder(View view){
            super(view);

            iv_postCard = view.findViewById(R.id.iv_postCard);
            tv_titlePostCard = view.findViewById(R.id.tv_titlePostCard);
            tv_descriptionPostCard = view.findViewById(R.id.tv_descriptionPostCard);
            tv_usernamePostCard = view.findViewById(R.id.tv_usernamePostCard);
            iv_like = view.findViewById(R.id.iv_like);
            tv_likes = view.findViewById(R.id.tv_likes);
            viewHolder = view;
        }
    }
}
