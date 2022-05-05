package com.miguel_lm.socialmedia.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Post;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.PostProvider;
import com.miguel_lm.socialmedia.ui.activities.LoginActivity;
import com.miguel_lm.socialmedia.ui.activities.PostActivity;
import com.miguel_lm.socialmedia.ui.adapters.PostsAdapter;
import com.miguel_lm.socialmedia.utils.ViewedMessageHelper;


public class HomeFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener{

    View root;
    FloatingActionButton fab;
    AuthProvider mAuthProvider;
    RecyclerView recyclerPost;
    PostsAdapter adapter;
    PostsAdapter postsAdapterSearch;
    PostProvider postProvider;
    MaterialSearchBar searchBar;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        init(root);
        eventFab();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getAllPost();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        if(postsAdapterSearch != null){
            postsAdapterSearch.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(adapter.getListener() != null){
            adapter.getListener().remove();
        }
    }

    private void init(View root) {

        fab = root.findViewById(R.id.fab);
        recyclerPost = root.findViewById(R.id.recyclerHome);
        searchBar = root.findViewById(R.id.searchBar);

        searchBar.setOnSearchActionListener(this);
        searchBar.inflateMenu(R.menu.menu);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerPost.setLayoutManager(linearLayoutManager);

        setHasOptionsMenu(true);

        postProvider = new PostProvider();
        mAuthProvider = new AuthProvider();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllPost() {

        Query query = postProvider.getAll();
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();

        adapter = new PostsAdapter(options, getContext());
        recyclerPost.setAdapter(adapter);
        adapter.startListening();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchByTitle(String title){

        Query query = postProvider.getPostByTitle(title);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();

        postsAdapterSearch = new PostsAdapter(options, getContext());
        postsAdapterSearch.notifyDataSetChanged();
        recyclerPost.setAdapter(postsAdapterSearch);
        postsAdapterSearch.startListening();
    }

    private void eventFab(){

        searchBar.getMenu().setOnMenuItemClickListener(this::itemSelected);
        fab.setOnClickListener(v -> goToPost());
    }

    private void goToPost() {

        Intent i = new Intent(requireContext(), PostActivity.class);
        startActivity(i);
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private boolean itemSelected(MenuItem item) {

        if(item.getItemId() == R.id.itemLogout) {
            logout();
        }
        return true;
    }

    private void logout() {

        mAuthProvider.logout(requireContext());
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        ViewedMessageHelper.updateOnline(false, getContext());
        //requireActivity().finish();
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (!enabled) {
            getAllPost();
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {

       searchByTitle(text.toString().toLowerCase());
    }

    @Override
    public void onButtonClicked(int buttonCode) {

    }
}