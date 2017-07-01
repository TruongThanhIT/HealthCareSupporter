package mobi.devteam.demofalldetector.myServices;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import mobi.devteam.demofalldetector.activity.ConfirmFallActivity;
import mobi.devteam.demofalldetector.model.Accelerator;

/**
 * Created by Administrator on 6/29/2017.
 */

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

    private double threshold_1 = 6f;
    private double threshold_2 = 5f;
    private double threshold_3 = 10f;

    @Override
    public void onCreate() {
        super.onCreate();

        acceleratorArrayList = new ArrayList<>();
        recoveryArrayList = new ArrayList<>();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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

            if (!waiting_for_recovery)
                detect_fall();
            else {
                recoveryArrayList.add(current_accelerator);
                detect_recovery();
            }
        }
    }


    private void detect_fall() {
        double svm_current = calculate_svm(current_accelerator);

        if (svm_current >= threshold_1) { //Threshold 1
            if (current_accelerator.getX() > threshold_2 || current_accelerator.getY() > threshold_2 || current_accelerator.getZ() > threshold_2) { //Threshold 2
                //is falling ? saving stage
                //TODO: save stage
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

            if (sum_x > threshold_3 || sum_y > threshold_3 || sum_z > threshold_3){ //Threshold 3
                waiting_for_recovery = false; // ok i'm recovery :)),
                recoveryArrayList.clear(); // clear recovery stages
            }

        } else {
            //Didn't get recover after 3s
            //TODO: send confirm
            Intent dialogIntent = new Intent(this, ConfirmFallActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
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
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
