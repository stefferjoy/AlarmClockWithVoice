package com.ls.alarmclockwithvoice;

import android.app.AlarmManager;
import android.app.AlertDialog;
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

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {


    private static final String CHANNEL_ID = "alarm_channel";
    private static final String ALARM_ID_EXTRA = "alarm_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if this is a snooze action
        if ("SNOOZE_ACTION".equals(intent.getAction())) {
            snoozeAlarm(context);
        } else {
            // Extract alarm details from intent
            int alarmId = intent.getIntExtra(ALARM_ID_EXTRA, -1);
            if (alarmId != -1) {

                // Showing a toast
                Toast.makeText(context, "Alarm!", Toast.LENGTH_LONG).show();

                // Showing a notification
                createNotificationChannel(context);
                showNotification(context);

                // Play the alarm sound
                playAlarmSound(context);
            }
        }
    }
    // Method to schedule an alarm
    public static void scheduleAlarm(Context context, int alarmId, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(ALARM_ID_EXTRA, alarmId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }
    // Method to cancel an alarm
    public static void cancelAlarm(Context context, int alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


    private void snoozeAlarm(Context context) {
        // Snooze time in minutes
        int snoozeMinutes = 5;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_IMMUTABLE);

        // Set the snooze alarm
        long snoozeTimeMillis = System.currentTimeMillis() + snoozeMinutes * 60 * 1000;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent);
            } else {
                // The app doesn't have permission to schedule exact alarms.
                // Here you could show a dialog to the user explaining why the app needs this permission
                // and then direct them to the system settings to grant it.
            }
        } else {
            // For older OS versions or if canScheduleExactAlarms is not a concern, schedule the alarm as usual
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent);
            }
        }

        // Optionally, cancel the current notification
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void showPermissionRequestDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Permission Required")
                .setMessage("This app requires permission to schedule alarms.")
                .setPositiveButton("Grant Permission", (dialog, which) -> {
                    // Intent to open the system settings for exact alarms
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    context.startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);

        // Start playing the sound
        mediaPlayer.start();

        // Optional: Set OnCompletionListener if you want to release the MediaPlayer after the sound is played
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

}
