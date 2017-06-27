package mobi.devteam.demofalldetector.utils;

import mobi.devteam.demofalldetector.activity.MyApplication;

public class ReminderType {
    public static int TYPE_DAILY = 0;
    public static int TYPE_WEEKLY = 1;
    public static int TYPE_MONTHLY = 2;
    public static int TYPE_YEARLY = 3;

    public static int get_repeat_type(String text) {
        if (text.equals(MyApplication.reminder_types[0])) {
            return ReminderType.TYPE_DAILY; //Daily
        } else if (text.equals(MyApplication.reminder_types[1])) {
            return ReminderType.TYPE_WEEKLY; //Weekly
        } else if (text.equals(MyApplication.reminder_types[2])) {
            return ReminderType.TYPE_MONTHLY; //Monthly
        } else {
            return ReminderType.TYPE_YEARLY; //Yearly
        }
    }
}
