package mobi.devteam.demofalldetector.myServices;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import mobi.devteam.demofalldetector.myBroadcasts.SmsReceiver;

/**
 * Created by Administrator on 6/25/2017.
 */

public class GetLocationService extends RelativeBaseService {

    private SmsReceiver mSmsReceiver;
    private IntentFilter mIntentFilter;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("create_service", "sv");

        //SMS event receiver
        mSmsReceiver = new SmsReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSmsReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the SMS receiver
        unregisterReceiver(mSmsReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
