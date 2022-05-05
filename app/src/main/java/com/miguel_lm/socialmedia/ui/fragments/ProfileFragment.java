package com.miguel_lm.socialmedia.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Post;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.PostProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.activities.EditProfileActivity;
import com.miguel_lm.socialmedia.ui.adapters.MyPostAdapter;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    LinearLayout mLinearLayout;
    TextView tv_name_profile, tv_email_profile, tv_phone_profile, tv_number_posts, tv_postExist;
    CircleImageView profile_picture;
    ImageView iv_cover_image;
    UserProvider userProvider;
    AuthProvider authProvider;
    PostProvider postProvider;
    RecyclerView recyclerMyPost;
    MyPostAdapter adapter;
    ListenerRegistration listenerRegistration;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        init(root);
        event();
        getUser();
        getPostNumber();
        checkIfExistPost();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = postProvider.getPostByUser(authProvider.getUid());
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();

        adapter = new MyPostAdapter(options, getContext());
        recyclerMyPost.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null){
            listenerRegistration.remove();
        }
    }

    private void init(View root){

        mLinearLayout = root.findViewById(R.id.linearLayoutEditProfile);
        tv_name_profile = root.findViewById(R.id.tv_name_profile);
        tv_email_profile = root.findViewById(R.id.tv_email_profile);
        tv_phone_profile = root.findViewById(R.id.tv_phone_profile);
        tv_number_posts = root.findViewById(R.id.tv_number_posts);
        tv_postExist = root.findViewById(R.id.tv_postExist);
        profile_picture = root.findViewById(R.id.profile_picture);
        iv_cover_image = root.findViewById(R.id.iv_cover_image);
        recyclerMyPost = root.findViewById(R.id.recyclerMyPost);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerMyPost.setLayoutManager(linearLayoutManager);

        userProvider = new UserProvider();
        authProvider = new AuthProvider();
        postProvider = new PostProvider();
    }

    private void event(){

        mLinearLayout.setOnClickListener(v -> goToEditProfile());
    }

    @SuppressLint("SetTextI18n")
    private void checkIfExistPost() {

        listenerRegistration = postProvider.getPostByUser(authProvider.getUid()).addSnapshotListener((value, error) -> {
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

        postProvider.getPostByUser(authProvider.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int numberPost = queryDocumentSnapshots.size();
            tv_number_posts.setText(String.valueOf(numberPost));
        });
    }

    private void goToEditProfile() {

        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void getUser(){

        userProvider.getUser(authProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
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
                            Picasso.with(getContext()).load(imageProfile).into(profile_picture);
                        }
                    }
                }
                if(documentSnapshot.contains("image_cover")){
                    String cover = documentSnapshot.getString("image_cover");
                    if(cover != null){
                        if(!cover.isEmpty()){
                            Picasso.with(getContext()).load(cover).into(iv_cover_image);
                        }
                    }
                }
            }
        });
    }
}