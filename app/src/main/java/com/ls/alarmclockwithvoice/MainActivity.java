package com.ls.alarmclockwithvoice;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ls.alarmclockwithvoice.databinding.ActivityMainBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Declare a constant for the speech request code
    private static final int SPEECH_REQUEST_CODE = 0;
    private ActivityMainBinding binding;
    private AlarmAdapter alarmAdapter;
    private TextView currentTimeTextView;



    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.d("Voice Recognition", "Recognized text: " + spokenText);
            setAlarm(spokenText); // Use this text to set the alarm
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private void setAlarm(String spokenText) {
        try {
            // Attempt to parse the spoken text into a date object
            String adjustedSpokenText = spokenText.replace("p.m", "PM").replace("a.m", "AM");

            SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.US);
            Date date = format.parse(adjustedSpokenText);
            Log.d("Set Alarm", "Parsed Date: " + date);


            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Get the AlarmManager service
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                // Create the Intent and PendingIntent to schedule the alarm
                Intent intent = new Intent(this, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                // Check if the app can schedule exact alarms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    // Prompt the user to grant the permission
                    Toast.makeText(this, "Please grant the permission to schedule exact alarms.", Toast.LENGTH_SHORT).show();
                    // Intent to navigate the user to the app's settings page
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    settingsIntent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(settingsIntent);
                    return;
                }

                // Schedule the alarm
                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                // Handle the case where the date could not be parsed
                Toast.makeText(this, "Could not recognize the time. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            Log.e("Set Alarm", "ParseException: " + e.getMessage());

            // Handle the case where the spoken text is not in expected format
            Toast.makeText(this, "Could not parse the time. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Now you can use binding.addAlarmButton to reference your FloatingActionButton
        binding.addAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeechRecognition(view);
            }
        });
        // Initialize alarm list here, this could be from a database or static for now
        List<Alarm> alarmList = new ArrayList<>();

        // Initialize the adapter with the alarm list
        alarmAdapter = new AlarmAdapter(alarmList);

        // Set the adapter to the RecyclerView
        binding.alarmsRecyclerView.setAdapter(alarmAdapter);

        // Now check for empty alarms
        checkForEmptyAlarms();
        // Initialize the TextView for current time
        currentTimeTextView = findViewById(R.id.noAlarmsText);
        updateCurrentTime();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentTime(); // Update the time when the activity comes into the foreground
    }
    private void checkForEmptyAlarms() {
        if (alarmAdapter.isEmpty()) {
            // No alarms, show the "No alarms" state
            binding.noAlarmsText.setVisibility(View.VISIBLE);
            binding.alarmIcon.setVisibility(View.VISIBLE);
            binding.addAlarmText.setVisibility(View.VISIBLE);
            // Hide RecyclerView as there are no items
            binding.alarmsRecyclerView.setVisibility(View.GONE);
        } else {
            // There are alarms, hide the "No alarms" state
            binding.noAlarmsText.setVisibility(View.GONE);
            binding.alarmIcon.setVisibility(View.GONE);
            binding.addAlarmText.setVisibility(View.GONE);
            // Show RecyclerView
            binding.alarmsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void startSpeechRecognition(View view) {
        displaySpeechRecognizer();
    }

    private void updateCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        // Use ViewBinding to set the current time
        binding.noAlarmsText.setText(getString(R.string.current_time, currentTime));
    }
}