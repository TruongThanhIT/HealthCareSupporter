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

    public static int getRandomPendingId() {
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
        } else if(reminder.getRepeat_type() == ReminderType.TYPE_WEEKLY){
            //SHEDULE FOR WEEKLY
            if (rem.get(Calendar.DAY_OF_WEEK) > temp.get(Calendar.DAY_OF_WEEK)){
                //SHEDULE FOR NEXT WEEK
                rem.add(Calendar.DAY_OF_WEEK,0);
//                rem.add(Calendar.DAY_OF_MONTH, 1);
            }

            rem.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            rem.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            rem.set(Calendar.SECOND, 30);
            rem.set(Calendar.MILLISECOND, 30);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, sender);
        }
        else if(reminder.getRepeat_type() == ReminderType.TYPE_MONTHLY){
            if(rem.get(Calendar.DAY_OF_MONTH) > temp.get(Calendar.DAY_OF_MONTH)){
                rem.add(Calendar.DAY_OF_MONTH, 0);
            }
            rem.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            rem.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            rem.set(Calendar.SECOND, 30);
            rem.set(Calendar.MILLISECOND, 30);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY * getDuration(), sender);
        }
        else{
            if(rem.get(Calendar.DAY_OF_YEAR) > temp.get(Calendar.DAY_OF_YEAR)){
                rem.add(Calendar.DAY_OF_YEAR, 0);
            }
            rem.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
            rem.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
            rem.set(Calendar.SECOND, 30);
            rem.set(Calendar.MILLISECOND, 30);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rem.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 365, sender);
        }


    }
    public static int getDuration(){
        // get todays date
        Calendar cal = Calendar.getInstance();
        // get current month
        int currentMonth = cal.get(Calendar.MONTH);

        // move month ahead
        currentMonth++;
        // check if has not exceeded threshold of december

        if(currentMonth > Calendar.DECEMBER){
            // alright, reset month to jan and forward year by 1 e.g fro 2013 to 2014
            currentMonth = Calendar.JANUARY;
            // Move year ahead as well
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)+1);
        }

        // reset calendar to next month
        cal.set(Calendar.MONTH, currentMonth);
        // get the maximum possible days in this month
        int maximumDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        return maximumDay;
    }
}
