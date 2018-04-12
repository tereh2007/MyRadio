package com.example.dimon.myradio;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.util.Objects;
import static com.example.dimon.myradio.Constants.ACTION.ACTION_PAUSE;
import static com.example.dimon.myradio.Constants.ACTION.ACTION_PLAY;

/**
 * Created by Dimon on 18.03.2018.
 */

public class NotificationService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    final String LOG_TAG = "NOTIFICATION";

    //***** Media Session
    private MediaSession mMediaSession;
    private PlaybackState mPlaybackState;
    private MediaController mMediaController;

    //***** Media Player
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate ();
        Log.d(LOG_TAG,"onCreate" );

        //** setup media session with callback
        mPlaybackState = new PlaybackState.Builder (  )
                .setState ( PlaybackState.STATE_NONE, 0, 1.0f )
                .build ();
        //** create MediaSession
        mMediaSession = new MediaSession (this, Constants.SESSION_TAG);

        //** Set the callback to receive updates for the MediaSession
        mMediaSession.setCallback ( mMediaSessionCallback );

        //** this session is currently active and ready to receive commands
        mMediaSession.setActive ( true );

        //** int FLAG_HANDLES_MEDIA_BUTTONS - Set this flag on the session to indicate
        // that it can handle media button events.
        // int FLAG_HANDLES_TRANSPORT_CONTROLS - Set this flag on the session to indicate
        // that it handles transport control commands through its MediaSession.Callback.
        mMediaSession.setFlags ( MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS |
                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS );
        //** Update the current playback state
        mMediaSession.setPlaybackState ( mPlaybackState );

        //** Get instance to AudioManager
        getSystemService ( Context.AUDIO_SERVICE );

        //** Create Media Player
        mMediaPlayer = new MediaPlayer ();
        mMediaPlayer.setOnPreparedListener ( this );
        mMediaPlayer.setOnCompletionListener ( this );
        mMediaPlayer.setOnBufferingUpdateListener ( this );

        //** Create media controller
        mMediaController = new MediaController ( this, mMediaSession.getSessionToken() );
    }

    private Binder mBinder = new ServiceBinder();
    //*** Media PLayer
    @Override
    public void onPrepared(MediaPlayer mp) {

        Log.d(LOG_TAG,"onPrepared" );

        mMediaPlayer.start();
        mPlaybackState = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0, 1.0f)
                .build();
        mMediaSession.setPlaybackState(mPlaybackState);
        updateNotification();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {

        Log.d(LOG_TAG,"onCompletion" );

        mPlaybackState = new PlaybackState.Builder (  )
                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                .build();
        mMediaSession.setPlaybackState(mPlaybackState);
        mMediaPlayer.reset();

    }
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d(LOG_TAG, "onBufferingUpdate" + percent);
    }
    //** Create Binder for  NotificationServise to bind it with HeavyActivity,
    // in HeavyActivity we have to create "onServiceConnected"
    public class ServiceBinder extends Binder {

        public NotificationService getService() {
            return NotificationService.this;
        }
    }
    //** Create method for mMediaSessionCallback
    private MediaSession.Callback mMediaSessionCallback = new MediaSession.Callback() {

        //** Set source for Media Player
        @Override
        //** Override to handle requests to begin playback from a search query.
        // An empty query indicates that the app may play any music.
        // The implementation should attempt to make a smart choice about what to play.
        public void onPlayFromSearch(String query, Bundle extras) {
            Uri uri = extras.getParcelable(Constants.DATA_STREAM);
             onPlayFromUri ( uri, null );
            Log.d(LOG_TAG,"onPlayFromSearch" );
        }
        //** Override to handle requests to play a specific media item represented by a URI.
        @SuppressLint("SwitchIntDef")
        @Override
        public void onPlayFromUri(Uri uri , Bundle extras) {

            Log.d(LOG_TAG,"onPlayFromUri" );

            try {
                switch (mPlaybackState.getState ()) {
                    case PlaybackState.STATE_PLAYING: // =3
                    case PlaybackState.STATE_PAUSED:  //=2
                        mMediaPlayer.reset ();
                        mMediaPlayer.setAudioStreamType ( AudioManager.STREAM_MUSIC );
                        mMediaPlayer.setDataSource ( Constants.DATA_STREAM );
                        mMediaPlayer.prepareAsync ();
                        mPlaybackState = new PlaybackState.Builder ()
                                .setState (PlaybackState.STATE_CONNECTING, 0 , 1.0f)
                                .build ();
                        mMediaSession.setPlaybackState ( mPlaybackState );
                    break;
                    case PlaybackState.STATE_NONE:
                        mMediaPlayer.setAudioStreamType ( AudioManager.STREAM_MUSIC );
                        mMediaPlayer.setDataSource ( Constants.DATA_STREAM );
                        mMediaPlayer.prepareAsync ();
                        mPlaybackState = new PlaybackState.Builder (  )
                                .setState ( PlaybackState.STATE_CONNECTING,0, 1.0f )
                                .build ();
                        mMediaSession.setPlaybackState ( mPlaybackState );
                    break;
                }
            } catch (IOException ignored) {}
        }
       @SuppressLint("SwitchIntDef")
       @Override
       public  void onPlay() {

                switch (mPlaybackState.getState ()) {
                    case PlaybackState.STATE_PAUSED:
                        mMediaPlayer.start ();
                        mPlaybackState = new PlaybackState.Builder (  )
                                .setState ( PlaybackState.STATE_PLAYING,0,1.0f )
                                .build ();
                        mMediaSession.setPlaybackState ( mPlaybackState );
                        updateNotification();
                    break;
                }
           Log.d(LOG_TAG,"onPlay" );
       }
       @SuppressLint("SwitchIntDef")
       @Override
       public  void onPause() {

                switch (mPlaybackState.getState ()) {
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlayer.pause ();
                        mPlaybackState = new PlaybackState.Builder (  )
                                .setState ( PlaybackState.STATE_PAUSED,0,1.0f )
                                .build ();
                        mMediaSession.setPlaybackState ( mPlaybackState );
                        updateNotification();
                    break;
                }
           Log.d(LOG_TAG,"onPause" );
       }
       @SuppressLint("SwitchIntDef")
       @Override
       public void onStop() {

                switch (mPlaybackState.getState ()) {
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlayer.release ();
                        updateNotification ();
                    break;
                }
           Log.d(LOG_TAG,"onStop" );
       }
    };
    private void updateNotification() {

        Log.d(LOG_TAG,"updateNotification" );

        Notification.Action playPauseAction = mPlaybackState.getState () == PlaybackState.STATE_PLAYING ?
                createAction(android.R.drawable.ic_media_pause, "pause", ACTION_PAUSE):
                createAction(android.R.drawable.ic_media_play, "play", ACTION_PLAY);
        //*** Create Notification for Media Session
        Notification notification = new Notification.Builder ( this )
                .setPriority ( Notification.PRIORITY_DEFAULT )
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setContentTitle("Radio Roks Hard&Heavy")
                .setContentText("TEST")
                .setOngoing(mPlaybackState.getState() == PlaybackState.STATE_PLAYING)
                .setShowWhen(false)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setAutoCancel(false)
                .addAction(playPauseAction)
                .setStyle(new Notification.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0))
                .build();
                notification.icon = android.R.drawable.ic_lock_silent_mode_off;
        ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).notify(1, notification);
    }

    private Notification.Action createAction(int iconResId, String title, String action) {

        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(iconResId, title, pendingIntent).build();

    }
    public NotificationService() {}
    //***************************
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public MediaSession.Token getMediaSessionToken() {

        Log.d(LOG_TAG,"MediaSessionToken" );

        return mMediaSession.getSessionToken();

    }
    //***************************
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LOG_TAG,"onStartCommand" );

        if (intent != null && intent.getAction() != null) {

            switch (intent.getAction()) {
                case ACTION_PLAY:
                    mMediaController.getTransportControls().play();
                    break;
                case ACTION_PAUSE:
                    mMediaController.getTransportControls().pause();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaSession.release();
        Log.d(LOG_TAG,"onDestroy" );
    }
}