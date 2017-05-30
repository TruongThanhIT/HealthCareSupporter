package mobi.devteam.demofalldetector.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
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

    }

    private void initData() {
        String[] reminderArrayList = getResources().getStringArray(R.array.repeat_array);
        spinReminderRepeat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,reminderArrayList));
        spinReminderRepeat.setSelection(0);

    }

    @OnClick(R.id.txtStart) void pickStartDate(){
        View inflate = getLayoutInflater().inflate(R.layout.reminder_pick_date_diaglog, null, false);
        ButterKnife.bind(inflate);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddEditReminderActivity.this);
        alertDialog.setView(inflate);
        alertDialog.show();

    }

    @OnClick(R.id.txtEnd) void pickEndDate(){
        View inflate = getLayoutInflater().inflate(R.layout.reminder_pick_date_diaglog, null, false);
        ButterKnife.bind(inflate);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddEditReminderActivity.this);
        alertDialog.setView(inflate);
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
