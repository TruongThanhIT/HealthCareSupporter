package mobi.devteam.demofalldetector.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by DELL on 5/21/2017.
 */

public class Relative implements Parcelable {
    private long id;
    private String name;
    private byte[] avatar;
    private String phone;

    public Relative() {
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

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Relative{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    protected Relative(Parcel in) {
        id = in.readLong();
        name = in.readString();
        avatar = in.createByteArray();
        phone = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeByteArray(avatar);
        dest.writeString(phone);
    }
}
