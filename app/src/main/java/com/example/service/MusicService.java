package com.example.service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


import androidx.core.app.NotificationCompat;

import com.example.models.Constants;
import com.example.models.Song;
import com.example.tunetide.R;
import com.example.tunetide.TuneTideApp;
import com.example.tunetide.receiver.NotificationActionReceiver;

import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private List<Song> songsList;

    private MediaSessionCompat mediaSession;
    private int currentPosition = 0;
    private final IBinder binder = new LocalBinder();

    private AudioManager audioManager;


    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean wasPlayingBeforeCall = false;


    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TuneTideApp.setMusicService(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaSession= new MediaSessionCompat(this, "TuneTideSession");
        handleNotificationForNewerVersions();
        registerPhoneStateListener();
        showMediaNotification("Tune Tide", true);

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

    private void registerPhoneStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {

            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            wasPlayingBeforeCall = true;
                            pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (wasPlayingBeforeCall) {
                            wasPlayingBeforeCall = false;
                            play();
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, NotificationActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT |  PendingIntent.FLAG_IMMUTABLE);

    }

    private void handleNotificationForNewerVersions() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
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
            showMediaNotification(songsList.get(position).getTitle(), true);
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
                int result = audioManager.requestAudioFocus(afChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.start();
                    showMediaNotification(songsList.get(currentPosition).getTitle(), true);
                }
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            showMediaNotification(songsList.get(currentPosition).getTitle(), false);
        }
    }

    private final AudioManager.OnAudioFocusChangeListener afChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                break;
        }
    };

    private void showMediaNotification(String title, boolean isPlaying) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.notification_prev, "Previous", getPendingIntent(Constants.ACTION_PREV))
                .addAction(isPlaying ? R.drawable.notification_pause : R.drawable.notification_play, "PlayPause", getPendingIntent(Constants.ACTION_PLAY_PAUSE))
                .addAction(R.drawable.notification_next,"Next", getPendingIntent(Constants.ACTION_NEXT))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0,1,2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);
        startForeground(1, builder.build());

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
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        TuneTideApp.setMusicService(null);

        super.onDestroy();
    }
}
