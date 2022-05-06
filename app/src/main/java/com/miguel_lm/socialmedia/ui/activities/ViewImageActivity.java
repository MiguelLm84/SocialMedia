package com.miguel_lm.socialmedia.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    ImageView iv_image;
    String extraIdUser1;
    UserProvider userProvider;
    String image_profile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        init();
    }

    private void init() {

        iv_image = findViewById(R.id.image_profile);
        extraIdUser1 = getIntent().getStringExtra("idUser1");

        userProvider = new UserProvider();

        viewImage(extraIdUser1);
    }

    private void viewImage(String extraIdUser){

        userProvider.getUser(extraIdUser).addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                if(documentSnapshot.contains("image_profile")){
                    image_profile = documentSnapshot.getString("image_profile");
                    if(image_profile != null){
                        if(!image_profile.isEmpty()){
                            Picasso.with(ViewImageActivity.this).load(image_profile).into(iv_image);
                        }
                    }
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