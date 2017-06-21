package mobi.devteam.demofalldetector.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Relative implements Parcelable{
    private long id;
    private String name;
    private String phone;
    private String thumb;

    public Relative() {
    }

    public Relative(long id, String name, String phone, String thumb) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.thumb = thumb;
    }

    protected Relative(Parcel in) {
        id = in.readLong();
        name = in.readString();
        phone = in.readString();
        thumb = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(thumb);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Relative> CREATOR = new Creator<Relative>() {
        @Override
        public Relative createFromParcel(Parcel in) {
            return new Relative(in);
        }

        @Override
        public Relative[] newArray(int size) {
            return new Relative[size];
        }
    };

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
