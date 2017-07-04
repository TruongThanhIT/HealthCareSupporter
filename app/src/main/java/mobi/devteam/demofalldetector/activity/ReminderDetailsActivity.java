package mobi.devteam.demofalldetector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.utils.Constants;
import mobi.devteam.demofalldetector.utils.Utils;

public class ReminderDetailsActivity extends AppCompatActivity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_reminder);
        ButterKnife.bind(this);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        reminder = intent.getParcelableExtra(Constants.KEY.ITEM_KEY);
        onControls();
    }

    private void onControls() {
        if(reminder == null)
            return;

        edtReminder.setText(reminder.getName());
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
        edtNote.setText(reminder.getNote());
        Picasso.with(this)
                .load(reminder.getThumb())
                .resize(300, 300)
                .into(imgThumb);

    }
}
