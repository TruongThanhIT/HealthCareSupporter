package mobi.devteam.demofalldetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private long lastTimestamp = 0;

    EditText editText;

    TextView txtDemo,txtMax;
    double max;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);

        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        txtDemo = (TextView) findViewById(R.id.txtDemo);
        txtMax = (TextView) findViewById(R.id.txtMax);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.timestamp - lastTimestamp < 300){
            return;
        }
        lastTimestamp = event.timestamp;

        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        //Bo loc chuyen dong , cang -> 1 thi bo loc cang giam (van dong nhieu)
        // -> 0 bo loc nhay hay (nguoi gia)
        float alpha = 0;
        try {
            alpha = Float.parseFloat(editText.getText().toString());
        }catch (Exception e){
            alpha = 0;
        }

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]; //x
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]; //y
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]; //Z

//        linear_acceleration[0] = event.values[0] - gravity[0];
//        linear_acceleration[1] = event.values[1] - gravity[1];
//        linear_acceleration[2] = event.values[2] - gravity[2];

        double m = Math.abs(gravity[2]); //Truong hop dien thoai up va ngang
        if (count >= 5) {
            txtDemo.setText(String.format("%.2f", m));

            if (m > max) {
                max = m;
                txtMax.setText(String.format("%.2f", m));
            }
        }else{
            count++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
