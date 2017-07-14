package mobi.devteam.demofalldetector.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    public static int getRandomPendingId() {
        int id = Tools.getRandomInt();
        return id;
    }

    public static void scheduleNotification(Activity activity, Reminder reminder) {
        Intent service = new Intent(activity, ReminderService.class);
        service.setAction(Constants.ACTION.START_SERVICE);
        service.putExtra(Constants.KEY.ITEM_KEY, reminder);

        PendingIntent sender = PendingIntent.getService(activity, reminder.getPendingId(), service, 0);
        AlarmManager alarmManager = (AlarmManager) activity.getBaseContext().
                getSystemService(Context.ALARM_SERVICE);
        Calendar rem = Calendar.getInstance();
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(reminder.getHour_alarm());
        if (reminder.getRepeat_type() == ReminderType.TYPE_DAILY) {
            if (rem.get(Calendar.HOUR_OF_DAY) > temp.get(Calendar.HOUR_OF_DAY)) {
                //Miss that hour , shedule for next day
                rem.add(Calendar.DAY_OF_MONTH, 0);
            }

            rem.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            rem.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            rem.set(Calendar.SECOND, 30);
            rem.set(Calendar.MILLISECOND, 30);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
            //Nhan duoc reminder se set tiep reminder
        } else  {
            //SHEDULE FOR WEEKLY
            if (rem.get(Calendar.DAY_OF_WEEK) > temp.get(Calendar.DAY_OF_WEEK)) {
                //SHEDULE FOR NEXT WEEK
                rem.add(Calendar.DAY_OF_WEEK, 0);
//                rem.add(Calendar.DAY_OF_MONTH, 0);
            }

            rem.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            rem.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            rem.set(Calendar.SECOND, 30);
            rem.set(Calendar.MILLISECOND, 30);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, sender);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void cancelAlarmWakeUp(Activity mActivity, Reminder reminder) {
        Intent service = new Intent(mActivity, ReminderService.class);
        service.setAction(Constants.ACTION.START_SERVICE);
        service.putExtra(Constants.KEY.ITEM_KEY, reminder);
        PendingIntent sender = PendingIntent.getService(mActivity, reminder.getPendingId(), service,
                0);
        AlarmManager alarmManager = (AlarmManager) mActivity.getBaseContext().
                getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
