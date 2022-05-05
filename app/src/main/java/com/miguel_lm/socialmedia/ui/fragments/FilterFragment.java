package com.miguel_lm.socialmedia.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.ui.activities.FiltersActivity;


public class FilterFragment extends Fragment {

    CardView cardview_ps4, cardview_xbox, cardview_nintendo, cardview_pc;

    public FilterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_filter, container, false);
        inits(root);
        events();

        return root;
    }

    private void inits(View root) {

        cardview_ps4 = root.findViewById(R.id.cardview_ps4);
        cardview_xbox = root.findViewById(R.id.cardview_xbox);
        cardview_nintendo = root.findViewById(R.id.cardview_nintendo);
        cardview_pc = root.findViewById(R.id.cardview_pc);
    }

    private void events() {

        cardview_ps4.setOnClickListener(v -> goToFilterActivity("PS4"));
        cardview_xbox.setOnClickListener(v -> goToFilterActivity("XBOX"));
        cardview_nintendo.setOnClickListener(v -> goToFilterActivity("NINTENDO"));
        cardview_pc.setOnClickListener(v -> goToFilterActivity("PC"));
    }

    private void goToFilterActivity(String category){

        Intent intent = new Intent(getContext(), FiltersActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}