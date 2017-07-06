package mobi.devteam.demofalldetector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.utils.Constants;
import mobi.devteam.demofalldetector.utils.Utils;

public class ReminderDetailsActivity extends AppCompatActivity {
    private final int ADD_REMINDER_REQUEST = 123;
    public static final String EXTRA_REMINDER = "extra reminder";
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

    private static Reminder reminder;
    private Calendar start;
    private Calendar end;
    private Calendar remind;
    private FirebaseAuth mAuth;
    private DatabaseReference reminder_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_reminder);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (getIntent().hasExtra(EXTRA_REMINDER)) {
            reminder = intent.getParcelableExtra(EXTRA_REMINDER);
        }
//        else {
//            reminder = intent.getParcelableExtra(Constants.KEY.ITEM_KEY);
//        }
        onInit();
        onControls();
    }

    private void onInit() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        reminder_data = database.getReference("reminders");

    }

    private void onControls() {
        if (reminder == null)
            return;
        edtReminder.setText(reminder.getName());
        edtReminder.setKeyListener(null);
        start = Calendar.getInstance();
        end = Calendar.getInstance();
        remind = Calendar.getInstance();
        start.setTimeInMillis(reminder.getStart());
        end.setTimeInMillis(reminder.getEnd());
        remind.setTimeInMillis(reminder.getHour_alarm());
        txtStart.setText(Utils.get_calendar_date(start));
        txtEnd.setText(Utils.get_calendar_date(end));
        txtTime.setText(Utils.get_calendar_time(remind));
        spinReminderRepeat.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                MyApplication.reminder_types));
        spinReminderRepeat.setSelection(reminder.getRepeat_type());
        spinReminderRepeat.setEnabled(false);
        edtNote.setText(reminder.getNote());
        edtNote.setKeyListener(null);
        Picasso.with(this)
                .load(reminder.getThumb())
                .resize(300, 300)
                .into(imgThumb);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions_relatives_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.mnuEdit:
                Intent intent = new Intent(this, AddEditReminderActivity.class);
                intent.putExtra(AddEditReminderActivity.EXTRA_IS_ADD_MODE, false);
                intent.putExtra(AddEditReminderActivity.EXTRA_REMINDER_DATA, reminder);
                startActivityForResult(intent, ADD_REMINDER_REQUEST);
                finish();
                break;
            case R.id.mnuDelete:
                FirebaseUser currentUser = mAuth.getCurrentUser();
                DatabaseReference child = reminder_data.child(currentUser.getUid());
                DatabaseReference remind = child.child(reminder.getId() + "");
                remind.removeValue();
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
