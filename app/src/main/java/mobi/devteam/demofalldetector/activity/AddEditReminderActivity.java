package mobi.devteam.demofalldetector.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.utils.ReminderType;
import mobi.devteam.demofalldetector.utils.Tools;
import mobi.devteam.demofalldetector.utils.Utils;

public class AddEditReminderActivity extends AppCompatActivity implements IPickResult {
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
    FirebaseAuth mAuth;
    DatabaseReference reminder_data;
    private boolean is_add_mode = true;
    private Reminder reminder;
    private Calendar now;
    private Calendar start;
    private Calendar end;
    private Calendar alarm;
    private StorageReference mStorageRef;
    private boolean isImageChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_reminder);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        spinReminderRepeat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                MyApplication.reminder_types));
        spinReminderRepeat.setSelection(0);

        now = Calendar.getInstance();
        start = Calendar.getInstance();
        end = Calendar.getInstance();
        alarm = Calendar.getInstance();

        if (!is_add_mode) {

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

            alarm.setTimeInMillis(reminder.getHour_alarm());
            txtTime.setText(Utils.get_calendar_time(alarm));

            spinReminderRepeat.setSelection(reminder.getRepeat_type());

        } else {
            txtStart.setText(Utils.get_calendar_date(now));
            txtEnd.setText(Utils.get_calendar_date(now));
            txtTime.setText(Utils.get_calendar_time(now));
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

    @OnClick(R.id.txtTime)
    void pickTime() {
        int selected_reminder = get_selected_reminder();
        if (selected_reminder == ReminderType.TYPE_DAILY) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    alarm.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    alarm.set(Calendar.MINUTE, minute);

                    txtTime.setText(Utils.get_calendar_time(alarm));
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        } else if (selected_reminder == ReminderType.TYPE_WEEKLY) {
            new SlideDayTimePicker.Builder(getSupportFragmentManager())
                    .setListener(new SlideDayTimeListener() {
                        @Override
                        public void onDayTimeSet(int day, int hour, int minute) {
                            alarm.set(Calendar.DAY_OF_WEEK, day);//ngay doan nay tui set dow vao
                            alarm.set(Calendar.HOUR_OF_DAY, hour);
                            alarm.set(Calendar.MINUTE, minute);
                            String[] dayOfWeek = getResources().getStringArray(R.array.days_array);
                            txtTime.setText(dayOfWeek[day - 1] + ", " + hour + ":" + minute);
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

    private void save_reminder() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final DatabaseReference child = reminder_data.child(currentUser.getUid());
        final StorageReference reminders_images = mStorageRef.child("reminders_images")
                .child(currentUser.getUid());

        long tsLong = System.currentTimeMillis();

        if (reminder == null)
            reminder = new Reminder();

        if (edtReminder.getText().toString().equals("")) {
            Toast.makeText(this, R.string.err_empty_name_reminder, Toast.LENGTH_SHORT).show();
        } else {
            reminder.setStart(start.getTimeInMillis());
            reminder.setEnd(end.getTimeInMillis()); // start -> end
            reminder.setName(edtReminder.getText().toString());
            reminder.setNote(edtNote.getText().toString());

            // Using for alarm
            if (is_add_mode)
                reminder.setPendingId(Utils.getRandomPendingId());

            reminder.setHour_alarm(alarm.getTimeInMillis());
            reminder.setRepeat_type(get_selected_reminder());

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
}
