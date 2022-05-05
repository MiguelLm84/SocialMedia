package com.miguel_lm.socialmedia.ui.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Post;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.LikesProvider;
import com.miguel_lm.socialmedia.provaiders.PostProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.activities.PostDetailActivity;
import com.miguel_lm.socialmedia.utils.RelativeTime;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


public class MyPostAdapter extends FirestoreRecyclerAdapter<Post, MyPostAdapter.viewHolder> {

    Context context;
    UserProvider userProvider;
    LikesProvider likesProvider;
    AuthProvider authProvider;
    PostProvider postProvider;

    public MyPostAdapter(@NonNull FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        userProvider = new UserProvider();
        likesProvider = new LikesProvider();
        authProvider = new AuthProvider();
        postProvider = new PostProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull final viewHolder holder, int position, @NonNull final Post post) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();
        String relativeTime = RelativeTime.getTimeAgo(post.getTimestamp(), context);

        holder.tv_relativeTimeMyPost.setText(relativeTime);
        holder.tv_title_myPost.setText(post.getTitle());

        if(post.getIdUser().equals(authProvider.getUid())){
            holder.iv_deleteMyPost.setVisibility(View.VISIBLE);

        } else {
            holder.iv_deleteMyPost.setVisibility(View.GONE);
        }

        if(post.getImage1() != null){
            if(!post.getImage1().isEmpty()){
                Picasso.with(context).load(post.getImage1()).into(holder.circleImageMyPost);
            }
        }

        holder.viewHolder.setOnClickListener(v -> {
            Intent i = new Intent(context, PostDetailActivity.class);
            i.putExtra("id", postId);
            context.startActivity(i);
        });

        holder.iv_deleteMyPost.setOnClickListener(v -> showConfirmDelete(postId));
    }

    private void showConfirmDelete(String postId) {

        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Elimimnar publicación")
                .setMessage("¿Estás seguro de realizar esta acción?")
                .setPositiveButton("SI", (dialog, which) -> deletePost(postId))
                .setNegativeButton("NO", null)
                .show();
    }

    private void deletePost(String postId) {

        postProvider.delete(postId).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(context, "El post se ha eliminado correctamente", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, "No se ha podido eliminar el post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_my_post, parent, false);

        return new viewHolder(view);
    }

    public static class viewHolder extends RecyclerView.ViewHolder{

        CircleImageView circleImageMyPost;
        ImageView iv_deleteMyPost;
        TextView tv_title_myPost, tv_relativeTimeMyPost;
        View viewHolder;

        public viewHolder(View view){
            super(view);

            circleImageMyPost = view.findViewById(R.id.circleImageMyPost);
            iv_deleteMyPost = view.findViewById(R.id.iv_deleteMyPost);
            tv_title_myPost = view.findViewById(R.id.tv_title_myPost);
            tv_relativeTimeMyPost = view.findViewById(R.id.tv_relativeTimeMyPost);

            viewHolder = view;
        }
    }
}
