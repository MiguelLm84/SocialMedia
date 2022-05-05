package com.miguel_lm.socialmedia.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.miguel_lm.socialmedia.R;
import com.miguel_lm.socialmedia.model.SliderItem;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;
import java.util.List;


public class SliderAdapter extends
        SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private final Context context;
    private final List<SliderItem> mSliderItems;

    public SliderAdapter(Context context, List<SliderItem> sliderItems) {
        this.context = context;
        mSliderItems = sliderItems;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        @SuppressLint("InflateParams") View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_layout_item, null);
        return new SliderAdapterVH(inflate);
        }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

        SliderItem sliderItem = mSliderItems.get(position);
        if(sliderItem.getImageUrl() != null){
            if(!sliderItem.getImageUrl().isEmpty()){
                Picasso.with(context).load(sliderItem.getImageUrl()).into(viewHolder.iv_slider);
            }
        }
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView iv_slider;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            iv_slider = itemView.findViewById(R.id.iv_slider);
            this.itemView = itemView;
        }
    }
}