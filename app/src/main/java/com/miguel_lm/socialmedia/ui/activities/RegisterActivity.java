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
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;


public class RegisterActivity extends AppCompatActivity {

    CircleImageView circleImageBack;
    TextInputEditText ed_name, ed_email_reg, ed_password_reg, ed_password_confirm,ed_phone_reg;
    Button btn_registrar;
    AuthProvider mAuthProvider;
    UserProvider userProvider;
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        events();
    }

    private void init(){

        circleImageBack = findViewById(R.id.circleImageBack);
        ed_name = findViewById(R.id.ed_name);
        ed_email_reg = findViewById(R.id.ed_email_reg);
        ed_phone_reg = findViewById(R.id.ed_phone_reg);
        ed_password_reg = findViewById(R.id.ed_password_reg);
        ed_password_confirm = findViewById(R.id.ed_password_confirm);
        btn_registrar = findViewById(R.id.btn_registrar);

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        mAuthProvider = new AuthProvider();
        userProvider = new UserProvider();
    }

    private void events(){

        circleImageBack.setOnClickListener(v -> finish());
        btn_registrar.setOnClickListener(v -> register());
    }

    private void register() {

        String username = Objects.requireNonNull(ed_name.getText()).toString();
        String email_reg = Objects.requireNonNull(ed_email_reg.getText()).toString();
        String password_reg = Objects.requireNonNull(ed_password_reg.getText()).toString();
        String password_confirm = Objects.requireNonNull(ed_password_confirm.getText()).toString();
        String phone_reg = Objects.requireNonNull(ed_phone_reg.getText()).toString();

        if(!username.isEmpty() && !email_reg.isEmpty() && !password_reg.isEmpty() && !password_confirm.isEmpty() && !phone_reg.isEmpty()){
            comprobacionDatosIntroducidos(email_reg, password_reg, password_confirm, username, phone_reg);
            
        } else {
            Toast.makeText(RegisterActivity.this,
                    "Error, para continuar inserta todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void comprobacionDatosIntroducidos(String email_reg, String password_reg,
                                               String password_confirm, String name_reg, String phone_reg) {

        if(isEmailValid(email_reg)){
            passwordValidate(password_reg, password_confirm, email_reg, name_reg, phone_reg);

        } else {
            Toast.makeText(RegisterActivity.this, "Error, el email no es valido", Toast.LENGTH_SHORT).show();
        }
    }


    private void passwordValidate(String pass_reg, String pass_confirm, String email_reg, String name_reg, String phone_reg) {

        if(pass_reg.equals(pass_confirm)){
            comprobacionLongitudEmail(pass_reg, email_reg, name_reg, phone_reg);

        } else {
            Toast.makeText(RegisterActivity.this,
                    "Error, las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }
    }

    private void comprobacionLongitudEmail(String pass_reg, String email_reg, String name_reg, String phone_reg) {

        if(pass_reg.length() >= 6){
            createUser(email_reg, pass_reg, name_reg, phone_reg);

        } else {
            Toast.makeText(RegisterActivity.this,
                    "Error, la contraseña debe contener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
        }
    }

    private void createUser(final String email_reg, final String pass_reg, final String name_reg, final String phone_reg) {

        mDialog.show();
        mAuthProvider.register(email_reg, pass_reg).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                String id = mAuthProvider.getUid();
                registerBD(email_reg, name_reg, id,phone_reg);
                Toast.makeText(RegisterActivity.this,
                        "El usuario se ha registrado correctamente", Toast.LENGTH_SHORT).show();

            } else {
                mDialog.dismiss();
                Toast.makeText(RegisterActivity.this,
                               "No se ha podido registrar al usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isEmailValid(String email) {

        String expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    private void registerBD(final String email_reg, final String name, final String id, final String phone) {

        if(email_reg != null && name != null && phone != null){
            User user = new User();
            user.setId(id);
            user.setEmail(email_reg);
            user.setUsername(name);
            user.setPhone(phone);
            user.setTimestamp(new Date().getTime());

            userProvider.create(user).addOnCompleteListener(task -> {
                mDialog.dismiss();
                if(task.isSuccessful()){
                     Intent i = new Intent(RegisterActivity.this, HomeActivity.class);
                     i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                     startActivity(i);
                     overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                } else {
                    Toast.makeText(RegisterActivity.this,
                            "No se ha podido almacenar al usuario en la BD", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            ed_password_reg.setError("Nombre, email y/o password incorrectos.");
            ed_password_reg.requestFocus();
        }
    }
}