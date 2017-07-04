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
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_fall);
        ButterKnife.bind(this);

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
        long[] pattern = { 0, 200, 0 }; //0 to start now, 200 to vibrate 200 ms, 0 to sleep for 0 ms.
        vibrator.vibrate(pattern,0);

        Handler handler = new Handler();
        handler.postDelayed(new TimerTask() {
            @Override
            public void run() {
                confirm_timeout();
            }
        }, Common.WAITING_FOR_CONFIRM);
    }

    @Override
    public void onStateChange(boolean active) {
        if (active) {
            vibrator.cancel();
            mDatabase.getReference("fall_detection_logs").child(mCurrentUser.getUid()).child("confirm_ok").setValue(true);

            imgFall.clearAnimation();

            imgFall.setImageResource(R.drawable.smile);

            ScaleAnimation scaleAnimation = new ScaleAnimation(0,0,0,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            scaleAnimation.setDuration(2000);
            scaleAnimation.setRepeatCount(2);
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //TODO: finish
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
                    txtHoldOn.setVisibility(View.VISIBLE);

                    ObjectAnimator.ofFloat(txtHoldOn, "alpha", 0f, 1f).setDuration(1000).start();
                    txtHoldOn.setText(getString(R.string.confirm_you_are_ok));
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

