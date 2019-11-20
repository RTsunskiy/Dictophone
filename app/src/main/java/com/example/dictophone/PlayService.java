package com.example.dictophone;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PlayService extends Service {
    private Messenger mMessenger = new Messenger(new InternalHandler());
    private Messenger mMainActivityMessenger;
    public static final int MSG_START_PLAYER = 202;
    public static final int MSG_STOP_PLAYER = 203;
    private MediaPlayer mediaPlayer;
    private String fileName;

    class InternalHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_START_PLAYER:
                    mMainActivityMessenger = msg.replyTo;
                    playStart();
                    break;
                case MSG_STOP_PLAYER:
                    mMainActivityMessenger = msg.replyTo;
                    playStop();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    public void playStart() {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
