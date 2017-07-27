package mobi.devteam.demofalldetector.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 7/27/2017.
 */

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

    public MyNotification() {
    }

    public MyNotification(int pendingId, long hourAlarm) {
        this.pendingId = pendingId;
        this.hourAlarm = hourAlarm;
    }

    protected MyNotification(Parcel in) {
        pendingId = in.readInt();
        hourAlarm = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pendingId);
        dest.writeLong(hourAlarm);
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
}
