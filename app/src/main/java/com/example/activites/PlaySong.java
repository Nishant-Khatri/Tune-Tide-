package com.example.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.models.Enums.SongPlaybackActionEnum;
import com.example.models.Song;
import com.example.service.MusicService;
import com.example.tunetide.R;

import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    private TextView textView, duration;
    private ImageView play, prev, next;
    private SeekBar seekBar;
    private ArrayList<Song> songsList;
    private int position;

    private MusicService musicService;
    private boolean isBound = false;

    private final Handler handler = new Handler();


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;

            // Start playback when the service is connected
            musicService.setSongsList(songsList, position);
            updateSongInfo();
            updateSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(songCompletionReceiver, new IntentFilter("com.example.tunetide.SONG_COMPLETED"));
        registerReceiver(playbackStateReceiver, new IntentFilter("com.example.tunetide.PLAYBACK_STATE_CHANGED"));
        if (isBound && musicService != null) {
            // Sync the UI with the current song
            position = musicService.getCurrentSongPosition();
            updateSongInfo();
            updateSeekBar();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        initializeViews();

        Intent intent = getIntent();
        songsList = intent.getParcelableArrayListExtra("songslist");
        position = intent.getIntExtra("position", 0);

        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        play.setOnClickListener(view -> togglePlayPause());
        prev.setOnClickListener(view -> playPreviousSong());
        next.setOnClickListener(view -> playNextSong());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initializeViews() {
        textView = findViewById(R.id.textView2);
        play = findViewById(R.id.play);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        duration = findViewById(R.id.durat);
    }

    private void togglePlayPause() {
        if (musicService.isPlaying()) {
            musicService.pause(true);
            play.setImageResource(R.drawable.play);
        } else {
            musicService.play();
            play.setImageResource(R.drawable.pause);
        }
    }

    private void playNextSong() {
        musicService.playNextSong();
        updateSongInfo();
        seekBar.setProgress(0);
        updateSeekBar();
    }

    private void playPreviousSong() {
        musicService.playPreviousSong();
        updateSongInfo();
        seekBar.setProgress(0);
        updateSeekBar();
    }

    private void updateSongInfo() {
        position = musicService.getCurrentSongPosition();
        Song currentSong = songsList.get(position);
        textView.setText(currentSong.getTitle());
        textView.setSelected(true);
        duration.setText(milliSecondsToTimer(musicService.getDuration()));
        seekBar.setMax(musicService.getDuration());
    }

    private void updateSeekBar() {
        seekBar.setProgress(musicService.getCurrentPosition());
        duration.setText(milliSecondsToTimer(musicService.getDuration() - musicService.getCurrentPosition()));
        handler.postDelayed(this::updateSeekBar, 200);
    }

    private String milliSecondsToTimer(long milliSeconds) {
        int hours = (int) (milliSeconds / (1000 * 60 * 60));
        int minutes = (int) (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliSeconds % (1000 * 60)) / 1000);

        return (hours > 0 ? hours + ":" : "") + String.format("%02d:%02d", minutes, seconds);
    }

    private final BroadcastReceiver playbackStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch ((SongPlaybackActionEnum) intent.getSerializableExtra("playbackAction")){
                case PLAY :
                    play.setImageResource(R.drawable.pause);
                    break;
                case PAUSE:
                    play.setImageResource(R.drawable.play);
                    break;

                case NEXT:
                case PREV:
                    updateSongInfo();
                    break;

            }
        }
    };

    private final BroadcastReceiver songCompletionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSongInfo();
            seekBar.setProgress(0);
            updateSeekBar();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(songCompletionReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isBound) {
            musicService.pause(true); // Pause the music
            isBound = false;
        }
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
            unbindService(serviceConnection);
            isBound = false;
        handler.removeCallbacksAndMessages(null);
    }
}
