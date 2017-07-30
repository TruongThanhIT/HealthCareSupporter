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

    public Calendar getReminderCalendar() {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(hourAlarm);
        return instance;
    }
}
