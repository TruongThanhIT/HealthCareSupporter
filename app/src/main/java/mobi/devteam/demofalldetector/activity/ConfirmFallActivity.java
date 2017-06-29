package mobi.devteam.demofalldetector.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;

public class ConfirmFallActivity extends AppCompatActivity implements OnStateChangeListener {

    @BindView(R.id.imgFall)
    ImageView imgFall;

    @BindView(R.id.swipe_btn)
    SwipeButton swipe_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_fall);
        ButterKnife.bind(this);

        RotateAnimation rotateAnimation = new RotateAnimation(0,-60f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        imgFall.setAnimation(rotateAnimation);

        swipe_btn.setOnStateChangeListener(this);
    }

    @Override
    public void onStateChange(boolean active) {
        if (active){
            //TODO: make a call
        }
    }
}
