package com.example.tunetide.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.models.Constants;
import com.example.service.MusicService;
import com.example.tunetide.TuneTideApp;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicService service = ((TuneTideApp) context.getApplicationContext()).getMusicService();

        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case Constants.ACTION_PREV:
                service.playPreviousSong();
                break;
            case Constants.ACTION_PLAY_PAUSE:
                if (service.isPlaying()) {
                    service.pause();
                } else {
                    service.play();
                }

                break;
            case Constants.ACTION_NEXT:
                service.playNextSong();
                break;
        }

    }
}
