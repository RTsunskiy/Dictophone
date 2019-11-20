package com.example.dictophone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DictophoneService extends Service {

    private static final String CHANNEL_ID = "Channel_1";
    private static final int NOTIFICATION_ID = 1;
    private static String fileName;
    private final String STOP = "STOP";
    private final String PLAY_PAUSE = "PLAY/Pause";
    private RemoteViews notificationLayout;
    private boolean switcher = true;
    private MediaRecorder recorder;
    private IBinder mLocalBinder = new LocalBinder();
    private File myRecords;




    private List<String> fileNameList;


    class LocalBinder extends Binder {
        DictophoneService getBoundService() {
            return DictophoneService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.dictophone_notifcation_custom);
        createNotificationChannel();
        fileNameList = new ArrayList<>();
        myRecords = new File(Environment.getExternalStorageDirectory() + "/myRecords");
        if (!myRecords.exists()) {
            myRecords.mkdir();}
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        fileName = myRecords.toString() + "/recorder" + n + ".3gpp";
        if (intent != null && intent.getAction() != null) {
            if (switcher && intent.getAction() == PLAY_PAUSE) {
                notificationLayout.setImageViewResource(R.id.play_pause_btn,
                        R.drawable.ic_play_arrow_black_24dp);
                switcher = false;
                pauseRecording();
            } else if (!switcher && intent.getAction() == PLAY_PAUSE) {
                notificationLayout.setImageViewResource(R.id.play_pause_btn,
                        R.drawable.ic_pause_black_24dp);
                switcher = true;
                resumeRecording();
            }
            else if (intent.getAction() == STOP) {
                stopRecording();
                fileNameList.add(fileName);
            }
            updateNotification();
        } else {
            startRecording();
            startForeground(startId, createNotification());
        }
        return START_NOT_STICKY;
    }



    private void updateNotification() {
        Notification notification = createNotification();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(getString(R.string.notification_channel_description));
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Notification createNotification() {
        Notification customNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_keyboard_voice_black_24dp)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .build();

        Intent intent = new Intent(this, DictophoneService.class);
        intent.setAction(PLAY_PAUSE);
        PendingIntent pending = PendingIntent.getService(this, 0, intent, 0);
        notificationLayout.setOnClickPendingIntent(R.id.play_pause_btn, pending);

        Intent stopIntent = new Intent(this, MainActivity.class);
        intent.setAction(STOP);
        PendingIntent stopPending = PendingIntent.getActivity(this, 0, stopIntent, 0);
        notificationLayout.setOnClickPendingIntent(R.id.stop_btn, stopPending);
        return customNotification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void startRecording() {
        try {
            releaseRecorder();

            File outFile = new File(fileName);
            if (outFile.exists()) {
                outFile.delete();
            }
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(fileName);
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecording() {
        if (recorder != null) {
            recorder.pause();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording() {
        if (recorder != null) {
            recorder.resume();
        }
    }



    private void releaseRecorder() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }



    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
        }
    }
}
