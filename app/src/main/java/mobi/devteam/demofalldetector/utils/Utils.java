package mobi.devteam.demofalldetector.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import mobi.devteam.demofalldetector.model.MyNotification;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.myServices.ReminderService;

public class Utils {

    public static String get_calendar_time(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String get_calendar_dow(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String get_calendar_date(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(calendar.getTime());
    }

    public static int getRandomPendingId() {
        return Tools.getRandomInt();
    }

    public static void scheduleNotification(Activity activity, Reminder reminder) {
        Intent service = new Intent(activity, ReminderService.class);
        service.setAction(Constants.ACTION.START_SERVICE);
        service.putExtra(Constants.KEY.ITEM_KEY, reminder);

//        PendingIntent sender = PendingIntent.getService(activity, 123, service, 0);
//        AlarmManager alarmManager = (AlarmManager) activity.getBaseContext().
//                getSystemService(Context.ALARM_SERVICE);
//        Calendar rem = Calendar.getInstance();
//        Calendar temp = Calendar.getInstance();


        final AlarmManager alarmManager = (AlarmManager) activity.getBaseContext().
                getSystemService(Context.ALARM_SERVICE);

        for (MyNotification myNotification : reminder.getAlarms()) {
            if (!myNotification.isEnable())
                return;

            service.putExtra(Constants.KEY.PENDING_ID, myNotification.getPendingId());
            final PendingIntent sender = PendingIntent.getService(activity, myNotification.getPendingId(), service, 0);
            final Calendar rem = Calendar.getInstance();
            Calendar current = Calendar.getInstance();

            Calendar temp = Calendar.getInstance();
            temp.setTimeInMillis(myNotification.getHourAlarm());

            int dow = temp.get(Calendar.DAY_OF_WEEK);
            int hour = temp.get(Calendar.HOUR_OF_DAY);
            int minute = temp.get(Calendar.MINUTE);

            /**
             * Note:
             * Tren api 21 alarm ko duoc < 1m
             * Cac alarm add co thoi gian < thoi gian hien tai se duoc shedule vao gio alarm tiep theo
             */
            if (reminder.getRepeat_type() == ReminderType.TYPE_DAILY) {

                rem.set(Calendar.HOUR_OF_DAY, hour);
                rem.set(Calendar.MINUTE, minute);
                rem.set(Calendar.SECOND, 0);

                if (hour < current.get(Calendar.HOUR_OF_DAY)
                        || (hour == current.get(Calendar.HOUR_OF_DAY) && minute < current.get(Calendar.MINUTE))
                        ) {
                    rem.add(Calendar.HOUR_OF_DAY, 1);
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
            } else {
                //SHEDULE FOR WEEKLY

                rem.set(Calendar.DAY_OF_WEEK, dow);
                rem.set(Calendar.HOUR_OF_DAY, hour);
                rem.set(Calendar.MINUTE, minute);
                rem.set(Calendar.SECOND, 0);

                if (dow < current.get(Calendar.DAY_OF_WEEK)
                        ||
                        (dow == current.get(Calendar.DAY_OF_WEEK)
                                && hour == current.get(Calendar.HOUR_OF_DAY)
                                && minute < current.get(Calendar.MINUTE))
                        )
                    rem.add(Calendar.DAY_OF_WEEK, 1);

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, sender);
            }
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

        for (MyNotification myNotification : reminder.getAlarms()) {
            PendingIntent sender = PendingIntent.getService(mActivity, myNotification.getPendingId(), service,
                    0);
            AlarmManager alarmManager = (AlarmManager) mActivity.getBaseContext().
                    getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
