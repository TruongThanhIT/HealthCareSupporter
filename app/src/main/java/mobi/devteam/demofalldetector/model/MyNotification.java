package mobi.devteam.demofalldetector.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class MyNotification implements Parcelable {

    public static final Creator<MyNotification> CREATOR = new Creator<MyNotification>() {
        @Override
        public MyNotification createFromParcel(Parcel in) {
            return new MyNotification(in);
        }

        @Override
        public MyNotification[] newArray(int size) {
            return new MyNotification[size];
        }
    };
    private int pendingId;
    private long hourAlarm;
    private boolean enable;

    public MyNotification() {
    }

    protected MyNotification(Parcel in) {
        pendingId = in.readInt();
        hourAlarm = in.readLong();
        enable = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pendingId);
        dest.writeLong(hourAlarm);
        dest.writeByte((byte) (enable ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getPendingId() {
        return pendingId;
    }

    public void setPendingId(int pendingId) {
        this.pendingId = pendingId;
    }

    public long getHourAlarm() {
        return hourAlarm;
    }

    public void setHourAlarm(long hourAlarm) {
        this.hourAlarm = hourAlarm;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * This will return the clean method without relating anything
     *
     * @return
     */
    public Calendar getReminderCalendarClean() {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(hourAlarm);
        return instance;
    }

    /**
     * This method get the parse the reminder relative to the current time
     *
     * @return
     */
    public Calendar getReminderCalendarRelateCurrent() {
        Calendar current = Calendar.getInstance();

        Calendar reminder = Calendar.getInstance();
        reminder.setTimeInMillis(hourAlarm);
        int dow = reminder.get(Calendar.DAY_OF_WEEK);

        reminder.set(Calendar.YEAR, current.get(Calendar.YEAR));
        reminder.set(Calendar.MONTH, current.get(Calendar.MONTH));
        reminder.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));

        if (current.get(Calendar.DAY_OF_WEEK) > dow) {
            reminder.add(Calendar.DAY_OF_MONTH, 7);
        } else {
            int hour = reminder.get(Calendar.HOUR_OF_DAY);
            int minute = reminder.get(Calendar.MINUTE);

            if (dow == current.get(Calendar.DAY_OF_WEEK)) {
                if (hour < current.get(Calendar.HOUR_OF_DAY)
                        || (hour == current.get(Calendar.HOUR_OF_DAY) && minute < current.get(Calendar.MINUTE))) {
                    reminder.add(Calendar.DAY_OF_MONTH, 7);
                } else {
                    reminder.add(Calendar.DAY_OF_MONTH, dow - current.get(Calendar.DAY_OF_WEEK));
                }
            } else {
                reminder.add(Calendar.DAY_OF_MONTH, dow - current.get(Calendar.DAY_OF_WEEK));
            }

        }

        return reminder;
    }
}
