package com.example.tunetide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if(updateSeek!=null)
        {updateSeek.interrupt();}
        //updateSeek=null;
        //updateSong=null;
    }

    TextView textView,duration;
    ImageView play,prev,next;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    Thread updateSeek,updateSong;
    boolean isSongPlaying=false;
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

        // Inside the onCreate method, after mediaPlayer.start():
        updateSong = new Thread() {
            @Override
            public void run() {
                try {
                    while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        runOnUiThread(() -> {
                            int currentPosition = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                            duration.setText(milliSecondsToTimer(mediaPlayer.getDuration() - currentPosition));
                        });
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSong.start();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromuser) {
//                        if(fromuser&&mediaPlayer!=null)
//                        {
//                            mediaPlayer.seekTo(progress);
//                        }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(isSongPlaying)
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer=null;
                }
                if(position!=songs.size()-1){
                    position=position+1;
                }
                else{
                    position=0;
                }

                Uri uri =Uri.parse(songs.get(position).toString());
//                seekBar.setProgress(0);

                try {
                    mediaPlayer=new MediaPlayer();
                    mediaPlayer.setDataSource(getApplicationContext(),uri);
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                            play.setImageResource(R.drawable.pause);
                            seekBar.setMax(mp.getDuration());

                            textContent=songs.get(position).getName().toString();
                            textView.setText(textContent);
                            duration.setText(milliSecondsToTimer(mp.getDuration()));

                            startUpdateSeekThread();
                        }
                    });
                    mediaPlayer.prepareAsync();


                } catch (Exception e) {
                   e.printStackTrace();
                }
                // mediaPlayer=MediaPlayer.create(getApplicationContext(), uri);
            }
        });





        updateSeek=new Thread() {
            @Override
            public void run() {
                int currentPos = 0;
                try {
                        while (mediaPlayer!=null && currentPos<mediaPlayer.getDuration()){
                            currentPos=mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPos);
                            sleep(200);
                        }

                }
            catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();

       play.setOnClickListener(view -> {
                  if ( mediaPlayer!=null )
                  {
                      if( mediaPlayer.isPlaying()) {
                          isSongPlaying=false;
                          play.setImageResource(R.drawable.play);
                          mediaPlayer.pause();}

                       else  {
                      isSongPlaying=true;
                          play.setImageResource(R.drawable.pause);
                          mediaPlayer.start();
                      }
                  }



                });
        prev.setOnClickListener(view -> {
                    isSongPlaying=false;
                    if(mediaPlayer!=null)
                    {
                         mediaPlayer.stop();
                         mediaPlayer.release();
                    }
                    if(position!=0){
                        position=position-1;
                    }
                    else{
                        position=songs.size()-1;
                    }
                    Uri uri12 =Uri.parse(songs.get(position).toString());
                    mediaPlayer=MediaPlayer.create(getApplicationContext(), uri12);
                //    seekBar.setProgress(0);

                    mediaPlayer.start();

                    play.setImageResource(R.drawable.pause);
                    seekBar.setMax(mediaPlayer.getDuration());




                    textContent=songs.get(position).getName().toString();
                    textView.setText(textContent);
                    duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
            startUpdateSeekThread();
                });
        next.setOnClickListener(view -> {
            isSongPlaying=false;
            if(mediaPlayer!=null)
            {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            if(position!=songs.size()-1){
                position=position+1;
            }
            else{
                position=0;
            }
            Uri uri1 =Uri.parse(songs.get(position).toString());
            mediaPlayer=MediaPlayer.create(getApplicationContext(), uri1);
          //  seekBar.setProgress(0);
            mediaPlayer.start();



            play.setImageResource(R.drawable.pause);
            seekBar.setMax(mediaPlayer.getDuration());
            textContent=songs.get(position).getName().toString();
            textView.setText(textContent);
            duration.setText(milliSecondsToTimer(mediaPlayer.getDuration()));

            startUpdateSeekThread();
        });



        }
        private  void startUpdateSeekThread()
        {
            if(updateSeek!=null &&updateSeek.isAlive())
            {
                updateSeek.interrupt();
            }
            seekBar.setProgress(0);
            updateSeek=new Thread()
            {
                public void run()
                {
                    try {
                        while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int currentPos = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPos);
                            sleep(800);
                        }}
                    catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
            };
            updateSeek.start();

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
