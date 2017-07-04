package mobi.devteam.demofalldetector.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Reminder implements Parcelable {

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };
    private long id;
    private String name;
    private long start; //time in ms
    private long end;
    private long hour_alarm;
    private int repeat_type;
    private String note;
    private String thumb;
    private int pendingId;

    public Reminder() {
    }

    public Reminder(long id, String name, long start, long end, long hour_alarm, int repeat_type, String note, String thumb, int pendingId) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.hour_alarm = hour_alarm;
        this.repeat_type = repeat_type;
        this.note = note;
        this.thumb = thumb;
        this.pendingId = pendingId;
    }

    protected Reminder(Parcel in) {
        id = in.readLong();
        name = in.readString();
        start = in.readLong();
        end = in.readLong();
        hour_alarm = in.readLong();
        repeat_type = in.readInt();
        note = in.readString();
        thumb = in.readString();
        pendingId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeLong(hour_alarm);
        dest.writeInt(repeat_type);
        dest.writeString(note);
        dest.writeString(thumb);
        dest.writeInt(pendingId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getHour_alarm() {
        return hour_alarm;
    }

    public void setHour_alarm(long hour_alarm) {
        this.hour_alarm = hour_alarm;
    }

    public int getRepeat_type() {
        return repeat_type;
    }

    public void setRepeat_type(int repeat_type) {
        this.repeat_type = repeat_type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public int getPendingId() {
        return pendingId;
    }

    public void setPendingId(int pendingId) {
        this.pendingId = pendingId;
    }
}

