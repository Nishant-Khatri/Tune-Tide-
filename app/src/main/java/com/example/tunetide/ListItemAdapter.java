package com.example.tunetide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ListItemAdapter extends ArrayAdapter<String> {
    public ListItemAdapter(Context context, ArrayList<String> songTitles) {
        super(context, R.layout.list_item, songTitles);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView songTitle = convertView.findViewById(R.id.song_title);
        songTitle.setText(getItem(position));
        return convertView;
    }
}
