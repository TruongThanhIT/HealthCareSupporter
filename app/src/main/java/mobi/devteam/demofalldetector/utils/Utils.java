package mobi.devteam.demofalldetector.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import mobi.devteam.demofalldetector.model.Reminder;

public class Utils {

    public static String get_calendar_time(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String get_calendar_date(Calendar calendar){
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

//        alarmManager.cancel(sender);
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getHour_alarm(), sender);

    }
}
