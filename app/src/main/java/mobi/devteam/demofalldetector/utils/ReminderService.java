package mobi.devteam.demofalldetector.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.AddEditReminderActivity;
import mobi.devteam.demofalldetector.activity.MainActivity;
import mobi.devteam.demofalldetector.activity.ReminderDetailsActivity;
import mobi.devteam.demofalldetector.fragment.HomeFragment;
import mobi.devteam.demofalldetector.model.Reminder;

public class ReminderService extends Service {
    private static final int TIME_SNOOZE = 10 * 60 * 1000;
    private static Reminder reminder;
    private static NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reminder = intent.getParcelableExtra(Constants.KEY.ITEM_KEY);
        if (reminder == null) {
            Log.e(getClass().getName(),"Reminder is null");
        }
        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(reminder.getEnd());
        Calendar nowDate = Calendar.getInstance();
        if(endDate.after(nowDate))
            return START_NOT_STICKY;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        switch (intent.getAction()) {
            case Constants.ACTION.START_SERVICE:
                showNotificationReminder();
                break;
            case Constants.ACTION.DISMISS_ACTION:
                //Cancel notification with PendingId
                notificationManager.cancel(reminder.getPendingId());
                break;
            case Constants.ACTION.SNOOZE_ACTION:
                // Handling snooze
                setSnooze(reminder);
                notificationManager.cancel(reminder.getPendingId());
                break;
            case Constants.ACTION.STOP_SERVICE:
                stopSelf();
                break;
            default:
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    private void showNotificationReminder() {
        //Content Intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra(Constants.KEY.ITEM_KEY, reminder);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, reminder.getPendingId(),
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Dismiss action intent
        Intent dismissIntent = new Intent(this, ReminderService.class);
        dismissIntent.setAction(Constants.ACTION.DISMISS_ACTION);
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        dismissIntent.putExtra(Constants.KEY.ITEM_KEY, reminder);
        PendingIntent dismissPendingIntent = PendingIntent.getService(this, reminder.getPendingId(),
                dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Snooze action intent
        Intent snoozeIntent = new Intent(this, ReminderService.class);
        snoozeIntent.setAction(Constants.ACTION.SNOOZE_ACTION);
        snoozeIntent.putExtra(Constants.KEY.ITEM_KEY, reminder);
        PendingIntent snoozePendingIntent = PendingIntent.getService(this, reminder.getPendingId(),
                snoozeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Create notification builder
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(reminder.getName())
                .setTicker(reminder.getName())
                .setSubText(Utils.get_calendar_date(Calendar.getInstance()))
                .setContentText(reminder.getNote())
                .setSmallIcon(R.drawable.ic_alarm)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss",
                        dismissPendingIntent)
                .addAction(android.R.drawable.ic_popup_reminder, "Snooze", snoozePendingIntent)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;
        //Show notification with notification manager
        notificationManager.notify(reminder.getPendingId(), notification);
    }

    private void setSnooze(Reminder reminder) {
        Context context = this.getBaseContext();
        Intent service = new Intent(context, ReminderService.class);
        service.setAction(Constants.ACTION.START_SERVICE);
        service.putExtra(Constants.KEY.ITEM_KEY, reminder);
        PendingIntent sender = PendingIntent.getService(context, reminder.getPendingId(), service, 0);
        AlarmManager alarmManager = (AlarmManager) context.
                getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() +
                TIME_SNOOZE, sender);
        Toast.makeText(context, context.getString(R.string.snoozeAlert), Toast.LENGTH_SHORT).show();
    }

}
