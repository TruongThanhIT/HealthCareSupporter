package mobi.devteam.demofalldetector.model;

/**
 * Created by Administrator on 5/21/2017.
 */

public class Reminder {
    public static String FIREBASE_KEY = "reminder";

    private int id;
    private String name;
    private double start; //time in ms
    private double end;
    private int repeat_type;
    private String note;

    public Reminder(int id, String name, double start, double end, int repeat_type, String note) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.repeat_type = repeat_type;
        this.note = note;
    }

    public static String getFirebaseKey() {
        return FIREBASE_KEY;
    }

    public static void setFirebaseKey(String firebaseKey) {
        FIREBASE_KEY = firebaseKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
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
}

