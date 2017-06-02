package mobi.devteam.demofalldetector.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Reminder;

public class AddEditReminderActivity extends AppCompatActivity {

    public static final String EXTRA_IS_ADD_MODE = "is_add_mode";
    public static final String EXTRA_REMINDER_DATA = "reminder_data";
    private boolean is_add_mode = true;
    private Reminder reminder;

    @BindView(R.id.edtReminder) TextView edtReminder;
    @BindView(R.id.txtStart) TextView txtStart;
    @BindView(R.id.txtEnd) TextView txtEnd;
    @BindView(R.id.spinReminderRepeat) Spinner spinReminderRepeat;
    @BindView(R.id.edtNote) EditText edtNote;
    @BindView(R.id.txtTime) TextView txtTime;

    private Calendar now;
    private Calendar start,end;

    FirebaseAuth mAuth;
    DatabaseReference reminder_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_reminder);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(EXTRA_IS_ADD_MODE)){
            Intent intent = getIntent();

            is_add_mode = intent.getBooleanExtra(EXTRA_IS_ADD_MODE,true);
            reminder = intent.getParcelableExtra(EXTRA_REMINDER_DATA);
        }else{
            Log.e("REQUIRED_INTENT_EXTRA",EXTRA_IS_ADD_MODE);
            finish();
        }

        initData();

        now = Calendar.getInstance();
        start = Calendar.getInstance();
        end = Calendar.getInstance();

        txtStart.setText(get_calendar_date(now));
        txtEnd.setText(get_calendar_date(now));


        txtTime.setText(get_calendar_time(now));
    }

    private void initData() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reminder_data = database.getReference("reminders");

        String[] reminderArrayList = getResources().getStringArray(R.array.repeat_array);
        spinReminderRepeat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,reminderArrayList));
        spinReminderRepeat.setSelection(0);

    }

    private String get_calendar_time(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:ss");
        return simpleDateFormat.format(calendar.getTime());
    }

    private String get_calendar_date(Calendar calendar){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(calendar.getTime());
    }

    @OnClick(R.id.txtStart) void pickStartDate(){
        DatePickerDialog dialog =
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        start.set(year,month,dayOfMonth);
                        txtStart.setText(get_calendar_date(start));
                    }
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        dialog.getDatePicker().setMinDate(now.getTimeInMillis());
        dialog.show();

    }

    @OnClick(R.id.txtEnd) void pickEndDate(){
        DatePickerDialog dialog =
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        end.set(year,month,dayOfMonth);
                        txtEnd.setText(get_calendar_date(end));
                    }
                }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DATE));
        dialog.getDatePicker().setMinDate(start.getTimeInMillis());
        dialog.show();

    }

    @OnClick(R.id.txtTime) void pickTime(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                start.set(Calendar.HOUR_OF_DAY,hourOfDay);
                start.set(Calendar.MINUTE,minute);

                txtTime.setText(get_calendar_time(now));
            }
        },now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),true);
        timePickerDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add_reminder:
                FirebaseUser currentUser = mAuth.getCurrentUser();
                DatabaseReference child = reminder_data.child(currentUser.getUid());
                long tsLong = System.currentTimeMillis();

                if (reminder == null)
                    reminder = new Reminder();

                reminder.setStart(start.getTimeInMillis());
                reminder.setEnd(start.getTimeInMillis());
                reminder.setName(edtReminder.getText().toString());
                reminder.setNote(edtNote.getText().toString());

                reminder.setRepeat_type(get_repeat_type(spinReminderRepeat.getSelectedItem().toString()));

                if (is_add_mode){
                    reminder.setId(tsLong);

                    child.child(tsLong+"").setValue(reminder);
                }else{
                    //update
                    child.child(reminder.getId()+"").setValue(reminder);
                }

                finish();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_reminder_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private int get_repeat_type(String text){
        int type = 0;
        switch (text){
            case "Daily" :
                type = 0;
                break;
            case "Weekly" :
                type = 1;
                break;
            case "Monthly" :
                type = 2;
                break;
            case "Yearly" :
                type = 3;
                break;
        }
        return type;
    }
}
