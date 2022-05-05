package com.miguel_lm.socialmedia.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.User;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import java.util.Objects;
import dmax.dialog.SpotsDialog;


public class LoginActivity extends AppCompatActivity {

    TextView tv_register;
    TextInputEditText ed_email, ed_password;
    Button btnInicioSesion;
    GoogleSignInButton btn_loginGoogle;
    AuthProvider authProvider;
    UserProvider userProvider;
    private GoogleSignInClient mGoogleSignInClient;
    AlertDialog mDialog;
    String email = "";
    String password = "";


    private static final String TAG = "ERROR";
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        init();
        events();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(authProvider.getUserSession() != null){
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void init(){

        tv_register = findViewById(R.id.tv_register);
        ed_email = findViewById(R.id.ed_email);
        ed_password = findViewById(R.id.ed_password);
        btnInicioSesion = findViewById(R.id.btn_inicioSesion);
        btn_loginGoogle = findViewById(R.id.btn_loginGoogle);

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        authProvider = new AuthProvider();
        userProvider = new UserProvider();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id_2))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    private void events(){

        btnInicioSesion.setOnClickListener(v -> login());
        tv_register.setOnClickListener(v -> goToRegister());
        btn_loginGoogle.setOnClickListener(v -> signInGoogle());
    }

    private void goToRegister(){

        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        mDialog.show();
        authProvider.googleLogin(idToken).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                checkUserExiste(authProvider.getUid());

            } else {
                mDialog.dismiss();
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(LoginActivity.this,
                        "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserExiste(String id) {

        userProvider.getUser(id).addOnSuccessListener(LoginActivity.this, documentSnapshot -> {
            if(documentSnapshot.exists()){
                mDialog.dismiss();
                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            } else {

                registerUserWithGoogle(id);
            }
        });
    }

    private void registerUserWithGoogle(String id){

        String email = authProvider.getCorreo();
        User user = new User();
        user.setEmail(email);
        user.setId(id);

        userProvider.create(user).addOnCompleteListener(task -> {
            mDialog.dismiss();
            if(task.isSuccessful()){
                finish();
                Intent i = new Intent(LoginActivity.this, CompleteProfileActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            } else {
                Toast.makeText(LoginActivity.this,
                        "No se pudo almacenar la información del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {

        email = Objects.requireNonNull(ed_email.getText()).toString();
        password = Objects.requireNonNull(ed_password.getText()).toString();

        if(!email.isEmpty() || !password.isEmpty()){
            mDialog.show();
            authProvider.login(email, password).addOnCompleteListener(task -> {
                mDialog.dismiss();
                if(task.isSuccessful()){
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                } else {
                    Toast.makeText(LoginActivity.this,
                            "El email o la contraseña no son correctas", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(LoginActivity.this,
                    "El email o la contraseña no son correctas", Toast.LENGTH_SHORT).show();
        }

        Log.d("CAMPO", "Email: " + email);
        Log.d("CAMPO", "Password: " + password);
    }
}