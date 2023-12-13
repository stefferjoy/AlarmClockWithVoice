package com.ls.alarmclockwithvoice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
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
import java.util.Map;

import android.content.SharedPreferences;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnAlarmClickListener{

    // Declare a constant for the speech request code
    private static final int SPEECH_REQUEST_CODE = 0;
    private ActivityMainBinding binding;
    private AlarmAdapter alarmAdapter;
    private TextView currentTimeTextView;
    private List<Alarm> alarmList; // List to hold Alarm objects




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


        // Initialize the alarm list
        alarmList = new ArrayList<>();
        alarmList.add(new Alarm(1, "08:00 AM", false, "Mon,Tue", "ringtoneUri", "Test Alarm 1", true));
        // Initialize the adapter with the alarm list
        alarmAdapter = new AlarmAdapter(alarmList, this);

        binding.alarmsRecyclerView.setAdapter(alarmAdapter);
        Log.d("Mainact:", "alarmlist:"+alarmList);
        loadAlarms(); // Load saved alarms

        binding.alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Add this line
        binding.alarmsRecyclerView.setAdapter(alarmAdapter);

        // Set the adapter to the RecyclerView
        binding.alarmsRecyclerView.setAdapter(alarmAdapter);

        // Initialize the TextView for current time
        currentTimeTextView = findViewById(R.id.noAlarmsText);
        updateCurrentTime();


        // Now check for empty alarms
        checkForEmptyAlarms();
        alarmAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentTime(); // Update the time when the activity comes into the foreground
    }



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
            String adjustedSpokenText = spokenText.replace("p.m", "PM").replace("a.m", "AM");
            SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.US);
            Date date = format.parse(adjustedSpokenText);

            if (date != null) {
                Calendar setCalendar = Calendar.getInstance();
                Calendar now = Calendar.getInstance();

                setCalendar.setTime(date);
                // Ensure that the calendar is set for today's date
                setCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
                setCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
                setCalendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

                // Check if the set time is before the current time
                if (setCalendar.before(now)) {
                    // Add one day if the time has already passed
                    setCalendar.add(Calendar.DATE, 1);
                }

                // Schedule the alarm
                scheduleAlarm(setCalendar.getTimeInMillis());

                Alarm newAlarm = new Alarm();
                // Set properties of newAlarm based on parsed data
                addAlarm(newAlarm);
                saveAlarm(newAlarm); // Save the alarm

            } else {
                Toast.makeText(this, "Could not recognize the time. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Could not parse the time. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    public void showAlarmDetails(Alarm alarm) {
        // For example, showing a dialog with alarm details
        new AlertDialog.Builder(this)
                .setTitle("Alarm Details")
                .setMessage("Time: " + alarm.getTime() + "\nLabel: " + alarm.getLabel() + "\nRepeats: " + alarm.getRepeatMode())
                .setPositiveButton("OK", null)
                .show();
    }


    private void scheduleAlarm(long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Check for permission to schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            requestExactAlarmPermission();
        } else if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    private void addAlarm(Alarm alarm) {
        alarmList.add(alarm);
        alarmAdapter.notifyDataSetChanged();
        checkForEmptyAlarms();
    }


    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
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

    @Override
    public void onAlarmClick(Alarm alarm) {

        showAlarmDetails(alarm);

    }
    public void saveAlarm(Alarm alarm) {
        try {
            // Create a JSONObject from the Alarm object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", alarm.getId());
            jsonObject.put("time", alarm.getTime());
            jsonObject.put("isRepeating", alarm.isRepeating());
            jsonObject.put("repeatDays", alarm.getRepeatDays());
            jsonObject.put("ringtoneUri", alarm.getRingtoneUri());
            jsonObject.put("label", alarm.getLabel());
            jsonObject.put("isEnabled", alarm.isEnabled());

            // Convert JSONObject to String
            String alarmJson = jsonObject.toString();

            // Save this string to SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("AlarmClockWithVoice", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("alarm_" + alarm.getId(), alarmJson);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load all saved alarms from SharedPreferences
    private void loadAlarms() {
        SharedPreferences sharedPreferences = getSharedPreferences("AlarmClockWithVoice", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                JSONObject jsonObject = new JSONObject(entry.getValue().toString());

                Alarm alarm = new Alarm();
                alarm.setId(jsonObject.getInt("id"));
                alarm.setTime(jsonObject.getString("time"));
                alarm.setRepeating(jsonObject.getBoolean("isRepeating"));
                alarm.setRepeatDays(jsonObject.getString("repeatDays"));
                alarm.setRingtoneUri(jsonObject.getString("ringtoneUri"));
                alarm.setLabel(jsonObject.getString("label"));
                alarm.setEnabled(jsonObject.getBoolean("isEnabled"));


                alarmList.add(alarm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        alarmAdapter.notifyDataSetChanged();
    }


}