package com.ls.alarmclockwithvoice;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.recyclerview.widget.RecyclerView;
import com.ls.alarmclockwithvoice.databinding.AlarmItemBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private static List<Alarm> alarmList;
    private OnAlarmClickListener listener;
    private Context context;

    public AlarmAdapter(List<Alarm> alarmList, OnAlarmClickListener listener, Context context) {
        AlarmAdapter.alarmList = alarmList;
        this.listener = listener;
        this.context = context;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate with ViewBinding
        AlarmItemBinding binding = AlarmItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);
        AlarmItemBinding binding = holder.binding;

        // Bind the data to the views
        holder.binding.alarmTimeTextView.setText(alarm.getTime());
        holder.binding.alarmRepeatTextView.setText(alarm.getRepeatMode());
        holder.binding.switchAlarm.setChecked(alarm.isEnabled());

        holder.binding.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Modify the alarm data
            alarm.setEnabled(isChecked);

            if (isChecked) {
                // Schedule the alarm
                AlarmReceiver.scheduleAlarm(holder.itemView.getContext(), alarm.getId(), alarm.getTimeInMillis());
            } else {
                // Cancel the alarm
                AlarmReceiver.cancelAlarm(holder.itemView.getContext(), alarm.getId());
            }

            // Update the alarm state in SharedPreferences and notify the adapter
            updateAlarmState(alarm, holder.itemView.getContext());
            notifyDataSetChanged();
        });

    }

    // Call this method when you want to check for empty state
    public boolean isEmpty() {
        return alarmList.isEmpty();
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    // ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {

        private AlarmItemBinding binding;




        public ViewHolder(AlarmItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Alarm clickedAlarm = alarmList.get(position);
                        listener.onAlarmClick(clickedAlarm);
                    }
                }
            });
        }
    }
    private void updateAlarmState(Alarm alarm, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AlarmClockWithVoice", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", alarm.getId());
            jsonObject.put("time", alarm.getTime());
            jsonObject.put("isRepeating", alarm.isRepeating());
            jsonObject.put("repeatDays", alarm.getRepeatDays());
            jsonObject.put("ringtoneUri", alarm.getRingtoneUri());
            jsonObject.put("label", alarm.getLabel());
            jsonObject.put("isEnabled", alarm.isEnabled());

            String alarmJson = jsonObject.toString();
            editor.putString("alarm_" + alarm.getId(), alarmJson);
        } catch (JSONException e) {
            e.printStackTrace(); // Handle the exception
        }

        editor.apply();
    }


}
