package mobi.devteam.demofalldetector.model;

/**
 * Created by Administrator on 5/21/2017.
 */

public class Reminder {
    private int id;
    private String name;
    private double start; //time in ms
    private double end;
    private RepeatType repeat;
    private String note;

    public Reminder(int id, String name, double start, double end, RepeatType repeat, String note) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.repeat = repeat;
        this.note = note;
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

    public RepeatType getRepeat() {
        return repeat;
    }

    public void setRepeat(RepeatType repeat) {
        this.repeat = repeat;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
