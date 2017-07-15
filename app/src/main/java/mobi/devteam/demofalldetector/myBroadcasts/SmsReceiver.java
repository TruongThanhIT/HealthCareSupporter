package mobi.devteam.demofalldetector.myBroadcasts;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.utils.Common;
import mobi.devteam.demofalldetector.utils.Utils;

import static mobi.devteam.demofalldetector.utils.Common.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static mobi.devteam.demofalldetector.utils.Common.UPDATE_INTERVAL_IN_MILLISECONDS;
import static mobi.devteam.demofalldetector.utils.Common.WAITING_FOR_WIFI_AUTO_CONNECT;

public class SmsReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<Relative> relatives = new ArrayList<>();
    private Intent myIntent;
    private Context context;

    private String strMsgSrc;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private Location mLocation;

    @Override
    public void onReceive(Context context, Intent intent) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLocation = locationResult.getLastLocation();
                send_sms_with_location(mLocation);

                mFusedLocationClient.removeLocationUpdates(this);
            }
        };

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

                boolean flag2 = false;
                if (strMsgBody.compareToIgnoreCase(Common.SMS_COMMAND_GET_GPS) == 0) {
                    flag2 = true;
                }

                boolean flag3 = false;
                if (strMsgBody.compareToIgnoreCase(Common.SMS_COMMAND_MAX_SOUND) == 0) {
                    flag3 = true;
                }

                if (flag) {//auth
                    if (flag2) {
                        Log.e("phone_number_auth", "auth");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Log.e("PERMISSION_NOT_GRANT", "COARSE");
                                send_sms_permission_deny(); //Close this for save money when testing :))
                                Log.e(TAG, "Sent denied get location sms");
                                return;
                            }
                        }
                        //request the last location
                        getLastLocation();

                        //request update permission
                        if (Utils.isNetworkAvailable(context)) {
                            user_authed_request_location();
                        } else {
                            enable_wifi_and_get_location();
                        }
                    } else if (flag3) {
                        AudioManager audioManager =
                                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                        //max ring volumn
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, streamMaxVolume, AudioManager.FLAG_SHOW_UI);

                        //turn on speaker
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                }
            }

        }

    }

    private void user_authed_request_location() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void enable_wifi_and_get_location() {

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        Log.e("TRYING_CONNECT_WIFI", "waiting");
        new Handler().postDelayed(new TimerTask() {
            @Override
            public void run() {
                if (Utils.isNetworkAvailable(context)) {
                    //update the mLocation, and wait for response
                    user_authed_request_location();
                } else {
                    send_sms_with_location(mLocation);
                }
            }
        }, WAITING_FOR_WIFI_AUTO_CONNECT);

    }

    private void send_sms_with_location(Location loc) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        //
        SmsManager sms = SmsManager.getDefault();
        if (loc == null) {
            sms.sendTextMessage(strMsgSrc, null, context.getString(R.string.location_not_found), null, null);
        } else {
            long time_span = System.currentTimeMillis() - loc.getTime();
            long minutes = (time_span / 1000) / 60;

            String msg = "Last update: " + minutes + " minutes ago . See map at " + " http://mrga2411.ddns.net/do_an.php?lat=" + loc.getLatitude() + "&lng=" + loc.getLongitude() + "&radius=" + loc.getAccuracy() + "&units=kilometres";
            sms.sendTextMessage(strMsgSrc, null, msg, null, null);
        }
    }

    private void send_sms_permission_deny() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(strMsgSrc, null, context.getString(R.string.location_denied), null, null);
    }

    private void getLastLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocation = task.getResult();
                        } else {
                            Log.e(TAG, "Failed to get location. - last location");
                        }
                    }
                });
    }
}