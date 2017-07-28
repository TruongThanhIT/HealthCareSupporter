package mobi.devteam.demofalldetector.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

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
    private int repeat_type;
    private String note;
    private String thumb;
    private ArrayList<MyNotification> alarms;

    public Reminder() {
    }

    protected Reminder(Parcel in) {
        id = in.readLong();
        name = in.readString();
        start = in.readLong();
        end = in.readLong();
        repeat_type = in.readInt();
        note = in.readString();
        thumb = in.readString();
        alarms = in.createTypedArrayList(MyNotification.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeInt(repeat_type);
        dest.writeString(note);
        dest.writeString(thumb);
        dest.writeTypedList(alarms);
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

    public ArrayList<MyNotification> getAlarms() {
        return alarms;
    }

    public void setAlarms(ArrayList<MyNotification> alarms) {
        this.alarms = alarms;
    }
}

