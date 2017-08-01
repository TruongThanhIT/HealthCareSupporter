package mobi.devteam.demofalldetector.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.AddEditReminderActivity;
import mobi.devteam.demofalldetector.activity.MainActivity;
import mobi.devteam.demofalldetector.activity.MyApplication;
import mobi.devteam.demofalldetector.adapter.AlarmAdapter;
import mobi.devteam.demofalldetector.adapter.ReminderAdapter;
import mobi.devteam.demofalldetector.model.Profile;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.myInterface.OnRecyclerItemClickListener;
import mobi.devteam.demofalldetector.myInterface.OnRequestPermissionListener;
import mobi.devteam.demofalldetector.myServices.DetectFallService;
import mobi.devteam.demofalldetector.myServices.GetLocationService;
import mobi.devteam.demofalldetector.utils.AppPermission;
import mobi.devteam.demofalldetector.utils.Utils;

import static android.content.Context.BIND_AUTO_CREATE;

public class HomeFragment extends Fragment implements OnRecyclerItemClickListener, OnRequestPermissionListener {

    private static final int MY_PERMISSIONS_REQUEST = 235;
    private final int ADD_REMINDER_REQUEST = 123;
    @BindView(R.id.rcv_reminders)
    RecyclerView rcv_reminders;
    @BindView(R.id.progressBarReminder)
    ProgressBar progressBarReminder;
    @BindView(R.id.sw_allow_find)
    Switch sw_allow_find;
    @BindView(R.id.sw_fall_detect)
    Switch sw_fall_detect;
    private View mView;
    private Unbinder bind;
    private FirebaseAuth mAuth;
    private DatabaseReference reminder_data;
    private ArrayList<Reminder> reminderArrayList;
    private ReminderAdapter reminderAdapter;
    private int mLong_click_selected;
    private DatabaseReference profile_data;
    private Profile mProfile;
    private String TAG = "HomeFragment";
    private boolean startBindService;

    private DetectFallService m_service;

    private AppPermission appPermission;

    private ServiceConnection m_serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            m_service = ((DetectFallService.MyBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            m_service = null;
        }
    };
    private boolean isWaitingForSettingResult;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        bind = ButterKnife.bind(this, mView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        rcv_reminders.setLayoutManager(linearLayoutManager);
        getActivity().setTitle(R.string.nav_home);
        initData();
        addEvents();

        appPermission = new AppPermission(getActivity(), this);

        return mView;
    }

    private void addEvents() {
        profile_data = FirebaseDatabase.getInstance().getReference().child("profile").child(mAuth.getCurrentUser().getUid());
        profile_data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProfile = dataSnapshot.getValue(Profile.class);

                if (mProfile == null)
                    return;

                if (!isAdded())
                    return;

                sw_fall_detect.setChecked(mProfile.isDetect_fall());
                sw_allow_find.setChecked(mProfile.isAllow_find());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sw_fall_detect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mProfile == null) {
                    goto_profile_page();
                    return;
                }

                mProfile.setDetect_fall(isChecked);
                profile_data.setValue(mProfile);

                if (appPermission.check_permission())
                    start_fall_detect_service();
                else
                    appPermission.request_permission();
            }
        });

        sw_allow_find.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mProfile == null) {
                    goto_profile_page();
                    return;
                }

                mProfile.setAllow_find(isChecked);
                profile_data.setValue(mProfile);

                if (appPermission.check_permission())
                    start_allow_find_location_service();
                else
                    appPermission.request_permission();
            }
        });
    }

    private void start_allow_find_location_service() {
        boolean isChecked = sw_allow_find.isChecked();
        Intent intent = new Intent(getActivity(), GetLocationService.class);
        if (isChecked) {
            getActivity().startService(intent);
        } else {
            //cancel service
            getActivity().stopService(intent);
        }
    }

    private void start_fall_detect_service() {
        boolean isChecked = sw_fall_detect.isChecked();
        Intent intent = new Intent(getActivity(), DetectFallService.class);
        if (isChecked) {
            if (!Utils.isMyServiceRunning(getActivity(), DetectFallService.class)) {
                getActivity().startService(intent);
                getActivity().bindService(intent, m_serviceConnection, BIND_AUTO_CREATE);
                startBindService = true;
            }

        } else {
            try {
                //cancel service
                getActivity().stopService(intent);
                if (startBindService)
                    getActivity().unbindService(m_serviceConnection);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void goto_profile_page() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.home_require_profile_config))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.navItemSelected(R.id.nav_profile);
                    }
                }).setCancelable(false);
        builder.show();

    }

    private void initData() {
        reminderArrayList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(getActivity(), reminderArrayList, this);
        rcv_reminders.setAdapter(reminderAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        reminder_data = database.getReference("reminders");
        rcv_reminders.setOnCreateContextMenuListener(this);

        load_firebase_data();

    }

    private void load_firebase_data() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference child = reminder_data.child(currentUser.getUid());

        progressBarReminder.setVisibility(View.VISIBLE);

        child.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isAdded())
                    return;

                GenericTypeIndicator<HashMap<String, Reminder>> t = new GenericTypeIndicator<HashMap<String, Reminder>>() {
                };

                HashMap<String, Reminder> value = dataSnapshot.getValue(t);

                reminderArrayList.clear();
                if (value != null) {
                    reminderArrayList.addAll(value.values());
                    smartSortReminder();
                    if (reminderArrayList.size() == 0)
                        handler_empty_list();
                }

                reminderAdapter.notifyDataSetChanged();
                progressBarReminder.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void smartSortReminder() {
        Collections.sort(reminderArrayList, new Comparator<Reminder>() {
            @Override
            public int compare(Reminder o1, Reminder o2) {
                Calendar c1 = Utils.getNextCalendarBaseCurrentTime(o1);
                Calendar c2 = Utils.getNextCalendarBaseCurrentTime(o2);
                return c1.compareTo(c2);
            }
        });

    }

    private void handler_empty_list() {
        //TODO: handle empty list
    }

    @OnClick(R.id.fab_add)
    void fab_onclick() {
        Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
        intent.putExtra(AddEditReminderActivity.EXTRA_IS_ADD_MODE, true);
        startActivityForResult(intent, ADD_REMINDER_REQUEST);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
        try {
            if (startBindService)
                getActivity().unbindService(m_serviceConnection);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onRecyclerItemClick(final int position) {
        /*
        Reminder selected_reminder = reminderArrayList.get(position);
        Intent intent = new Intent(getActivity(), ReminderDetailsActivity.class);
        intent.putExtra(ReminderDetailsActivity.EXTRA_REMINDER, selected_reminder);
        startActivityForResult(intent, ADD_REMINDER_REQUEST);
        */

        Reminder reminder = reminderArrayList.get(position);
        if (reminder != null) {
            final View dialog_view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_reminder_detail, null);

            TextView txtTitle = (TextView) dialog_view.findViewById(R.id.txtTitle);
            TextView txtNote = (TextView) dialog_view.findViewById(R.id.txtNote);
            TextView txtType = (TextView) dialog_view.findViewById(R.id.txtType);
            final View btnEdit = dialog_view.findViewById(R.id.btnEdit);

            txtTitle.setText(reminder.getName());
            txtNote.setText(reminder.getNote());
            try {
                txtType.setText(MyApplication.reminder_types[reminder.getRepeat_type()]);
            } catch (Exception ignored) {
            }

            RecyclerView recyclerView = (RecyclerView) dialog_view.findViewById(R.id.rcv_alarms_time);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            AlarmAdapter alarmAdapter = new AlarmAdapter(getActivity(), reminder.getAlarms(), new OnRecyclerItemClickListener() {
                @Override
                public void onRecyclerItemClick(int position) {
                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(300);

                    ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.5f, 1, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation.setDuration(400);
                    scaleAnimation.setRepeatMode(Animation.REVERSE);
                    scaleAnimation.setRepeatCount(3);

                    btnEdit.startAnimation(scaleAnimation);
                }

                @Override
                public void onRecyclerItemLongClick(int position) {

                }
            }, reminder.getRepeat_type());
            alarmAdapter.setHideSwitch(true);
            recyclerView.setAdapter(alarmAdapter);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialog_view);

            final AlertDialog alertDialog = builder.show();

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Reminder selected_reminder = reminderArrayList.get(position);
                    Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
                    intent.putExtra(AddEditReminderActivity.EXTRA_IS_ADD_MODE, false);
                    intent.putExtra(AddEditReminderActivity.EXTRA_REMINDER_DATA, selected_reminder);
                    startActivityForResult(intent, ADD_REMINDER_REQUEST);
                    alertDialog.dismiss();
                }
            });

            if (alertDialog.getWindow() != null)
                alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }

    }

    @Override
    public void onRecyclerItemLongClick(int position) {
        mLong_click_selected = position;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.actions_relatives_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mLong_click_selected == -1)
            return true;

        Reminder selected_reminder = reminderArrayList.get(mLong_click_selected);

        switch (item.getItemId()) {
            case R.id.mnuEdit:
                Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
                intent.putExtra(AddEditReminderActivity.EXTRA_IS_ADD_MODE, false);
                intent.putExtra(AddEditReminderActivity.EXTRA_REMINDER_DATA, selected_reminder);
                startActivityForResult(intent, ADD_REMINDER_REQUEST);
                break;
            case R.id.mnuDelete:
                Utils.cancelAlarmWakeUp(getActivity(), selected_reminder);
                FirebaseUser currentUser = mAuth.getCurrentUser();
                DatabaseReference child = reminder_data.child(currentUser.getUid());
                DatabaseReference remind = child.child(selected_reminder.getId() + "");
                remind.removeValue();
                break;
        }

        mLong_click_selected = -1;
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isWaitingForSettingResult) {
            isWaitingForSettingResult = false;
            start_fall_detect_service();
            start_allow_find_location_service();
            return;
        }
    }

    @Override
    public void showSettingIntent() {
        isWaitingForSettingResult = true;
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void requestPermissions(String[] permissions) {
        requestPermissions(permissions, MY_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST && permissions.length > 0) {
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), getString(R.string.request_permissions_deny), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            start_fall_detect_service();
            start_allow_find_location_service();
        }
    }
}
