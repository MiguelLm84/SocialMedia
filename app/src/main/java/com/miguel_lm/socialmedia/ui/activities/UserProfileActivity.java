package com.miguel_lm.socialmedia.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Post;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.PostProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.adapters.MyPostAdapter;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfileActivity extends AppCompatActivity {

    LinearLayout mLinearLayout;
    TextView tv_name_profile, tv_email_profile, tv_phone_profile, tv_number_posts, tv_postExist;
    CircleImageView profile_picture;
    ImageView iv_cover_image;
    UserProvider userProvider;
    AuthProvider authProvider;
    PostProvider postProvider;
    String extraIdUser;
    RecyclerView recyclerMyPost;
    MyPostAdapter adapter;
    Toolbar toolbar;
    FloatingActionButton fabChat;
    ListenerRegistration listenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        init();
        event();
        getUser();
        getPostNumber();
        checkIfExistPost();
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = postProvider.getPostByUser(extraIdUser);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();

        adapter = new MyPostAdapter(options, UserProfileActivity.this);
        recyclerMyPost.setAdapter(adapter);
        adapter.startListening();
        ViewedMessageHelper.updateOnline(true, UserProfileActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, UserProfileActivity.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null){
            listenerRegistration.remove();
        }
    }

    private void init(){

        mLinearLayout = findViewById(R.id.linearLayoutEditProfile);
        tv_name_profile = findViewById(R.id.tv_name_profile);
        tv_email_profile = findViewById(R.id.tv_email_profile);
        tv_phone_profile = findViewById(R.id.tv_phone_profile);
        tv_number_posts = findViewById(R.id.tv_number_posts);
        tv_postExist = findViewById(R.id.tv_postExist);
        profile_picture = findViewById(R.id.profile_picture);
        iv_cover_image = findViewById(R.id.iv_cover_image);
        recyclerMyPost = findViewById(R.id.recyclerMyPost);
        toolbar = findViewById(R.id.toolbar);
        fabChat = findViewById(R.id.fabChat);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserProfileActivity.this);
        recyclerMyPost.setLayoutManager(linearLayoutManager);

        userProvider = new UserProvider();
        authProvider = new AuthProvider();
        postProvider = new PostProvider();

        extraIdUser = getIntent().getStringExtra("idUser");

        if(authProvider.getUid().equals(extraIdUser)){
            fabChat.setVisibility(View.GONE);
        }
    }

    private void event(){

        fabChat.setOnClickListener(v -> goToChatActivity());
    }

    private void goToChatActivity() {

        Intent i = new Intent(UserProfileActivity.this, ChatActivity.class);
        i.putExtra("idUser1", authProvider.getUid());
        i.putExtra("idUser2", extraIdUser);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @SuppressLint("SetTextI18n")
    private void checkIfExistPost() {

        listenerRegistration = postProvider.getPostByUser(extraIdUser).addSnapshotListener((value, error) -> {
            if(value != null){
                int numberPost = value.size();
                if(numberPost > 0){
                    tv_postExist.setText("Publicaciones");
                    tv_postExist.setTextColor(Color.RED);

                } else {
                    tv_postExist.setText("No hay publicaciones");
                    tv_postExist.setTextColor(Color.GRAY);
                }
            }
        });
    }

    private void getPostNumber(){

        postProvider.getPostByUser(extraIdUser).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int numberPost = queryDocumentSnapshots.size();
            tv_number_posts.setText(String.valueOf(numberPost));
        });
    }

    private void getUser(){

        userProvider.getUser(extraIdUser).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("email")){
                    String email = documentSnapshot.getString("email");
                    tv_email_profile.setText(email);
                }
                if(documentSnapshot.contains("phone")){
                    String phone = documentSnapshot.getString("phone");
                    tv_phone_profile.setText(phone);
                }
                if(documentSnapshot.contains("username")){
                    String username = documentSnapshot.getString("username");
                    tv_name_profile.setText(username);
                }
                if(documentSnapshot.contains("image_profile")){
                    String imageProfile = documentSnapshot.getString("image_profile");
                    if(imageProfile != null){
                        if(!imageProfile.isEmpty()){
                            Picasso.with(UserProfileActivity.this).load(imageProfile).into(profile_picture);
                        }
                    }
                }
                if(documentSnapshot.contains("image_cover")){
                    String cover = documentSnapshot.getString("image_cover");
                    if(cover != null){
                        if(!cover.isEmpty()){
                            Picasso.with(UserProfileActivity.this).load(cover).into(iv_cover_image);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return true;
    }
}