package com.ls.alarmclockwithvoice;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Alarm implements Serializable {
    private int id;
    private String time; // For simplicity, using a String to represent time
    private boolean isRepeating;
    private String repeatDays; // A string representing the days to repeat
    private String ringtoneUri; // The Uri string of the ringtone
    private String label;
    private boolean isEnabled;



    // Constructor
    public Alarm(int id, String time, boolean isRepeating, String repeatDays, String ringtoneUri, String label, boolean isEnabled) {
        this.id = id;
        this.time = time;
        this.isRepeating = isRepeating;
        this.repeatDays = repeatDays;
        this.ringtoneUri = ringtoneUri;
        this.label = label;
        this.isEnabled = isEnabled;
    }

    public Alarm() {
        Alarm alarm = new Alarm(1, "08:00 AM", false, "Mon,Tue", "ringtoneUri", "Test Alarm 1", true);
    }

    // Modify the getTime() method to return a long
    public long getTimeInMillis() {
        // Convert the time string to a long value
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.US);
        try {
            Date date = format.parse(this.time); // Assuming 'time' is the time string in your Alarm class
            if (date != null) {
                return date.getTime(); // Return the time in milliseconds
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0; // Return a default value or handle errors as needed
    }


    public String getRepeatMode() {
        // This is where you define the logic to interpret the repeatDays string
        // and return a user-friendly description of the repeat mode.
        if (repeatDays == null || repeatDays.isEmpty()) {
            return "Once only";
        } else {
            // A more complex implementation might parse repeatDays and return a string like "Every day"
            return repeatDays;
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(long timeInMillis) {
        // Convert the long value to a formatted time String if needed
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.US);
        Date date = new Date(timeInMillis);
        this.time = format.format(date);
    }


    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }

    public String getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(String repeatDays) {
        this.repeatDays = repeatDays;
    }

    public String getRingtoneUri() {
        return ringtoneUri;
    }

    public void setRingtoneUri(String ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    // You might want to override toString() for easy printing of the alarm's data
    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", isRepeating=" + isRepeating +
                ", repeatDays='" + repeatDays + '\'' +
                ", ringtoneUri='" + ringtoneUri + '\'' +
                ", label='" + label + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
