package com.ls.alarmclockwithvoice;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.ls.alarmclockwithvoice.databinding.AlarmItemBinding;
import java.util.List;


public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private List<Alarm> alarmList;

    public AlarmAdapter(List<Alarm> alarmList) {
        this.alarmList = alarmList;
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Store the binding
        final AlarmItemBinding binding;

        public ViewHolder(AlarmItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
