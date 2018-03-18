package com.example.ran.fingerfood.Logic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.ran.fingerfood.R;

import java.util.ArrayList;

/**
 * Created by idan on 19/02/2018.
 */


public class GridImageAdapter extends RecyclerView.Adapter<GridImageAdapter.ViewHolder>{

    public interface OnItemClickListener {
        void onItemClick(Card card);
    }

    private Context mContext;
    private ArrayList<Card> galleryList;
    private final OnItemClickListener listener;

/////// Constructor /////////
    public GridImageAdapter(Context mContext, ArrayList<Card> galleryList, OnItemClickListener listener) {
        this.mContext = mContext;
        this.galleryList = galleryList;
        this.listener = listener;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GridImageAdapter.ViewHolder viewHolder, int position) {
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //viewHolder.img.setImageResource(galleryList.get(position).getImageUrl());

        Glide.clear(viewHolder.img);
        Glide.with(mContext).load(galleryList.get(position).getImageUrl()).into(viewHolder.img);

        viewHolder.bind(galleryList.get(position),listener);


    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;

        public ViewHolder(View view){
            super(view);
            img = (ImageView) view.findViewById(R.id.img);
        }


        public void bind(final Card card , final OnItemClickListener listener){

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override public void onClick(View v) {
                    listener.onItemClick(card);
                }
            });
        }
    }
}
