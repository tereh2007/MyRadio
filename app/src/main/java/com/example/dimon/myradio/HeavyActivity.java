package com.example.dimon.myradio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.dimon.myradio.R.id.btn_play;


/**
 * Created by Dimon on 12.03.2018.
 */

public class HeavyActivity extends Activity implements Constants.ACTION, ServiceConnection, View.OnClickListener {
    final String LOG_TAG = "HeavyActivity";

    //*****************************************************
    public ArrayList<String> titleList = new ArrayList<> ();
    private ArrayAdapter<String> adapter;
    ImageView album_art;
    // Media Player
    MediaPlayer mediaPlayer;
    // Code for html parsing
    GetMetaData th_async;
    //Receiver
    BroadcastReceiver receive;
    // Control Button
    private ImageButton mediaButton;
    private MediaController mMediaController;
    //******************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_heavy);

        mediaButton = findViewById(btn_play);

        ListView lv = findViewById(R.id.list_info);

        adapter = new ArrayAdapter<> ( this, R.layout.activity_info, R.id.about_item, titleList );

        callAsynchronousTask();

        lv.setAdapter(adapter);

        album_art = findViewById(R.id.album_art);
        album_art.setImageResource(R.drawable.radio_logo);

        Intent intent = new Intent(this, NotificationService.class);
        getApplicationContext().bindService(intent, this, 0);
    }
    //** Set up Listener for control buttons
    @Override
    public void onClick(View v) {

        Log.d(LOG_TAG,"OnClick" );

        switch (v.getId()) {
            case btn_play:
                if (Objects.requireNonNull(mMediaController.getPlaybackState()).getState() == PlaybackState.STATE_PLAYING) {
                    mMediaController.getTransportControls().pause();
                } else if (mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PAUSED) {
                    mMediaController.getTransportControls().play();
                } else {
                    Uri uri = Uri.parse("http://online-radioroks2.tavrmedia.ua/RadioROKS_HardnHeavy");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mMediaController.getTransportControls().playFromUri(uri, null);
                    } else {
                        Bundle bundle = new Bundle();
                        mMediaController.getTransportControls().playFromSearch("", bundle);
                    }
                }
            break;
        }
    }
    //*** Bind Activity with Notification Service
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof NotificationService.ServiceBinder) {
            mMediaController = new MediaController (HeavyActivity.this,
                    ((NotificationService.ServiceBinder) service).getService().getMediaSessionToken());
            mMediaController.registerCallback(mMediaControllerCallback);
        }
        Log.d(LOG_TAG,"onServiceConnected" );
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LOG_TAG,"onServiceDisconnected" );


    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, NotificationService.class);
        startService(intent);
        Log.d(LOG_TAG,"onStart" );
    }
    //*** Create Callback for MediaSession
    private MediaController.Callback mMediaControllerCallback = new MediaController.Callback() {
        @SuppressLint("SwitchIntDef")
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {

            switch (state.getState()) {
                case PlaybackState.STATE_NONE:
                    mediaButton.setImageResource(R.drawable.button_play);
                break;
                case PlaybackState.STATE_PLAYING:
                    mediaButton.setImageResource(R.drawable.button_pause);
                break;
                case PlaybackState.STATE_PAUSED:
                    mediaButton.setImageResource(R.drawable.button_play);
                break;
            }
        }
    };
    /**********************************************************************************************
    ***************  Парсим код HTML с сайта трансляции (обязательно в фоне) ***********************
    **************  + таймер для повторного выполнения парсинга            ************************
    **********************************************************************************************/
    public void callAsynchronousTask() {
        Log.d(LOG_TAG,"Parcing" );
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {

                if(th_async != null) th_async.cancel(true);
                th_async = null;
                th_async =  new GetMetaData(adapter);
                th_async.execute();
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000);
    }
    //**********************************************************************************************
    //**********************************************************************************************
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,"OnDestroy");
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        unregisterReceiver(receive);
        onStart ();
    }
}
