package com.example.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.models.Song;

import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private List<Song> songsList;
    private int currentPosition = 0;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            playNextSong();
            Intent intent = new Intent("com.example.tunetide.SONG_COMPLETED");
            sendBroadcast(intent);
            // Automatically start playback of the next song
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            List<Song> receivedSongsList = intent.getParcelableArrayListExtra("songslist");
            int receivedPosition = intent.getIntExtra("position", 0);
            if (receivedSongsList != null && !receivedSongsList.isEmpty()) {
                setSongsList(receivedSongsList, receivedPosition);
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Set the song list and start playing the song at the given position.
     */
    public void setSongsList(List<Song> songs, int position) {
        this.songsList = songs;
        this.currentPosition = position;
        playSong(currentPosition);
    }

    /**
     * Play the song at the given position.
     */
    public void playSong(int position) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();

        } mediaPlayer.reset();

        currentPosition = position;
        Uri uri = songsList.get(position).getUri();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Play the next song in the list.
     */
    public void playNextSong() {
        currentPosition = (currentPosition + 1) % songsList.size();
        playSong(currentPosition);
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * Play the previous song in the list.
     */
    public void playPreviousSong() {
        currentPosition = (currentPosition - 1 < 0) ? songsList.size() - 1 : currentPosition - 1;
        playSong(currentPosition);
    }

    /**
     * Toggle between play and pause.
     */
    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /**
     * Seek to a specific position in the current song.
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    /**
     * Get the current playback position of the media player.
     */
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Get the duration of the current song.
     */
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * Check if the media player is currently playing.
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Get the current song's position in the list.
     */
    public int getCurrentSongPosition() {
        return currentPosition;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
