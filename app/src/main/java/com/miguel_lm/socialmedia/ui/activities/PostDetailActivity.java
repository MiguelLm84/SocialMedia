package com.miguel_lm.socialmedia.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Comment;
import com.miguel_lm.socialmedia.model.FCMBody;
import com.miguel_lm.socialmedia.model.FCMResponse;
import com.miguel_lm.socialmedia.model.SliderItem;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.CommentsProvider;
import com.miguel_lm.socialmedia.provaiders.LikesProvider;
import com.miguel_lm.socialmedia.provaiders.NotificationProvider;
import com.miguel_lm.socialmedia.provaiders.PostProvider;
import com.miguel_lm.socialmedia.provaiders.TokenProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.adapters.CommentAdapter;
import com.miguel_lm.socialmedia.ui.adapters.SliderAdapter;
import com.miguel_lm.socialmedia.utils.RelativeTime;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PostDetailActivity extends AppCompatActivity {

    SliderView sliderView;
    SliderAdapter sliderAdapter;
    List<SliderItem> sliderItems = new ArrayList<>();
    String extraPostId;
    PostProvider postProvider;
    UserProvider userProvider;
    CircleImageView circle_profileInfo;
    TextView tv_username_info, tv_phone_info, tv_titleGame,
            tv_categoryInfo, tv_title_descriptionInfo,
            tv_descriptionInfo, tv_relative_time, tv_number_likes;
    Button btn_showProfile, btn_cancel, btn_ok;
    EditText ed_txt_dialog;
    FloatingActionButton fab_slider;
    ImageView iv_categoryInfo;
    String idUser = "";
    CommentsProvider commentsProvider;
    AuthProvider authProvider;
    RecyclerView recyclerComments;
    CommentAdapter commentAdapter;
    LikesProvider likesProvider;
    Toolbar toolbar;
    NotificationProvider notificationProvider;
    TokenProvider tokenProvider;
    ListenerRegistration listenerRegistration;
    //ListenerRegistration listenerRegistrationComments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        init();
        events();
        getPost();
        getNumberLikes();
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = commentsProvider.getCommentByPost(extraPostId);
        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class).build();

        commentAdapter = new CommentAdapter(options,PostDetailActivity.this);
        recyclerComments.setAdapter(commentAdapter);
        commentAdapter.startListening();
        ViewedMessageHelper.updateOnline(true, PostDetailActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        commentAdapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, PostDetailActivity.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(listenerRegistration != null){
            listenerRegistration.remove();
        }
    }

    private void init() {

        sliderView = findViewById(R.id.imageSlider);
        circle_profileInfo = findViewById(R.id.circle_profileInfo);
        tv_username_info = findViewById(R.id.tv_username_info);
        tv_phone_info = findViewById(R.id.tv_phone_info);
        tv_titleGame = findViewById(R.id.tv_titleGame);
        tv_categoryInfo = findViewById(R.id.tv_categoryInfo);
        tv_title_descriptionInfo = findViewById(R.id.tv_title_descriptionInfo);
        tv_descriptionInfo = findViewById(R.id.tv_descriptionInfo);
        tv_relative_time = findViewById(R.id.tv_relative_time);
        tv_number_likes = findViewById(R.id.tv_number_likes);
        btn_showProfile = findViewById(R.id.btn_showProfile);
        fab_slider = findViewById(R.id.fab_slider);
        iv_categoryInfo = findViewById(R.id.iv_categoryInfo);
        recyclerComments = findViewById(R.id.recyclerComments);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostDetailActivity.this);
        recyclerComments.setLayoutManager(linearLayoutManager);

        postProvider = new PostProvider();
        userProvider = new UserProvider();
        commentsProvider = new CommentsProvider();
        authProvider = new AuthProvider();
        likesProvider = new LikesProvider();
        notificationProvider = new NotificationProvider();
        tokenProvider = new TokenProvider();

        extraPostId = getIntent().getStringExtra("id");
    }

    private void events(){

        //fab_slider.setOnClickListener(v -> showDialogComment());
        fab_slider.setOnClickListener(v -> showDialog());
        btn_showProfile.setOnClickListener(v -> goToShowProfile());
    }

    private void showDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
        final View dialogLayout = LayoutInflater.from(PostDetailActivity.this)
                .inflate(R.layout.dialog_comment, null);
        builder.setView(dialogLayout);
        final AlertDialog dialog = builder.create();
        initDialog(dialogLayout);
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_ok.setOnClickListener(v -> actionButtonOk(dialog));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void actionButtonOk(final AlertDialog dialog) {

        String value = ed_txt_dialog.getText().toString();

        if(!value.isEmpty()){
            createComment(value);
            dialog.dismiss();

        } else {
            Toast.makeText(PostDetailActivity.this,
                    "Debe ingresar el comentario", Toast.LENGTH_SHORT).show();
        }
    }

    private void initDialog(View dialogLayout) {

        btn_cancel = dialogLayout.findViewById(R.id.btn_cancel_dialog);
        btn_ok = dialogLayout.findViewById(R.id.btn_ok_dialog);
        ed_txt_dialog = dialogLayout.findViewById(R.id.ed_txt_dialog);

        ed_txt_dialog.getBackground().setColorFilter(getResources()
                .getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
    }

    private void createComment(final String value) {

        Comment comment = new Comment();
        comment.setComment(value);
        comment.setIdPost(extraPostId);
        comment.setIdUser(authProvider.getUid());
        comment.setTimestamp(new Date().getTime());

        commentsProvider.create(comment).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                sendNotification(value);
                Toast.makeText(PostDetailActivity.this,
                        "El comentario se creó correctamente", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(PostDetailActivity.this,
                        "El comentario no se ha podido crear", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(final String comment) {

        if(idUser == null){
            return;
        }
        tokenProvider.getToken(idUser).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("token")){
                    String token = documentSnapshot.getString("token");
                    Map<String, String> data = new HashMap<>();
                    data.put("title", "Nuevo comentario de " + authProvider.getUserSession().getDisplayName());
                    data.put("body", comment);

                    FCMBody body = new FCMBody(token, "high", "4500s", data);

                    notificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {

                            if(response.body() != null) {

                                if(response.body().getSuccess() == 1) {
                                    Toast.makeText(PostDetailActivity.this, "La notificación se envió correctamente", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(PostDetailActivity.this, "La notificación no se ha podido enviar", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(PostDetailActivity.this, "La notificación no se ha podido enviar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FCMResponse> call, @NonNull Throwable t) {

                        }
                    });
                }

            } else {
                Toast.makeText(PostDetailActivity.this, "El token no existe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToShowProfile() {

        if(!idUser.equals("")){
            Intent i = new Intent(PostDetailActivity.this, UserProfileActivity.class);
            i.putExtra("idUser", idUser);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        } else {
            Toast.makeText(PostDetailActivity.this, "El ID del usuario aún no se ha cargado", Toast.LENGTH_SHORT).show();
        }
    }

    private void instanceSlider(){

        sliderAdapter = new SliderAdapter(PostDetailActivity.this, sliderItems);
        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.THIN_WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();
    }

    @SuppressLint("SetTextI18n")
    private void getNumberLikes() {

        listenerRegistration = likesProvider.getLikesByPost(extraPostId).addSnapshotListener((value, error) -> {
            if(value != null){
                int numLikes = value.size();
                tv_number_likes.setText(numLikes + " Me gusta");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getPost(){

        postProvider.getPostById(extraPostId).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("image1")){
                    String image1 = documentSnapshot.getString("image1");
                    SliderItem item = new SliderItem();
                    item.setImageUrl(image1);
                    sliderItems.add(item);
                }
                if(documentSnapshot.contains("image2")){
                    String image2 = documentSnapshot.getString("image2");
                    SliderItem item = new SliderItem();
                    item.setImageUrl(image2);
                    sliderItems.add(item);
                }
                if(documentSnapshot.contains("title")){
                    String title = documentSnapshot.getString("title");
                    tv_titleGame.setText(title != null ? title.toUpperCase() : null);
                }
                if(documentSnapshot.contains("description")){
                    String description = documentSnapshot.getString("description");
                    tv_descriptionInfo.setText(description);
                }
                if(documentSnapshot.contains("category")){
                    String category = documentSnapshot.getString("category");
                    tv_categoryInfo.setText(category);
                    changeImageCategory(category);
                }

                if(documentSnapshot.contains("idUser")){
                    idUser = documentSnapshot.getString("idUser");
                    getUserInfo(idUser);
                }

                if(documentSnapshot.contains("timestamp")){
                    Long timestamp = documentSnapshot.getLong("timestamp");

                    if(timestamp != null){
                        String relativeTime = RelativeTime.getTimeAgo(timestamp, PostDetailActivity.this);
                        tv_relative_time.setText(relativeTime);

                    } else {
                        tv_relative_time.setText("null");
                    }
                }

                instanceSlider();
            }
        });
    }

    private void changeImageCategory(String category) {

        if(category != null && category.equals("PS4")){
            iv_categoryInfo.setImageResource(R.drawable.icon_ps4);
        }
        if(category != null && category.equals("XBOX")){
            iv_categoryInfo.setImageResource(R.drawable.icon_xbox);
        }
        if(category != null && category.equals("PC")){
            iv_categoryInfo.setImageResource(R.drawable.icon_pc);
        }
        if(category != null && category.equals("NINTENDO")){
            iv_categoryInfo.setImageResource(R.drawable.icon_nintendo);
        }
    }

    private void getUserInfo(String idUser) {

        userProvider.getUser(idUser).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                if(documentSnapshot.contains("username")){
                    String username = documentSnapshot.getString("username");
                    tv_username_info.setText(username);
                }
                if(documentSnapshot.contains("phone")){
                    String phone = documentSnapshot.getString("phone");
                    tv_phone_info.setText(phone);
                }
                if(documentSnapshot.contains("image_profile")){
                    String imageProfile = documentSnapshot.getString("image_profile");
                    Picasso.with(PostDetailActivity.this).load(imageProfile).into(circle_profileInfo);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}