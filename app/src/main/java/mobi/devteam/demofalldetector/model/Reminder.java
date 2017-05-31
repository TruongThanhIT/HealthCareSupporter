package mobi.devteam.demofalldetector.model;

/**
 * Created by Administrator on 5/21/2017.
 */

public class Reminder {

    private long id;
    private String name;
    private long start; //time in ms
    private long end;
    private int repeat_type;
    private String note;
    private String thumb;

    public Reminder() {
    }

    public Reminder(long id, String name, long start, long end, int repeat_type, String note, String thumb) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.repeat_type = repeat_type;
        this.note = note;
        this.thumb = thumb;
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
}

