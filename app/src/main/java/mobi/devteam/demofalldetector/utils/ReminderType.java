package mobi.devteam.demofalldetector.utils;

import mobi.devteam.demofalldetector.activity.MyApplication;

public class ReminderType {
    public static int TYPE_NEVER = 0;
    public static int TYPE_DAILY = 1;
    public static int TYPE_WEEKLY = 2;


    public static int get_repeat_type(String text) {
        if (text.equals(MyApplication.reminder_types[0])) {
            return ReminderType.TYPE_NEVER;
        } else if (text.equals(MyApplication.reminder_types[1])) {
            return ReminderType.TYPE_DAILY;
        } else {
            return ReminderType.TYPE_WEEKLY;
        }
    }
}
