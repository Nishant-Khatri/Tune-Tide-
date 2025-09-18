package com.example.tunetide;

import android.app.Application;

import com.example.service.MusicService;

public class TuneTideApp extends Application {
    private static MusicService musicService;

    public static void setMusicService(MusicService service) {
        musicService = service;
    }

    public static MusicService getMusicService() {
        return musicService;
    }
}