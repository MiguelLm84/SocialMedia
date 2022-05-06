package com.miguel_lm.socialmedia.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Post;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.PostProvider;
import com.miguel_lm.socialmedia.ui.adapters.PostsAdapter;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;

import java.util.Objects;


public class FiltersActivity extends AppCompatActivity {

    String extraCategory;
    RecyclerView recyclerPost;
    PostsAdapter adapter;
    PostProvider postProvider;
    AuthProvider mAuthProvider;
    Toolbar toolbar;
    TextView tv_numberFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = postProvider.getPostByCategoryAndTimestamp(extraCategory);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();

        adapter = new PostsAdapter(options, FiltersActivity.this, tv_numberFilter);
        recyclerPost.setAdapter(adapter);
        adapter.startListening();
        ViewedMessageHelper.updateOnline(true, FiltersActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, FiltersActivity.this);
    }

    private void init() {

        tv_numberFilter = findViewById(R.id.tv_numberFilter);
        recyclerPost = findViewById(R.id.recyclerFilter);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Filtros");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerPost.setLayoutManager(new GridLayoutManager(FiltersActivity.this, 2));

        extraCategory = getIntent().getStringExtra("category");

        postProvider = new PostProvider();
        mAuthProvider = new AuthProvider();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}