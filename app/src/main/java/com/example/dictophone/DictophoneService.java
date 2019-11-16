package com.example.dictophone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class DictophoneService extends Service {

    private static final String CHANNEL_ID = "Channel_1";
    private static final int NOTIFICATION_ID = 1;
    private RemoteViews notificationLayout;
    private boolean switcher = true;

    private final String STOP = "STOP";
    private final String PLAY = "PLAY";
    private final String PAUSE = "PAUSE";

    @Override
    public void onCreate() {
        super.onCreate();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.dictophone_notifcation_custom);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(startId, createNotification());

        if (intent != null && intent.getAction() != null) {
            String incomeIntent = intent.getAction();
            if (incomeIntent.equals(PAUSE)) {
                notificationLayout.setImageViewResource(R.id.play_pause_btn,
                        R.drawable.ic_play_arrow_black_24dp);
            }
            else if (incomeIntent.equals(PLAY)) {
                    notificationLayout.setImageViewResource(R.id.play_pause_btn,
                            R.drawable.ic_pause_black_24dp);
                    }
            }
        updateNotification();
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
            if (switcher) {
                intent.setAction(PLAY);
                switcher = false;
            }
            else {
                intent.setAction(PAUSE);
                switcher = true;
            }
            PendingIntent pending = PendingIntent.getService(this, 0, intent, 0);
            notificationLayout.setOnClickPendingIntent(R.id.play_pause_btn, pending);

        return customNotification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
