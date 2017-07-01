package mobi.devteam.demofalldetector.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Profile;
import mobi.devteam.demofalldetector.myServices.DetectFallService;
import mobi.devteam.demofalldetector.myServices.GetLocationService;

public class ProfileFragment extends Fragment implements ValueEventListener {

    private View mView;

    @BindView(R.id.sw_fall_detect)
    Switch sw_fall_detect;

    @BindView(R.id.sw_allow_find)
    Switch sw_allow_find;

    @BindView(R.id.edtHeight)
    EditText edtHeight;

    @BindView(R.id.edtWeight)
    EditText edtWeight;

    @BindView(R.id.chk_female)
    CheckBox chk_female;

    @BindView(R.id.chk_male)
    CheckBox chk_male;

    @BindView(R.id.btnUpdate)
    ActionProcessButton btnUpdate;

    private Profile mProfile;
    private DatabaseReference profile_data;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, mView);

        initData();
        addEvents();

        return mView;
    }

    private void addEvents() {
        sw_fall_detect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(getActivity(), DetectFallService.class);
                if (isChecked) {
                    getActivity().startService(intent);
                } else {
                    //cancel service
                    getActivity().stopService(intent);
                }
                updateOnclick();
            }
        });

        sw_allow_find.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(getActivity(), GetLocationService.class);
                if (isChecked) {
                    getActivity().startService(intent);
                } else {
                    //cancel service
                    getActivity().stopService(intent);
                }
                updateOnclick();
            }
        });
    }

    private void initData() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Log.e("Profile", "user is not login");
            return;
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        profile_data = firebaseDatabase.getReference().child("profile").child(currentUser.getUid());

        profile_data.addValueEventListener(this);
    }

    @OnClick(R.id.btnUpdate)
    void updateOnclick() {
        Profile profile = new Profile();
        profile.setAllow_find(sw_allow_find.isChecked());
        profile.setDetect_fall(sw_fall_detect.isChecked());
        profile.setHeight(Double.parseDouble(edtHeight.getText().toString()));
        profile.setWeight(Double.parseDouble(edtWeight.getText().toString()));
        profile.setMale(chk_male.isChecked());

        btnUpdate.setMode(ActionProcessButton.Mode.PROGRESS);
        profile_data.setValue(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                btnUpdate.setMode(ActionProcessButton.Mode.ENDLESS);
            }
        });
    }

    @Override
    public void onDestroy() {
        profile_data.removeEventListener(this);
        super.onDestroy();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        mProfile = dataSnapshot.getValue(Profile.class);

        if (mProfile == null)
            return;

        sw_allow_find.setChecked(mProfile.isAllow_find());
        sw_fall_detect.setChecked(mProfile.isDetect_fall());

        edtHeight.setText(mProfile.getHeight() + "");
        edtWeight.setText(mProfile.getWeight() + "");

        if (mProfile.isMale()) {
            chk_male.setChecked(true);
        } else {
            chk_male.setChecked(false);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
