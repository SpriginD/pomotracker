package com.erman.pomotracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class Logger {

    private static final String CHANNEL_ID = "default_channel";
    private static final int NOTIFICATION_ID = 1;

    private Context context;
    private NotificationManager notificationManager;

    public Logger(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        CharSequence name = "Default Channel";
        String description = "Channel for app notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        // Register the channel with the system
        notificationManager.createNotificationChannel(channel);
    }

    public void logEvent(String eventName) {
        // Log event to console or database
        // For simplicity, we'll print it to console
        System.out.println("Event logged: " + eventName);

        // Show notification
        showNotification(eventName);
    }

    private void showNotification(String eventName) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle("Durum Bildirimi")
                .setContentText(eventName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
