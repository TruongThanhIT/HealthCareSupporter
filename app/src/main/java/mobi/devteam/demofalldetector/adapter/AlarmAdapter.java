package mobi.devteam.demofalldetector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.MyNotification;
import mobi.devteam.demofalldetector.myInterface.OnRecyclerItemClickListener;
import mobi.devteam.demofalldetector.utils.ReminderType;
import mobi.devteam.demofalldetector.utils.Tools;
import mobi.devteam.demofalldetector.utils.Utils;

/**
 * Created by Administrator on 7/28/2017.
 */

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private Context context;
    private ArrayList<MyNotification> myNotificationArrayList;
    private OnRecyclerItemClickListener listener;
    private int alarmType;
    private boolean hideSwitch = false;

    public AlarmAdapter(Context context, ArrayList<MyNotification> myNotificationArrayList, OnRecyclerItemClickListener listener, int alarmType) {
        this.context = context;
        this.myNotificationArrayList = myNotificationArrayList;
        this.listener = listener;
        this.alarmType = alarmType;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.alarm_item, parent, false);
        return new AlarmViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        MyNotification myNotification = myNotificationArrayList.get(position);
        if (myNotification != null) {
            holder.swStatus.setChecked(myNotification.isEnable());

            Calendar dateTime = Tools.convertLongToCalendar(myNotification.getHourAlarm());

            String strTime = Utils.get_calendar_time(dateTime);

            if (alarmType == ReminderType.TYPE_DAILY || alarmType == ReminderType.TYPE_NEVER) {
                holder.txtTime.setText(strTime);
            } else if (alarmType == ReminderType.TYPE_WEEKLY) {
                holder.txtTime.setText(Utils.get_calendar_dow(dateTime) + ", " + strTime);
            }
        }
    }

    @Override
    public int getItemCount() {
        return myNotificationArrayList.size();
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public void setHideSwitch(boolean hideSwitch) {
        this.hideSwitch = hideSwitch;
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtTime)
        TextView txtTime;

        @BindView(R.id.swStatus)
        Switch swStatus;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            swStatus.setClickable(false);
            swStatus.setSelected(false);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!hideSwitch) {
                        swStatus.setChecked(!swStatus.isChecked());
                        myNotificationArrayList.get(getAdapterPosition())
                                .setEnable(swStatus.isChecked());
                    }

                    if (listener != null) {
                        listener.onRecyclerItemClick(getAdapterPosition());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onRecyclerItemLongClick(getAdapterPosition());
                    }
                    return false;
                }
            });
        }
    }
}
