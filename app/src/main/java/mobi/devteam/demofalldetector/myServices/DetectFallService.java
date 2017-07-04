package mobi.devteam.demofalldetector.myServices;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import mobi.devteam.demofalldetector.activity.ConfirmFallActivity;
import mobi.devteam.demofalldetector.model.Accelerator;
import mobi.devteam.demofalldetector.model.FallDetectionStage;
import mobi.devteam.demofalldetector.model.Profile;
import mobi.devteam.demofalldetector.utils.Common;

public class DetectFallService extends RelativeBaseService implements SensorEventListener {

    private static final int LIMIT_SIZE_OF_STATE = 30;
    private static final long TIME_PER_STAGE = 120; //ms
    private static final float ALPHA_CONSTANT = 0.8f;

    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];

    private long lastTimestamp = 0;

    private ArrayList<Accelerator> acceleratorArrayList;
    private ArrayList<Accelerator> recoveryArrayList;

    private SensorManager mSensorManager;
    private Accelerator current_accelerator;
    private boolean waiting_for_recovery = false;

    private double threshold_1 = Common.DEFAULT_THRESHOLD_1; //default
    private double threshold_2 = Common.DEFAULT_THRESHOLD_2; //default
    private double threshold_3 = Common.DEFAULT_THRESHOLD_3; //default

    private DatabaseReference profile_data;

    private int age;
    private double bmi;
    private boolean isMale;
    private Profile mProfile;
    private FallDetectionStage fallDetectionStage;

    @Override
    public void onCreate() {
        super.onCreate();

        acceleratorArrayList = new ArrayList<>();
        recoveryArrayList = new ArrayList<>();

        profile_data = mDatabase.getReference("profile").child(currentUser.getUid());
        profile_data.keepSynced(false);

        profile_data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProfile = dataSnapshot.getValue(Profile.class);
                age = mProfile.getAge();
                bmi = mProfile.getWeight() / Math.sqrt(mProfile.getHeight());
                isMale = mProfile.isMale();
                threshold_1 = mProfile.getThresh1();
                threshold_2 = mProfile.getThresh2();
                threshold_3 = mProfile.getThresh3();

                add_threshold_value();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Log.e("DETECT_FALL_SERVICE", "RUNNING");
    }

    /**
     * The void bellow will compare and add to threshold
     */
    private void add_threshold_value() {
        if (age <= 60) {
            threshold_1 += Common.T1_AGE_LT_60;
            threshold_2 += Common.T2_AGE_LT_60;
            threshold_3 += Common.T3_AGE_LT_60;
        } else {
            threshold_1 += Common.T1_AGE_GT_60;
            threshold_2 += Common.T2_AGE_GT_60;
            threshold_3 += Common.T3_AGE_GT_60;
        }

        if (isMale) {
            threshold_1 += Common.T1_IS_MALE;
            threshold_2 += Common.T2_IS_MALE;
            threshold_3 += Common.T3_IS_MALE;
        } else {
            threshold_1 += Common.T1_IS_FEMALE;
            threshold_2 += Common.T2_IS_FEMALE;
            threshold_3 += Common.T3_IS_FEMALE;
        }

        if (bmi < 18.5f) {
            threshold_1 += Common.T1_BMI_18;
            threshold_2 += Common.T2_BMI_18;
            threshold_3 += Common.T3_BMI_18;
        } else if (bmi < 25f) {

        } else if (bmi < 30) {
            threshold_1 += Common.T1_BMI_25_30;
            threshold_2 += Common.T2_BMI_25_30;
            threshold_3 += Common.T3_BMI_25_30;
        } else {
            threshold_1 += Common.T1_BMI_30;
            threshold_2 += Common.T2_BMI_30;
            threshold_3 += Common.T3_BMI_30;
        }

    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mProfile == null) {
            return;
        }

        if (event.timestamp - lastTimestamp < TIME_PER_STAGE) {
            return;
        }
        lastTimestamp = event.timestamp;

        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        //Bo loc chuyen dong , cang -> 1 thi bo loc cang giam (van dong nhieu)
        // -> 0 bo loc nhay hay (nguoi gia)
        gravity[0] = ALPHA_CONSTANT * gravity[0] + (1 - ALPHA_CONSTANT) * event.values[0]; //x
        gravity[1] = ALPHA_CONSTANT * gravity[1] + (1 - ALPHA_CONSTANT) * event.values[1]; //y
        gravity[2] = ALPHA_CONSTANT * gravity[2] + (1 - ALPHA_CONSTANT) * event.values[2]; //Z

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        current_accelerator = new Accelerator(linear_acceleration);
        acceleratorArrayList.add(current_accelerator);

        if (acceleratorArrayList.size() >= LIMIT_SIZE_OF_STATE) {
            acceleratorArrayList.remove(0);

            if (!waiting_for_recovery) {
                detect_fall();
            } else {
                recoveryArrayList.add(current_accelerator);
                detect_recovery();
            }
        }
    }

    private void detect_fall() {
        double svm_current = calculate_svm(current_accelerator);
        Log.e("WAITING_FALL", svm_current + "");
        if (svm_current >= threshold_1) { //Threshold 1
            if (current_accelerator.getX() > threshold_2 || current_accelerator.getY() > threshold_2 || current_accelerator.getZ() > threshold_2) { //Threshold 2
                //is falling ? saving stage

                fallDetectionStage = new FallDetectionStage();
                fallDetectionStage.setThresh_1(threshold_1);
                fallDetectionStage.setThresh_2(threshold_2);
                fallDetectionStage.setThresh_3(threshold_3);
                fallDetectionStage.setConfirm_ok(false);
                fallDetectionStage.setRecovery(false);
                fallDetectionStage.setTime(Calendar.getInstance().getTimeInMillis());

                waiting_for_recovery = true;
                recoveryArrayList.clear();
            }
        }
    }

    private void detect_recovery() {

        if (recoveryArrayList.size() < LIMIT_SIZE_OF_STATE) { //still waiting for recovery

            //calculate for sum of movement acceleration
            double sum_x = 0;
            double sum_y = 0;
            double sum_z = 0;
            for (Accelerator accelerator : recoveryArrayList) {
                sum_x += Math.abs(accelerator.getX());
                sum_y += Math.abs(accelerator.getY());
                sum_z += Math.abs(accelerator.getZ());
            }
            Log.e("WAITING_RECOVERY", sum_x + " - " + sum_y + " - " + sum_z);

            if (sum_x > threshold_3 || sum_y > threshold_3 || sum_z > threshold_3) { //Threshold 3
                fallDetectionStage.setRecovery(true);
                mDatabase.getReference("fall_detection_logs").child(currentUser.getUid()).setValue(fallDetectionStage);

                fallDetectionStage = null;
                waiting_for_recovery = false; // ok i'm recovery :)),
                recoveryArrayList.clear(); // clear recovery stages
            }

        } else {
            //Didn't get recover after 3s
            Intent dialogIntent = new Intent(this, ConfirmFallActivity.class);
            dialogIntent.putExtra("time", fallDetectionStage.getTime());
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);

            fallDetectionStage = null;
        }

    }


    private static double calculate_svm(Accelerator accelerator) {
        double x = accelerator.getX();
        double y = accelerator.getY();
        double z = accelerator.getZ();

        int i_x = x > 0 ? 1 : -1;
        int i_y = y > 0 ? 1 : -1;
        int i_z = z > 0 ? 1 : -1;

        try {
            return Math.sqrt(i_x * (x * x) + i_y * (y * y) + i_z * (z * z));
        } catch (Exception ignored) {
            return 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.e("onstartcommand", "onstart");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
