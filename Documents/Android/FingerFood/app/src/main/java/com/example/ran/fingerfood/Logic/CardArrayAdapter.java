package com.example.ran.fingerfood.Logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ran.fingerfood.R;

import java.util.List;

/**
 * Created by ran on 23/01/2018.
 */

public class CardArrayAdapter extends ArrayAdapter<Card> {

    Context context;

    public CardArrayAdapter(Context context , int resourceId , List<Card> items){
        super(context,resourceId,items);
    }

    public View getView(int position, View convertView , ViewGroup parent){
        Card cardItem = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item , parent , false);
        }
        TextView restName = (TextView)convertView.findViewById(R.id.rest_name_id);
        ImageView image = (ImageView)convertView.findViewById(R.id.image);

        restName.setText(cardItem.getRestName());

        Glide.clear(image);
        Glide.with(convertView.getContext()).load(cardItem.getImageUrl())
                .placeholder(R.drawable.app_logo)
                .error(R.drawable.app_logo).into(image);
        return convertView;
    }
}
