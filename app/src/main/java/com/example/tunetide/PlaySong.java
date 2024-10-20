package com.example.tunetide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    TextView textView, duration;
    ImageView play, prev, next;
    ArrayList<Uri> songs;

    ArrayList<String> songsTitle;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    Runnable updateSeekBar;
    boolean isSongPlaying = false;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView2);
        play = findViewById(R.id.play);
        prev = findViewById(R.id.prev);
        seekBar = findViewById(R.id.seekBar);
        next = findViewById(R.id.next);
        duration = findViewById(R.id.durat);

        // Get data from intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songlist");
        songsTitle = (ArrayList) bundle.getParcelableArrayList("songTitles");
        position = intent.getIntExtra("position", 0);

        // Set the current song name in the TextView
        textView.setText(textContent);
        textView.setSelected(true);

        playSong(position);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromuser) {
                if (fromuser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        play.setOnClickListener(view -> togglePlayPause());

        prev.setOnClickListener(view -> playPreviousSong());

        next.setOnClickListener(view -> playNextSong());


    }

    private void playSong(int songPosition) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Release the previous media player
        }

        Uri uri = songs.get(position);
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
        duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
        seekBar.setMax(mediaPlayer.getDuration());

        textView.setText(songsTitle.get(position));
        duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

        startSeekBarUpdate();
        // When the song completes, automatically play the next one
        mediaPlayer.setOnCompletionListener(mp -> playNextSong());

    }


    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            isSongPlaying = false;
            play.setImageResource(R.drawable.play);
            mediaPlayer.pause();
        } else {
            isSongPlaying = true;
            play.setImageResource(R.drawable.pause);
            mediaPlayer.start();
        }
    }

    private void startSeekBarUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress((mediaPlayer.getCurrentPosition()));
                    duration.setText(milliSecondsToTimer(mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 200);
                }
            }
        };
        handler.post(updateSeekBar);
    }

    private void playNextSong() {
        position = (position + 1) % songs.size(); // Loop back to the first song if it's the last
        playSong(position);
    }

    private void playPreviousSong() {
        position = (position - 1 < 0) ? songs.size() - 1 : position - 1;
        playSong(position);
    }

    public String milliSecondsToTimer(long milli) {
        String finalTimer = "";
        String se;
        int hour = (int) (milli / (1000 * 60 * 60));
        int min = (int) (milli % (1000 * 60 * 60)) / (1000 * 60);

        int sec = (int) (milli % (1000 * 60 * 60)) % (1000 * 60) / 1000;
        if (hour > 0)
            finalTimer = hour + ":";
        if (sec < 10)
            se = "0" + sec;
        else
            se = "" + sec;
        finalTimer = finalTimer + min + ":" + se;
        return finalTimer;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
    }
}
