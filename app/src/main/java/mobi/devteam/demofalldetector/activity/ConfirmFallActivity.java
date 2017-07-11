package mobi.devteam.demofalldetector.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.utils.Common;
import mobi.devteam.demofalldetector.utils.Utils;

import static mobi.devteam.demofalldetector.utils.Common.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static mobi.devteam.demofalldetector.utils.Common.UPDATE_INTERVAL_IN_MILLISECONDS;
import static mobi.devteam.demofalldetector.utils.Common.WAITING_FOR_WIFI_AUTO_CONNECT;

public class ConfirmFallActivity extends AppCompatActivity implements OnStateChangeListener {

    private static final String LOG_TAG = "FallActivity";
    @BindView(R.id.imgFall)
    ImageView imgFall;

    @BindView(R.id.swipe_btn)
    SwipeButton swipe_btn;

    @BindView(R.id.txtHoldOn)
    TextView txtHoldOn;

    @BindView(R.id.txtConfirm)
    TextView txtConfirm;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(new CustomPhoneStateListener(getApplicationContext()), PhoneStateListener.LISTEN_CALL_STATE);
        }
    };
    private ArrayList<Relative> relativeArrayList;
    private int current_call_position;
    private double time_key;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mDatabase;
    private Vibrator vibrator;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private Location mLocation;
    private TimerTask task_wait_for_timeout;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_fall);
        ButterKnife.bind(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLocation = locationResult.getLastLocation();

                mFusedLocationClient.removeLocationUpdates(this);
            }
        };

        request_the_location();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        time_key = getIntent().getDoubleExtra("time", Calendar.getInstance().getTimeInMillis());

        RotateAnimation rotateAnimation = new RotateAnimation(0, -60f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        imgFall.setAnimation(rotateAnimation);

        swipe_btn.setOnStateChangeListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");

        registerReceiver(broadcastReceiver, intentFilter);


        final DatabaseReference relative_data = mDatabase.getReference("relatives");

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e(LOG_TAG, "Can't get current user for auth");
            finish();
            return;
        }
        relativeArrayList = new ArrayList<>();
        relative_data.keepSynced(false);

        relative_data.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                GenericTypeIndicator<HashMap<String, Relative>> t = new GenericTypeIndicator<HashMap<String, Relative>>() {
                };
                HashMap<String, Relative> value = dataSnapshot.getValue(t);

                relativeArrayList.clear();
                if (value != null) {
                    relativeArrayList.addAll(value.values());
                }

                //ok , just get one time , sync with firebase
                //relative_data.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 200, 200}; //0 to start now, 200 to vibrate 200 ms, 0 to sleep for 0 ms.
        vibrator.vibrate(pattern, 0);

        handle_for_timeout();
    }

    private void request_the_location() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION_NOT_GRANT", "COARSE");
            return;
        }
        //request the last location
        getLastLocation();

        //request update permission
        if (Utils.isNetworkAvailable(this)) {
            user_authed_request_location();
        } else {
            enable_wifi_and_get_location();
        }
    }

    private void send_sms_with_location(String strNumber, Location loc) {
        //TODO: require send sms
        SmsManager sms = SmsManager.getDefault();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sms.sendTextMessage(strNumber, null, getString(R.string.location_denied), null, null);
            } else {
                Log.e("SEND_SMS_PERMISSION", "NOT GRANT");
            }
            return;
        }

        //
        if (loc == null) {
            sms.sendTextMessage(strNumber, null, getString(R.string.location_not_found), null, null);
        } else {
            long time_span = System.currentTimeMillis() - loc.getTime();
            long minutes = (time_span / 1000) / 60;
            float circle_radius = loc.getAccuracy() > 0 ? loc.getAccuracy() / 1000 : 0.1f;
            String msg = "Last update: " + minutes + " minutes ago . See map at " + " https://www.doogal.co.uk/Circles.php?lat=" + loc.getLatitude() + "&lng=" + loc.getLongitude() + "&dist=" + circle_radius + "&units=kilometres";
            sms.sendTextMessage(strNumber, null, msg, null, null);
        }
    }

    private void user_authed_request_location() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void enable_wifi_and_get_location() {

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        Log.e("TRYING_CONNECT_WIFI", "waiting");
        new Handler().postDelayed(new TimerTask() {
            @Override
            public void run() {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    user_authed_request_location();
                }
            }
        }, WAITING_FOR_WIFI_AUTO_CONNECT);

    }

    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocation = task.getResult();
                        } else {
                            Log.e(ConfirmFallActivity.this.getClass().getName(), "Failed to get location. - last location");
                        }
                    }
                });
    }

    private void handle_for_timeout() {
        txtHoldOn.setText(getString(R.string.confirm_you_are_fall));
        handler = new Handler();
        task_wait_for_timeout = new TimerTask() {
            @Override
            public void run() {
                if (relativeArrayList.size() > 0) {
                    send_sms_with_location(relativeArrayList.get(0).getPhone(), mLocation);
                }
                confirm_timeout();
            }
        };
        handler.postDelayed(task_wait_for_timeout, Common.WAITING_FOR_CONFIRM);
    }

    @Override
    public void onStateChange(boolean active) {
        if (active) {
            handler.removeCallbacks(task_wait_for_timeout);


            vibrator.cancel();
            //confirm ok
            mDatabase.getReference("fall_detection_logs").child(mCurrentUser.getUid()).child("confirm_ok").setValue(true);

            imgFall.clearAnimation();

            imgFall.setImageResource(R.drawable.smile);


            ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1f, 0.5f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(2000);
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            imgFall.startAnimation(scaleAnimation);

            swipe_btn.setClickable(false);
            ObjectAnimator hideSwipe = ObjectAnimator.ofFloat(swipe_btn, "alpha", 1f, 0f).setDuration(500);
            hideSwipe.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    swipe_btn.setVisibility(View.GONE);
                    txtHoldOn.setVisibility(View.GONE);
//                    txtHoldOn.setText(getString(R.string.confirm_you_are_ok));

                    ObjectAnimator.ofFloat(txtConfirm, "alpha", 0f, 1f).setDuration(1000).start();
                    txtConfirm.setText(getString(R.string.confirm_you_are_ok));

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            hideSwipe.start();
        }
    }

    private void confirm_timeout() {

        vibrator.cancel();

        //cancel animation
        imgFall.clearAnimation();

        //turn on speaker
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);

        swipe_btn.setClickable(false);
        ObjectAnimator hideSwipe = ObjectAnimator.ofFloat(swipe_btn, "alpha", 1f, 0f).setDuration(1000);
        hideSwipe.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                swipe_btn.setVisibility(View.GONE);
                txtHoldOn.setVisibility(View.VISIBLE);

                ObjectAnimator.ofFloat(txtHoldOn, "alpha", 0f, 1f).setDuration(2000).start();
                current_call_position = 0;
                make_a_call_to_list();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        hideSwipe.start();
    }

    private void make_a_call_to_list() {
        if (relativeArrayList.size() == 0) {
            Log.e(LOG_TAG, "Relative list is zero");
            return;
        }

        if (current_call_position >= relativeArrayList.size() - 1) {
            imgFall.setImageResource(R.drawable.fall_icon);
            txtHoldOn.setText(getString(R.string.calling_out_of_bound));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(getString(R.string.calling_emergency_service))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            call_to_number("911");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.show();
            return;
        }

        txtHoldOn.setText(getString(R.string.calling_someone, relativeArrayList.get(current_call_position).getName()));

        Relative relative = relativeArrayList.get(current_call_position);

        //TODO: test here
        String first_letter = relative.getName().length() > 0 ? relative.getName().substring(0, 1).toUpperCase() : "R";

        TextDrawable textDrawable = TextDrawable.builder()
                .beginConfig()
                .width(200)
                .height(200)
                .endConfig()
                .buildRound(first_letter, ColorGenerator.MATERIAL.getRandomColor());

        Picasso.with(this)
                .load(relative.getThumb())
                .placeholder(textDrawable)
                .into(imgFall);

        call_to_number(relative.getPhone());
    }

    private void call_to_number(String tel) {
        Log.e(LOG_TAG, tel);
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + tel));

        //check for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //wtf happen ? send intent dial :)
            Intent intent2 = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel, null));
            startActivity(intent2);

            return;
        }

        //permission is granted
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private class CustomPhoneStateListener extends PhoneStateListener {

        //private static final String TAG = "PhoneStateChanged";
        Context context; //Context to make Toast if required

        public CustomPhoneStateListener(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    //when Idle i.e no call
                    Toast.makeText(context, "Phone state Idle", Toast.LENGTH_LONG).show();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.e("off", "off");
                    //TODO: handle offhook then try to get connect with someone else
                    //when Off hook i.e in call
                    //Make intent and start your service here

                    /*
                    current_call_position += 1;
                    make_a_call_to_list();
                    */

                    Toast.makeText(context, "Phone state Off hook", Toast.LENGTH_LONG).show();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.e("Ringing", "Ringing");
                    //when Ringing
                    Toast.makeText(context, "Phone state Ringing", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }
}

