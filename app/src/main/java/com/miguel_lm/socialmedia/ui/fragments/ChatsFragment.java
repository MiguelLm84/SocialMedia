package com.miguel_lm.socialmedia.ui.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.Chat;
import com.miguel_lm.socialmedia.provaiders.AuthProvider;
import com.miguel_lm.socialmedia.provaiders.ChatsProvider;
import com.miguel_lm.socialmedia.ui.adapters.ChatsAdapter;

import java.util.Objects;


public class ChatsFragment extends Fragment {

    ChatsAdapter chatsAdapter;
    RecyclerView recyclerChats;
    ChatsProvider chatsProvider;
    AuthProvider authProvider;
    View root;
    Toolbar toolbar;

    public ChatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_chats, container, false);
        init(root);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = chatsProvider.getAll(authProvider.getUid());
        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class).build();

        chatsAdapter = new ChatsAdapter(options,getContext());
        recyclerChats.setAdapter(chatsAdapter);
        chatsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatsAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(chatsAdapter.getListener() != null){
            chatsAdapter.getListener().remove();
        }

        if(chatsAdapter.getListenerInfo() != null){
            chatsAdapter.getListenerInfo().remove();
        }

        if(chatsAdapter.getListenerLastMessage() != null){
            chatsAdapter.getListenerLastMessage().remove();
        }
    }

    private void init(View root) {

        recyclerChats = root.findViewById(R.id.recyclerChats);
        toolbar = root.findViewById(R.id.toolbar);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity())
                .getSupportActionBar()).setTitle("Chats");
        Objects.requireNonNull(((AppCompatActivity) requireActivity())
                .getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerChats.setLayoutManager(linearLayoutManager);

        chatsProvider = new ChatsProvider();
        authProvider = new AuthProvider();
    }
}