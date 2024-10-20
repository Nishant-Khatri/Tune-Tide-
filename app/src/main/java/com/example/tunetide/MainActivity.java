package com.example.tunetide;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> songTitles;
    ArrayList<Uri> songUris;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list);
        FloatingActionButton fabShuffle = findViewById(R.id.fab_shuffle);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        loadSongs();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
        fabShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleSongs();
            }
        });
    }

    private void loadSongs() {
        songTitles = new ArrayList<>();
        songUris = new ArrayList<>();
        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID};
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, MediaStore.Audio.Media.TITLE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                // Get the song URI
                if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)) > 5000) {
                    Uri uri = ContentUris.withAppendedId(contentUri, id);
                    songTitles.add(title);
                    songUris.add(uri);
                }
            }
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ListItemAdapter(MainActivity.this, songTitles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startPlayingSong(songUris, songTitles, i);
            }
        });

    }

    private void startPlayingSong(ArrayList<Uri> songUriList, ArrayList<String> songTitleList, int position) {
        Intent intent = new Intent(getApplicationContext(), PlaySong.class);
        intent.putExtra("songlist", songUriList);
        intent.putExtra("position", position);
        intent.putExtra("songTitles", songTitleList);
        startActivity(intent);
    }

    private void shuffleSongs() {
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < songTitles.size(); i++) {
            pairs.add(new Pair(songTitles.get(i), songUris.get(i)));
        }
        Collections.shuffle(pairs);

        ArrayList<String> shuffledTitles = new ArrayList<>();
        ArrayList<Uri> shuffledUris = new ArrayList<>();

        for (Pair pair : pairs) {
            shuffledTitles.add(pair.title);  // Add shuffled title
            shuffledUris.add(pair.uri);    // Add shuffled URI
        }
        startPlayingSong(shuffledUris, shuffledTitles, new java.util.Random().nextInt(pairs.size()));
    }

    private static class Pair {
        String title;
        Uri uri;

        Pair(String title, Uri uri) {
            this.title = title;
            this.uri = uri;
        }
    }
}