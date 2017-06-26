package mobi.devteam.demofalldetector.myBroadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
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

import mobi.devteam.demofalldetector.model.Relative;

public class SmsReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    private ArrayList<Relative> relatives = new ArrayList<>();
    private Intent myIntent;
    private Context context;

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

            for (int i = 0; i < smsextras.length; i++) {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                String strMsgBody = smsmsg.getMessageBody().toString();
                String strMsgSrc = smsmsg.getOriginatingAddress();

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
                        //TODO: HANDLER THING HERE
                        //setMobileDataEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

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
}