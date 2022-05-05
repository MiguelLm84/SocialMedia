package com.miguel_lm.socialmedia.provaiders;

import android.content.Context;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class AuthProvider {

    private final FirebaseAuth mAuth;

    public AuthProvider(){
        mAuth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> register(String email, String password){

        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> login(String email, String password){

        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> googleLogin(String idToken){

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return mAuth.signInWithCredential(credential);
    }

    public String getUid(){

        if(mAuth.getCurrentUser() != null){
            return mAuth.getCurrentUser().getUid();

        } else {
            return null;
        }
    }

    public String getCorreo(){

        if(mAuth.getCurrentUser() != null){
            return mAuth.getCurrentUser().getEmail();

        } else {
            return null;
        }
    }

    public void logout(Context context){

        if(mAuth != null){
            mAuth.signOut();
        } else {
            Toast.makeText(context, "No se ha podido cerrar la sesión", Toast.LENGTH_SHORT).show();
        }
    }

    public FirebaseUser getUserSession(){

        if(mAuth.getCurrentUser() != null){
            return mAuth.getCurrentUser();

        } else {
            return null;
        }
    }

}
