package com.example.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.models.Song;
import com.example.tunetide.R;

import java.util.ArrayList;

public class ListItemAdapter extends ArrayAdapter<Song> {
    public ListItemAdapter(Context context, ArrayList<Song> songs) {
        super(context, R.layout.list_item, songs);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        ImageView albumCoverView = convertView.findViewById(R.id.album_cover);
        TextView songTitle = convertView.findViewById(R.id.song_title);
        Song song = getItem(position);
        songTitle.setText(song.getTitle());
//        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//        try {
//            mmr.setDataSource(getContext(), song.getUri());
//            byte[] data = mmr.getEmbeddedPicture();
//            if (data != null) {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                albumCoverView.setImageBitmap(bitmap); // Display embedded album art
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            albumCoverView.setVisibility(View.GONE); // Hide ImageView on error
//        } finally {
//            mmr.release();
//        }

        return convertView;
    }
}
