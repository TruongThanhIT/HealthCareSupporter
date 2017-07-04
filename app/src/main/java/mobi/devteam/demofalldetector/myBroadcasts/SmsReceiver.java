package mobi.devteam.demofalldetector.myBroadcasts;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;

import static android.content.Context.LOCATION_SERVICE;

public class SmsReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<Relative> relatives = new ArrayList<>();
    private Intent myIntent;
    private Context context;
    private LocationManager locationManager;
    private String strMsgSrc;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (FirebaseApp.getInstance() == null) {
                FirebaseApp.initializeApp(context);
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.context = context;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null)
            return;

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        DatabaseReference relative_data = FirebaseDatabase.getInstance().getReference("relatives");

        Log.e("test_sms", "received");
        myIntent = intent;

        relative_data.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                GenericTypeIndicator<HashMap<String, Relative>> t = new GenericTypeIndicator<HashMap<String, Relative>>() {
                };
                HashMap<String, Relative> value = dataSnapshot.getValue(t);

                relatives.clear();
                if (value != null) {
                    relatives.addAll(value.values());
                }

                check_auth();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void check_auth() {
        Bundle extras = myIntent.getExtras();

        String strMessage = "";

        if (extras != null) {
            Object[] smsextras = (Object[]) extras.get("pdus");

            assert smsextras != null;
            for (int i = 0; i < smsextras.length; i++) {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                String strMsgBody = smsmsg.getMessageBody();
                strMsgSrc = smsmsg.getOriginatingAddress();

                strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;

                Log.e(TAG, strMessage);

                boolean flag = false;
                for (Relative relative : relatives) {
                    if (strMsgSrc.contains(relative.getPhone().substring(1))) {
                        flag = true;
                        break;
                    }
                }

                if (flag) {
                    Log.e("phone_number_auth", "auth");
                    try {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.e("PERMISSION_NOT_GRANT", "COARSE");
                            return;
                        }

                        boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                        if (network_enabled) {
                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            send_sms_with_location(location);
                        } else {
                            enable_wifi_and_get_location();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void enable_wifi_and_get_location() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        new Handler().postDelayed(new TimerTask() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.e("PERMISSION_NOT_GRANT", "COARSE");
                        return;
                    }
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    send_sms_with_location(location);
                } else {
                    //TODO: NO NETWORK
                }
            }
        }, 10000);
    }

    private void send_sms_with_location(Location loc) {
        //

        SmsManager sms = SmsManager.getDefault();
        if (loc == null) {
            sms.sendTextMessage(strMsgSrc, null, context.getString(R.string.location_not_found), null, null);
        } else {
            long time_span = System.currentTimeMillis() - loc.getTime();
            long minutes = (time_span / 1000) / 60;
            float circle_radius = loc.getAccuracy() > 0 ? loc.getAccuracy() / 1000 : 0.1f;
            String msg = "Last update: " + minutes + " minutes ago . See map at " + " https://www.doogal.co.uk/Circles.php?lat=" + loc.getLatitude() + "&lng=" + loc.getLongitude() + "&dist=" + circle_radius + "&units=kilometres";
            sms.sendTextMessage(strMsgSrc, null, msg, null, null);
        }
    }


    private void setMobileDataEnabled(boolean enabled) throws Exception {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
        final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}