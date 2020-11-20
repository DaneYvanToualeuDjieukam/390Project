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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.project_v1.R;
import com.example.project_v1.modules.MainActivity;

public class MyService extends Service {

    private static final String CHANNEL_ID = "MyNotificationChannel";


    @Override
    public void onCreate() {
        super.onCreate();}





    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

try{
    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

   /* Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Fire Detection System")
            .setContentText("RUNNING")
            .set
            .setContentIntent()*/

}
catch (Exception e){
    e.printStackTrace();
}



        return START_STICKY;
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    private void notification(){


        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on

        if (!isScreenOn) {

            if (!isScreenOn) {
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
                wl.acquire(3000); //set your time in milliseconds
            }



            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);

                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")

                    .setContentTitle("Fire Detection System")
                    .setContentText("FDS has detected a FIRE or SMOKE!")
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            managerCompat.notify(999,builder.build());


        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")

                .setContentTitle("Fire Detection System")
                .setContentText("FDS has detected a FIRE or SMOKE!")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999,builder.build());

    }

}
