package mobi.devteam.demofalldetector.activity;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import mobi.devteam.demofalldetector.R;

/**
 * Created by Administrator on 5/20/2017.
 */

public class MyApplication extends Application {
    public static String[] reminder_types;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        reminder_types = getResources().getStringArray(R.array.repeat_array);

    }
}
