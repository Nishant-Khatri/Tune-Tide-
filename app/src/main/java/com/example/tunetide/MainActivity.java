package com.example.tunetide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.activites.PlaySong;
import com.example.adapters.ListItemAdapter;
import com.example.models.Song;
import com.example.service.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    ListView listView;

    ArrayList<Song> songsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list);
        FloatingActionButton fabShuffle = findViewById(R.id.fab_shuffle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.READ_MEDIA_AUDIO)
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
        } else {
            // Fallback for older Android versions
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
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100);  // Request code for notification permission
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    102);  // Request code for phone state
        }
        fabShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleSongs();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("MainActivity", "Notification permission granted");
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Phone state permission granted");
            } else {
                Toast.makeText(this, "Phone state permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSongs() {
        songsList = new ArrayList<>();
        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID};
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, MediaStore.Audio.Media.TITLE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                // Get the song URI

                if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)) > 5000) {
                    Uri uri = ContentUris.withAppendedId(contentUri, id);
                    //MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                    String thumbnail = null;
//                    try {
//                        mmr.setDataSource(this, uri);
//                        byte[] data = mmr.getEmbeddedPicture();
//                        if (data != null) {
//                            thumbnail = Base64.encodeToString(data, Base64.DEFAULT); // Encode album art as a Base64 string
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        mmr.release();
//                    }
                    String filePath =cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if(filePath != null && filePath.toLowerCase().endsWith(".mp3")) {
                        songsList.add(new Song(id, title, uri, thumbnail));
                    }
                }
            }
            cursor.close();
        }
        ArrayAdapter<Song> adapter = new ListItemAdapter(MainActivity.this, songsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startPlayingSong(songsList, i);
            }
        });

    }


    private void startPlayingSong(ArrayList<Song> songsList, int position) {
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putParcelableArrayListExtra("songslist", songsList);
        serviceIntent.putExtra("position", position);
        ContextCompat.startForegroundService(this, serviceIntent);

        // Start the PlaySong activity
        Intent activityIntent = new Intent(this, PlaySong.class);
        activityIntent.putExtra("songslist", songsList);
        activityIntent.putExtra("position", position);
        startActivity(activityIntent);
    }

    private void shuffleSongs() {
        Collections.shuffle(songsList);
        startPlayingSong(songsList, new java.util.Random().nextInt(songsList.size()));
    }

}