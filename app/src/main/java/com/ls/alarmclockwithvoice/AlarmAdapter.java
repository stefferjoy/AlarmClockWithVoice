package com.ls.alarmclockwithvoice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.ls.alarmclockwithvoice.databinding.AlarmItemBinding;
import java.util.List;


public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private static List<Alarm> alarmList;
    private OnAlarmClickListener listener;

    public AlarmAdapter(List<Alarm> alarmList, OnAlarmClickListener listener) {
        this.alarmList = alarmList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate with ViewBinding
        AlarmItemBinding binding = AlarmItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);
        // Bind the data to the views
        holder.binding.alarmTimeTextView.setText(alarm.getTime());
        holder.binding.alarmRepeatTextView.setText(alarm.getRepeatMode());


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
        final AlarmItemBinding binding;

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
}
