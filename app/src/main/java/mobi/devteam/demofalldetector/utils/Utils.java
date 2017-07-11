package mobi.devteam.demofalldetector.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import mobi.devteam.demofalldetector.model.Reminder;

public class Utils {
    public static int tempPendingId = -1;
    public static boolean flag = false;

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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("reminders").child(currentUser.getUid());
        tempPendingId = id;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    GenericTypeIndicator<HashMap<String, Reminder>> indicator =
                            new GenericTypeIndicator<HashMap<String, Reminder>>();
                    HashMap<String, Reminder> reminderHashMap = dataSnapshot.getValue(indicator);
                    ArrayList<Reminder> reminders = new ArrayList<Reminder>();
                    for (Reminder reminder: reminders) {
                        if(reminder.getPendingId() == tempPendingId){
                            flag = true;
                            continue;
                        }
                    }
                }catch(Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return flag;
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
