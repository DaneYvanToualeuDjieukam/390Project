package com.example.project_v1.database;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.project_v1.R;
import com.example.project_v1.modules.MainActivity;

public class MyService extends Service {

    public static final String CHANNEL_ID = "exampleServiceChannel";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Fire Detection System")
                .setContentText("Ensuring You Receive Notifications")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1,notification);

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
