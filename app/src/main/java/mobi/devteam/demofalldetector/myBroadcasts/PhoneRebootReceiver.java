package mobi.devteam.demofalldetector.myBroadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.utils.Utils;

/**
 * Created by Administrator on 7/30/2017.
 */

public class PhoneRebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference reminder_data = database.getReference("reminders");

        //user is not login
        if (mAuth.getCurrentUser() == null)
            return;

        final DatabaseReference child = reminder_data.child(mAuth.getCurrentUser().getUid());
        child.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Reminder>> t = new GenericTypeIndicator<HashMap<String, Reminder>>() {
                };

                HashMap<String, Reminder> value = dataSnapshot.getValue(t);

                assert value != null;
                for (Reminder r : value.values())
                    Utils.scheduleNotification(context, r);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
