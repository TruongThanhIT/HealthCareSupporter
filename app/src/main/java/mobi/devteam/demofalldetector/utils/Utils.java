package mobi.devteam.demofalldetector.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import mobi.devteam.demofalldetector.model.Reminder;

public class Utils {

    public static String get_calendar_time(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String get_calendar_date(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(calendar.getTime());
    }

    public static int getRandomPendingId() { // ko ko Nen de kieu int, vi id trong alam chi chiu kieu int
        int id;
        do {
            id = Tools.getRandomInt();
        } while (checkPendingIdExist(id));
        return id;
    }

    private static boolean checkPendingIdExist(int id) {
        // Checking exist
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference = database.getReference("reminders").child(currentUser.getUid()+"");
        return false;
    }

    public static void scheduleNotification(Activity activity, Reminder reminder) {
        Intent service = new Intent(activity, ReminderService.class);
        service.setAction(Constants.ACTION.START_SERVICE);
        service.putExtra(Constants.KEY.ITEM_KEY, reminder);
        PendingIntent sender = PendingIntent.getService(activity, reminder.getPendingId(), service, 0);
        AlarmManager alarmManager = (AlarmManager) activity.getBaseContext().
                getSystemService(Context.ALARM_SERVICE);
        Calendar rem = Calendar.getInstance();
        rem.set(Calendar.SECOND, 0);
        rem.set(Calendar.MILLISECOND, 0);
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(reminder.getHour_alarm());
        if (reminder.getRepeat_type() == ReminderType.TYPE_DAILY) {

            if (rem.get(Calendar.HOUR_OF_DAY) > temp.get(Calendar.HOUR_OF_DAY)) {
                //Miss that hour , shedule for next day
                rem.add(Calendar.DAY_OF_MONTH, 1);
            }

            rem.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            rem.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
            //Nhan duoc reminder se set tiep reminder
        } else {
            //SHEDULE FOR WEEKLY
            if (rem.get(Calendar.DAY_OF_WEEK) > temp.get(Calendar.DAY_OF_WEEK)){
                //SHEDULE FOR NEXT WEEK
                rem.add(Calendar.DAY_OF_WEEK,1);
            }

            rem.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            rem.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            rem.set(Calendar.SECOND, 0);
            rem.set(Calendar.MILLISECOND, 0);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, sender);
        }

    }
}
