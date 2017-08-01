package mobi.devteam.demofalldetector.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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

    public static void scheduleNotification(Context context, Reminder reminder) {
        Intent service = new Intent(context, ReminderService.class);
        service.setAction(Constants.ACTION.START_SERVICE);
        service.putExtra(Constants.KEY.ITEM_KEY, reminder);

//        PendingIntent sender = PendingIntent.getService(activity, 123, service, 0);
//        AlarmManager alarmManager = (AlarmManager) activity.getBaseContext().
//                getSystemService(Context.ALARM_SERVICE);
//        Calendar rem = Calendar.getInstance();
//        Calendar temp = Calendar.getInstance();


        final AlarmManager alarmManager = (AlarmManager) context.
                getSystemService(Context.ALARM_SERVICE);

        for (MyNotification myNotification : reminder.getAlarms()) {
            if (!myNotification.isEnable())
                continue;

            service.putExtra(Constants.KEY.PENDING_ID, myNotification.getPendingId());
            final PendingIntent sender = PendingIntent.getService(context, myNotification.getPendingId(), service, 0);
            Calendar rem = Calendar.getInstance();
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
                    Log.e("keep_next_day", Utils.get_calendar_time(rem));
                    rem.add(Calendar.DAY_OF_MONTH, 1);
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
            } else {
                //SHEDULE FOR WEEKLY
                rem = myNotification.getReminderCalendarRelateCurrent(reminder.getRepeat_type());

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

    /**
     * This method will compare 2 reminder calendar by type
     * Type Daily just compare hour and minute
     * Type Week will compare dow,hour,minute
     * <p>
     * Don't use this method to compare 2 Calendar , use #Calendar.compare instead
     *
     * @param reminderType {#ReminderType}
     * @param c1
     * @param c2
     * @return
     */
    @Deprecated
    public static int compareReminderToCalendarByType(int reminderType, Calendar c1, Calendar c2) {
        if (reminderType == ReminderType.TYPE_DAILY || reminderType == ReminderType.TYPE_NEVER) {
            if (c1.get(Calendar.HOUR_OF_DAY) < c2.get(Calendar.HOUR_OF_DAY))
                return -1;

            if (c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY)) {
                if (c1.get(Calendar.MINUTE) < c2.get(Calendar.MINUTE))
                    return -1;
                else if (c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE))
                    return 0;
            }

            return 1;
        } else if (reminderType == ReminderType.TYPE_WEEKLY) {
            //compare week
            if (c1.get(Calendar.DAY_OF_WEEK) < c2.get(Calendar.DAY_OF_WEEK)) {
                return -1;
            } else if (c1.get(Calendar.DAY_OF_WEEK) > c2.get(Calendar.DAY_OF_WEEK))
                return 1;
            else {
                //compare hour
                if (c1.get(Calendar.HOUR_OF_DAY) < c2.get(Calendar.HOUR_OF_DAY)) {
                    return -1;
                } else if (c1.get(Calendar.HOUR_OF_DAY) > c2.get(Calendar.HOUR_OF_DAY)) {
                    return 1;
                } else {
                    //compare minute
                    if (c1.get(Calendar.MINUTE) < c2.get(Calendar.MINUTE)) {
                        return -1;
                    } else if (c1.get(Calendar.MINUTE) > c2.get(Calendar.MINUTE)) {
                        return 1;
                    }
                    //all are equal
                    return 0;
                }
            }
        }

        return -1;
    }

    /**
     * This method will get the next calendar which nearest to current time
     *
     * @param reminder
     * @return
     */
    public static Calendar getNextCalendarBaseCurrentTime(Reminder reminder) {
        Calendar dateTime = Calendar.getInstance();
        if (reminder.getAlarms() != null) {
            dateTime = reminder.getAlarms().get(0).getReminderCalendarRelateCurrent(reminder.getRepeat_type());

            for (int i = 1; i < reminder.getAlarms().size(); i++) {
                MyNotification myNotification = reminder.getAlarms().get(i);
                Calendar reminderCalendar = myNotification.getReminderCalendarRelateCurrent(reminder.getRepeat_type());

                if (dateTime.compareTo(reminderCalendar) > 0) {
                    //in this case all item in the array were sorted
                    //this array is already sorted when add
                    dateTime = reminderCalendar;
                }
            }
        }
        return dateTime;
    }
}
