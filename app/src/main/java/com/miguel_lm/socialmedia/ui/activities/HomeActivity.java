package com.miguel_lm.socialmedia.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.TokenProvider;
import com.miguel_lm.socialmedia.provaiders.UserProvider;
import com.miguel_lm.socialmedia.ui.fragments.ChatsFragment;
import com.miguel_lm.socialmedia.ui.fragments.FilterFragment;
import com.miguel_lm.socialmedia.ui.fragments.HomeFragment;
import com.miguel_lm.socialmedia.ui.fragments.ProfileFragment;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;


public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    TokenProvider tokenProvider;
    AuthProvider authProvider;
    UserProvider userProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(false, HomeActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, HomeActivity.this);
    }

    private void init(){

        bottomNavigation = findViewById(R.id.bottom_navigation);
        eventBottomNavBar();
        tokenProvider = new TokenProvider();
        authProvider = new AuthProvider();
        userProvider = new UserProvider();
        openFragment(new HomeFragment());
        createToken();
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void eventBottomNavBar(){

        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                openFragment(new HomeFragment());

            } else if (item.getItemId() == R.id.navigation_filters) {
                openFragment(new FilterFragment());

            }else if (item.getItemId() == R.id.navigation_chats) {
                openFragment(new ChatsFragment());

            }else if (item.getItemId() == R.id.navigation_profile) {
                openFragment(new ProfileFragment());
            }
            return true;
        });
    }

    private void createToken(){

        tokenProvider.create(authProvider.getUid());
    }
}