package com.ls.alarmclockwithvoice;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if this is a snooze action
        if ("SNOOZE_ACTION".equals(intent.getAction())) {
            snoozeAlarm(context);
        } else {
// Showing a toast
            Toast.makeText(context, "Alarm!", Toast.LENGTH_LONG).show();

            // Showing a notification
            createNotificationChannel(context);
            showNotification(context);
            // Play the alarm sound
            playAlarmSound(context);        }
    }

    private void snoozeAlarm(Context context) {
        // Snooze time in minutes
        int snoozeMinutes = 5;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_IMMUTABLE);

        // Set the snooze alarm
        long snoozeTimeMillis = System.currentTimeMillis() + snoozeMinutes * 60 * 1000;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent);

        // Optionally, cancel the current notification
    }


    private void showNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Alarm")
                .setContentText("Your alarm is going off!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Add a snooze action button to the notification
        Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
        snoozeIntent.setAction("SNOOZE_ACTION");
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_IMMUTABLE);

        builder.addAction(android.R.drawable.ic_media_pause, "Snooze", snoozePendingIntent);

        // Check for the notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Handle the case where you don't have permission.
                // You might want to log this or handle it according to your app's needs.
                return;
            }
        }

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }


    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Alarm Channel";
        String description = "Channel for Alarm Notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
    private void playAlarmSound(Context context) {
        // Initialize a MediaPlayer instance
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound); // Replace 'alarm_sound' with your audio file name

        // Start playing the sound
        mediaPlayer.start();

        // Optional: Set OnCompletionListener if you want to release the MediaPlayer after the sound is played
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }
}
