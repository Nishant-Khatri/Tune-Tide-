package com.example.tunetide;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    ListView listView,list2;
    ArrayList<File> mySongs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.list);
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                      new Thread(new Runnable() {
                          @Override
                          public void run() {
                            mySongs=new ArrayList<>();
                              fetchSongs(Environment.getExternalStorageDirectory(),mySongs);
                              runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      String [] items=new String[mySongs.size()];
                                      for(int i=0;i<mySongs.size();i++){
                                          items[i]=mySongs.get(i).getName().replace(".mp3","");
                                      }
                                      ArrayAdapter<String> adapter= new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,items );
                                      listView.setAdapter(adapter);

                                  }
                              });
                          }
                      }).start();






                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent intent=new Intent(getApplicationContext(),PlaySong.class);
                                String currentSong=listView.getItemAtPosition(i).toString();
                                intent.putExtra("songlist",mySongs);
                                intent.putExtra("CurrentSong",currentSong);
                                intent.putExtra("position",i);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }
        public void fetchSongs(File file , ArrayList<File> song){

               File[] songs=file.listFiles();
               if(songs!=null) {
                for(File myFile : songs){
                    if(!myFile.isHidden()&&myFile.isDirectory()){
                      fetchSongs(myFile,song);
                    }
                    else{
                        if((myFile.getName().endsWith("mp3"))&&!myFile.getName().startsWith(".")){
                            song.add(myFile);
                        }
                    }
               }
    }


        }
}