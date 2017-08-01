package mobi.devteam.demofalldetector.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.github.jjobes.slidedaytimepicker.SlideDayTimeListener;
import com.github.jjobes.slidedaytimepicker.SlideDayTimePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.adapter.AlarmAdapter;
import mobi.devteam.demofalldetector.model.MyNotification;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.myInterface.OnRecyclerItemClickListener;
import mobi.devteam.demofalldetector.utils.ReminderType;
import mobi.devteam.demofalldetector.utils.Tools;
import mobi.devteam.demofalldetector.utils.Utils;

public class AddEditReminderActivity extends AppCompatActivity implements IPickResult, OnRecyclerItemClickListener {
    public static final String TAG = "ReminderActivity";
    public static final String EXTRA_IS_ADD_MODE = "is_add_mode";
    public static final String EXTRA_REMINDER_DATA = "reminder_data";
    @BindView(R.id.imgThumb)
    ImageView imgThumb;
    @BindView(R.id.edtReminder)
    TextView edtReminder;
    @BindView(R.id.txtStart)
    TextView txtStart;
    @BindView(R.id.txtEnd)
    TextView txtEnd;
    @BindView(R.id.spinReminderRepeat)
    Spinner spinReminderRepeat;
    @BindView(R.id.edtNote)
    EditText edtNote;
    @BindView(R.id.txtTime)
    TextView txtTime;

    @BindView(R.id.btnAddReminder)
    FloatingActionButton btnAddReminder;
    @BindView(R.id.scrollView)
    NestedScrollView nestedScrollView;

    @BindView(R.id.btnAddAlarm)
    ActionProcessButton btnAddAlarm;

    @BindView(R.id.rcv_alarms)
    RecyclerView rcv_alarms;

    FirebaseAuth mAuth;
    DatabaseReference reminder_data;
    ArrayList<MyNotification> myNotificationArrayList;
    private boolean is_add_mode = true;
    private Reminder reminder;
    private Calendar now;
    private Calendar start;
    private Calendar end;
    private Calendar alarm;
    private StorageReference mStorageRef;
    private boolean isImageChanged = false;

    private AlarmAdapter alarmAdapter;
    private int mLong_click_selected = -1;
    private int old_spiner_position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_reminder);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(R.string.reminder_list);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    btnAddReminder.hide();
                } else {
                    btnAddReminder.show();
                }
            }
        });

        if (getIntent().hasExtra(EXTRA_IS_ADD_MODE)) {
            Intent intent = getIntent();

            is_add_mode = intent.getBooleanExtra(EXTRA_IS_ADD_MODE, true);
            reminder = intent.getParcelableExtra(EXTRA_REMINDER_DATA);
        } else {
            Log.e("REQUIRED_INTENT_EXTRA", EXTRA_IS_ADD_MODE);
            finish();
        }

        initData();

    }

    private void initData() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reminder_data = database.getReference("reminders");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        spinReminderRepeat.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layout,
                MyApplication.reminder_types));
        spinReminderRepeat.setSelection(0);

        now = Calendar.getInstance();
        start = Calendar.getInstance();
        end = Calendar.getInstance();
        alarm = Calendar.getInstance();

        myNotificationArrayList = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(this, myNotificationArrayList, this, ReminderType.TYPE_DAILY);
        rcv_alarms.setOnCreateContextMenuListener(this);
        rcv_alarms.setAdapter(alarmAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rcv_alarms.setLayoutManager(linearLayoutManager);

        if (!is_add_mode) {

            if (reminder.getThumb() != null)
                Picasso.with(this)
                        .load(reminder.getThumb())
                        .resize(300, 300)
                        .into(imgThumb);

            start.setTimeInMillis(reminder.getStart());
            end.setTimeInMillis(reminder.getEnd());

            edtReminder.setText(reminder.getName());
            edtNote.setText(reminder.getNote());
            txtStart.setText(Utils.get_calendar_date(start));
            txtEnd.setText(Utils.get_calendar_date(end));

            txtTime.setText(Utils.get_calendar_time(alarm));

            spinReminderRepeat.setSelection(reminder.getRepeat_type());

            btnAddReminder.setImageResource(R.drawable.ic_update);

            alarmAdapter.setAlarmType(reminder.getRepeat_type());

            if (reminder.getAlarms() != null)
                myNotificationArrayList.addAll(reminder.getAlarms());

            alarmAdapter.notifyDataSetChanged();

            spinReminderRepeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    for (MyNotification n : myNotificationArrayList) {
                        if (checkDuplicateReminder(n)) {
                            spinReminderRepeat.setSelection(old_spiner_position);
                            return;
                        }
                    }

                    old_spiner_position = position;

                    alarmAdapter.setAlarmType(get_selected_reminder());
                    alarmAdapter.notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } else {
            txtStart.setText(Utils.get_calendar_date(now));
            txtEnd.setText(Utils.get_calendar_date(now));
            txtTime.setText(Utils.get_calendar_time(now));

            btnAddReminder.setImageResource(R.drawable.ic_check);
        }
    }

    @OnClick(R.id.imgThumb)
    void pickImage() {
        PickImageDialog.build(new PickSetup()).show(this);
    }

    @OnClick(R.id.txtStart)
    void pickStartDate() {
        DatePickerDialog dialog =
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        start.set(year, month, dayOfMonth);
                        txtStart.setText(Utils.get_calendar_date(start));
                        if (start.compareTo(end) > 0) {
                            end = start;
                            txtEnd.setText(Utils.get_calendar_date(end));
                        }
                    }
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        dialog.getDatePicker().setMinDate(now.getTimeInMillis());
        dialog.show();
    }

    @OnClick(R.id.txtEnd)
    void pickEndDate() {
        DatePickerDialog dialog =
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        end.set(year, month, dayOfMonth);
                        txtEnd.setText(Utils.get_calendar_date(end));
                    }
                }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DATE));
        dialog.getDatePicker().setMinDate(start.getTimeInMillis());
        dialog.show();
    }

    /**
     * Type : daily -> get hour
     * Type: week -> get dow & hour
     */

    @OnClick(R.id.btnAddAlarm)
    void addAlarmOnClick() {
        pickTime(false);
    }

    private void pickTime(final boolean isEdit) {
        if (isEdit && mLong_click_selected != -1)
            alarm.setTimeInMillis(myNotificationArrayList.get(mLong_click_selected).getHourAlarm());
        else
            alarm.setTimeInMillis(System.currentTimeMillis());

        int selected_reminder = get_selected_reminder();
        if (selected_reminder == ReminderType.TYPE_DAILY || selected_reminder == ReminderType.TYPE_NEVER) {
            final TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if (!view.isShown())
                        return;

                    alarm.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    alarm.set(Calendar.MINUTE, minute);

                    MyNotification myNotification = new MyNotification();
                    myNotification.setHourAlarm(alarm.getTimeInMillis());
                    myNotification.setPendingId(Utils.getRandomPendingId());
                    myNotification.setEnable(true);

                    if (checkDuplicateReminder(myNotification)) {
                        return;
                    }

                    if (isEdit) {
                        myNotificationArrayList.remove(mLong_click_selected);
                        mLong_click_selected = -1;
                    }
                    myNotificationArrayList.add(myNotification);
                    sortTimeArray();
                    alarmAdapter.notifyDataSetChanged();

                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        } else {
            new SlideDayTimePicker.Builder(getSupportFragmentManager())
                    .setListener(new SlideDayTimeListener() {
                        @Override
                        public void onDayTimeSet(int day, int hour, int minute) {
                            alarm.set(Calendar.DAY_OF_WEEK, day);//ngay doan nay tui set dow vao
                            alarm.set(Calendar.HOUR_OF_DAY, hour);
                            alarm.set(Calendar.MINUTE, minute);
                            String[] dayOfWeek = getResources().getStringArray(R.array.days_array);
                            txtTime.setText(dayOfWeek[day - 1] + ", " + hour + ":" + minute);

                            MyNotification myNotification = new MyNotification();
                            myNotification.setHourAlarm(alarm.getTimeInMillis());
                            myNotification.setPendingId(Utils.getRandomPendingId());
                            myNotification.setEnable(true);

                            if (checkDuplicateReminder(myNotification)) {
                                return;
                            }

                            if (isEdit) {
                                myNotificationArrayList.remove(mLong_click_selected);
                                mLong_click_selected = -1;
                            }

                            myNotificationArrayList.add(myNotification);
                            sortTimeArray();
                            alarmAdapter.notifyDataSetChanged();
                        }
                    })
                    .setInitialDay(alarm.get(Calendar.DAY_OF_WEEK))
                    .setInitialHour(alarm.get(Calendar.HOUR_OF_DAY))
                    .setInitialMinute(alarm.get(Calendar.MINUTE))
                    .setIs24HourTime(true)
                    .build()
                    .show();
        }
    }

    private boolean checkDuplicateReminder(MyNotification n) {
        Calendar c1 = n.getReminderCalendarRelateCurrent(get_selected_reminder());
        for (MyNotification myNotification : myNotificationArrayList) {
            if (n.getPendingId() == myNotification.getPendingId())
                continue;

            Calendar c2 = myNotification.getReminderCalendarRelateCurrent(get_selected_reminder());
            if (c1.compareTo(c2) == 0) {
                Toast.makeText(this, getString(R.string.duplicate_reminder), Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    private void sortTimeArray() {
        final int selected_reminder = get_selected_reminder();

        Collections.sort(myNotificationArrayList, new Comparator<MyNotification>() {
            @Override
            public int compare(MyNotification o1, MyNotification o2) {
                return Utils.compareReminderToCalendarByType(selected_reminder, o1.getReminderCalendarClean(), o2.getReminderCalendarClean());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add_reminder:
                save_reminder();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btnAddReminder)
    void save_reminder() {
        if (!validate_form()) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(300);
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        final DatabaseReference child = reminder_data.child(currentUser.getUid());
        final StorageReference reminders_images = mStorageRef.child("reminders_images")
                .child(currentUser.getUid());

        long tsLong = System.currentTimeMillis();

        if (reminder == null)
            reminder = new Reminder();


        reminder.setStart(start.getTimeInMillis());
        reminder.setEnd(end.getTimeInMillis()); // start -> end
        reminder.setName(edtReminder.getText().toString());
        reminder.setNote(edtNote.getText().toString());

        // Using for alarm
        reminder.setRepeat_type(get_selected_reminder());
        reminder.setAlarms(myNotificationArrayList);

        if (is_add_mode) {
            reminder.setId(tsLong);
            child.child(tsLong + "").setValue(reminder);
        } else {
            //update
            child.child(reminder.getId() + "").setValue(reminder);
        }
        // Add notification
        Utils.scheduleNotification(this, reminder);
        try {
            if (isImageChanged) {
                Bitmap bitmapAvatar = Tools.convertImageViewToBitmap(imgThumb);
                byte[] bytes = Tools.convertBitmapToByteAray(bitmapAvatar);
                reminders_images.child(reminder.getId() + "").putBytes(bytes)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                @SuppressWarnings("VisibleForTests")
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                child.child(reminder.getId() + "").child("thumb").setValue(downloadUrl.toString());
                            }
                        });
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.toString());
        }
        finish();

    }

    /**
     * Do the form validator here
     *
     * @return boolean
     */
    private boolean validate_form() {
        if (edtReminder.getText().toString().equals("")) {
            ObjectAnimator translationX = ObjectAnimator.ofFloat(edtReminder, "translationX", 0, 50);
            translationX.setDuration(500);
            translationX.setRepeatMode(ValueAnimator.REVERSE);
            translationX.setRepeatCount(3);
            translationX.start();

            Toast.makeText(this, R.string.err_empty_name_reminder, Toast.LENGTH_SHORT).show();
            return false;
        }

//        if (edtNote.getText().toString().equals("")) {
//            ObjectAnimator translationX = ObjectAnimator.ofFloat(edtNote, "translationX", 0, 50);
//            translationX.setDuration(500);
//            translationX.setRepeatMode(ValueAnimator.REVERSE);
//            translationX.setRepeatCount(3);
//            translationX.start();
//
//            Toast.makeText(this, R.string.err_empty_note_reminder, Toast.LENGTH_SHORT).show();
//            return false;
//        }

        if (myNotificationArrayList.size() == 0 && get_selected_reminder() != ReminderType.TYPE_NEVER) {
            //should have at least 1 alarm time
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.5f, 1, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(500);
            scaleAnimation.setRepeatMode(Animation.REVERSE);
            scaleAnimation.setRepeatCount(3);
            Toast.makeText(this, R.string.ae_reminder_require_alarm_time, Toast.LENGTH_SHORT).show();
            btnAddAlarm.startAnimation(scaleAnimation);
            return false;
        }

        return true;
    }

    private int get_selected_reminder() {
        return ReminderType.get_repeat_type(spinReminderRepeat.getSelectedItem().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_reminder_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            Bitmap scaledBitmap = Tools.scaleCenterCrop(r.getBitmap(), 300, 300);
            imgThumb.setImageBitmap(scaledBitmap);
            isImageChanged = true;
        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
            isImageChanged = false;
        }
    }

    @Override
    public void onRecyclerItemClick(int position) {

    }

    @Override
    public void onRecyclerItemLongClick(int position) {
        mLong_click_selected = position;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions_relatives_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mLong_click_selected == -1)
            return true;

        switch (item.getItemId()) {
            case R.id.mnuEdit:
                pickTime(true);
                break;
            case R.id.mnuDelete:
                myNotificationArrayList.remove(mLong_click_selected);
                alarmAdapter.notifyItemRemoved(mLong_click_selected);
                break;
        }
        return true;
    }
}
