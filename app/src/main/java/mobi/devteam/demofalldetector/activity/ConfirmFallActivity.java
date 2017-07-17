package mobi.devteam.demofalldetector.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Date;
import java.util.HashMap;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.myServices.DetectFallService;
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


    @BindView(R.id.imgBackground)
    ImageView imgBackground;

    private ArrayList<Relative> relativeArrayList;
    private int current_call_position;
    private long time_key;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mDatabase;
    private Vibrator vibrator;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private Location mLocation;
    private TimerTask task_wait_for_timeout;
    private Handler handler;
    private MediaPlayer mMediaPlayer;
    private TimerTask task_detect_handoff_call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_confirm_fall);
        ButterKnife.bind(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLocation = locationResult.getLastLocation();

                //mFusedLocationClient.removeLocationUpdates(this);
            }
        };

        request_the_location();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        time_key = getIntent().getLongExtra("time", System.currentTimeMillis());

        RotateAnimation rotateAnimation = new RotateAnimation(0, -60f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        imgFall.setAnimation(rotateAnimation);

        swipe_btn.setOnStateChangeListener(this);

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

        mMediaPlayer = MediaPlayer.create(this, R.raw.fall_alarm);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        handle_for_timeout();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        Picasso.with(this)
                .load(R.drawable.bg_confirm)
                .resize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                .into(imgBackground);
    }

    private void request_the_location() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("PERMISSION_NOT_GRANT", "COARSE");
                return;
            }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    sms.sendTextMessage(strNumber, null, getString(R.string.location_denied), null, null);
                } else {
                    Log.e("SEND_SMS_PERMISSION", "NOT GRANT");
                }
                return;
            }

        }

        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + strNumber));

        //
        if (loc == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    sendIntent.putExtra("sms_body", getString(R.string.location_not_found));
                    startActivity(sendIntent);
                    return;
                }
            }
            sms.sendTextMessage(strNumber, null, getString(R.string.location_not_found), null, null);

        } else {
            long time_span = System.currentTimeMillis() - loc.getTime();
            long minutes = (time_span / 1000) / 60;

            String msg = "Last update: " + minutes + " minutes ago . See map at " + " http://mrga2411.ddns.net/do_an.php?lat=" + loc.getLatitude() + "&lng=" + loc.getLongitude() + "&radius=" + loc.getAccuracy();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    sendIntent.putExtra("sms_body", msg);
                    startActivity(sendIntent);
                    return;
                }
            }
            sms.sendTextMessage(strNumber, null, msg, null, null);
        }
    }

    private void user_authed_request_location() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
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
                    //this will update the mLocation
                    user_authed_request_location();
                }
            }
        }, WAITING_FOR_WIFI_AUTO_CONNECT);

    }

    private void getLastLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                confirm_timeout();
            }
        };
        handler.postDelayed(task_wait_for_timeout, Common.WAITING_FOR_CONFIRM);
    }

    @Override
    public void onStateChange(boolean active) {
        if (active) {
            mMediaPlayer.stop();
            handler.removeCallbacks(task_wait_for_timeout);


            vibrator.cancel();
            //confirm ok
            mDatabase.getReference("fall_detection_logs")
                    .child(mCurrentUser.getUid())
                    .child(time_key + "")
                    .child("confirm_ok")
                    .setValue(true);

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

                    new Handler().postDelayed(new TimerTask() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            hideSwipe.start();
            Intent intent = new Intent(this, DetectFallService.class);
            this.startService(intent);
        }
    }

    private void confirm_timeout() {
        mMediaPlayer.stop();
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
                try {
                    make_a_call_to_list();

                    task_detect_handoff_call = new TimerTask() {
                        @Override
                        public void run() {
                            handler_relative_handoff();
                        }
                    };

                    handler.postDelayed(task_detect_handoff_call, 40000);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }

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

        if (current_call_position > relativeArrayList.size() - 1) {
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

//        send_sms_with_location(relativeArrayList.get(current_call_position).getPhone(), mLocation);

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

    private void handler_relative_handoff() {
        Cursor c = managedQuery(CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.TYPE + " = " + CallLog.Calls.OUTGOING_TYPE,
                null,
                CallLog.Calls.DATE + " ASC");
        int c_number = c.getColumnIndex(CallLog.Calls.NUMBER);
        int c_date = c.getColumnIndex(CallLog.Calls.DATE);
        int c_duration = c.getColumnIndex(CallLog.Calls.DURATION);
        int count = 0;
        while (c.moveToLast() && count < 10) { //get last 10 calls log
            count++;
            try {
                String phNumber = c.getString(c_number);
                Date callDayTime = new Date(Long.valueOf(c.getString(c_date)));
                int callDuration = Integer.parseInt(c.getString(c_duration));

                String current_phonenumber = relativeArrayList.get(current_call_position).getPhone();

                if (phNumber.contains(current_phonenumber)) {
                    if (callDuration == 0) {
                        if (Calendar.getInstance().getTimeInMillis() - callDayTime.getTime() < 60000) {
                            Log.e("NO_ANSWER_FROM", current_phonenumber);

                            current_call_position++;
                            make_a_call_to_list();
                        }
                    } else {
                        //ANSWER THE CALL cancel handler
                        handler.removeCallbacks(task_detect_handoff_call);
                    }
                    break;

                }
            } catch (Exception ignored) {

            }
        }
    }

    private void call_to_number(String tel) {
        Log.e(LOG_TAG, tel);
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel));

        //check for permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //wtf happen ? send intent dial :)
                Intent intent2 = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel, null));
                startActivity(intent2);

                return;
            }
        }

        //permission is granted
        startActivity(intent);

    }

}

