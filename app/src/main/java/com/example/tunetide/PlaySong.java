package com.example.tunetide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        updateSeek.interrupt();
    }

    TextView textView,duration;
    ImageView play,prev,next;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    Thread updateSeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView=findViewById(R.id.textView2);
        play=findViewById(R.id.play);
        prev=findViewById(R.id.prev);
        seekBar=findViewById(R.id.seekBar);
        next=findViewById(R.id.next);
        duration=findViewById(R.id.durat);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        songs=(ArrayList) bundle.getParcelableArrayList("songlist");
        textContent=intent.getStringExtra("CurrentSong");

        textView.setText(textContent);
        textView.setSelected(true);
        position=intent.getIntExtra("position",0);
        Uri uri=Uri.parse(songs.get(position).toString());
        mediaPlayer=MediaPlayer.create(this,uri);
        mediaPlayer.start();
        duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        updateSeek=new Thread() {
            @Override
            public void run() {
                int currentPos = 0;
                try {
                        while (currentPos<mediaPlayer.getDuration()){
                            currentPos=mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPos);
                            sleep(800);
                        }
                }
            catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();

                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mediaPlayer.isPlaying()){
                            play.setImageResource(R.drawable.play);
                            mediaPlayer.pause();

                        }
                        else {
                            play.setImageResource(R.drawable.pause);
                            mediaPlayer.start();

                        }
                    }
                });
                prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        if(position!=0){
                            position=position-1;
                        }
                        else{
                            position=songs.size()-1;
                        }
                        Uri uri=Uri.parse(songs.get(position).toString());
                        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                        seekBar.setProgress(0);
                        mediaPlayer.start();

                        play.setImageResource(R.drawable.pause);
                        seekBar.setMax(mediaPlayer.getDuration());

                        textContent=songs.get(position).getName().toString();
                        textView.setText(textContent);
                        duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

                    }
                });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position!=songs.size()-1){
                    position=position+1;
                }
                else{
                    position=0;
                }
                Uri uri=Uri.parse(songs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                seekBar.setProgress(0);
                mediaPlayer.start();

                play.setImageResource(R.drawable.pause);
                seekBar.setMax(mediaPlayer.getDuration());
                textContent=songs.get(position).getName().toString();
                textView.setText(textContent);
                duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position!=songs.size()-1){
                    position=position+1;
                }
                else{
                    position=0;
                }
                Uri uri=Uri.parse(songs.get(position).toString());
                seekBar.setProgress(0);
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.pause);
                seekBar.setMax(mediaPlayer.getDuration());

                textContent=songs.get(position).getName().toString();
                textView.setText(textContent);
                duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

            }
        });

        }
        public String milliSecondsToTimer(long milli){
            String finalTimer="";
            String se;
            int hour=(int)(milli/(1000*60*60));
            int min=(int)(milli%(1000*60*60))/(1000*60);

            int sec=(int)(milli%(1000*60*60))%(1000*60)/1000;
            if(hour>0)
                finalTimer=hour+":";
            if(sec<10)
                se="0"+sec;
            else
                se=""+sec;
            finalTimer=finalTimer+min+":"+se;
            return finalTimer;
    }
}