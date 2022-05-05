package com.miguel_lm.socialmedia.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.User;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;
import dmax.dialog.SpotsDialog;


public class CompleteProfileActivity extends AppCompatActivity {

    TextInputEditText ed_name, ed_phone;
    Button btn_confirmar;
    AuthProvider mAuthProvider;
    UserProvider userProvider;
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        init();
        events();
    }

    private void init(){

        ed_name = findViewById(R.id.ed_name);
        ed_phone = findViewById(R.id.ed_phone);
        btn_confirmar = findViewById(R.id.btn_confirmar);

        mAuthProvider = new AuthProvider();
        userProvider = new UserProvider();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();
    }

    private void events(){

        btn_confirmar.setOnClickListener(v -> register());
    }

    private void register() {

        String username = Objects.requireNonNull(ed_name.getText()).toString();
        String phone = Objects.requireNonNull(ed_phone.getText()).toString();

        if(!username.isEmpty() /*&& !phone.isEmpty()*/){
            updateUser(username, phone);

        } else {
            Toast.makeText(CompleteProfileActivity.this,
                    "Error, para continuar inserta todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUser(final String username, final String phone) {

        String id = mAuthProvider.getUid();
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPhone(phone);
        user.setTimestamp(new Date().getTime());

        mDialog.show();
        userProvider.update(user).addOnCompleteListener(task -> {
            mDialog.dismiss();
            if(task.isSuccessful()){
                finish();
                Intent i = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            } else {
                Toast.makeText(CompleteProfileActivity.this,
                        "No se ha podido almacenar al usuario en la BD", Toast.LENGTH_SHORT).show();
            }
        });
    }
}