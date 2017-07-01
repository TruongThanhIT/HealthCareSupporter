package mobi.devteam.demofalldetector.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.AddEditReminderActivity;
import mobi.devteam.demofalldetector.adapter.ReminderAdapter;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.myInterface.OnRecyclerItemClickListener;

public class HomeFragment extends Fragment implements OnRecyclerItemClickListener {

    private final int ADD_REMINDER_REQUEST = 123;

    private View mView;
    private Unbinder bind;
    private FirebaseAuth mAuth;
    private DatabaseReference reminder_data;

    private ArrayList<Reminder> reminderArrayList;
    private ReminderAdapter reminderAdapter;

    @BindView(R.id.rcv_reminders)
    RecyclerView rcv_reminders;

    @BindView(R.id.progressBarReminder)
    ProgressBar progressBarReminder;

    private int mLong_click_selected;

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
        if (getArguments() != null) {

        }
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
        initData();

        return mView;
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
                    handler_empty_list();
                }

                reminderAdapter.notifyDataSetChanged();
                progressBarReminder.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBarReminder.setVisibility(View.GONE);
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
        menu.add(0, 0, 0, "Edit");
        menu.add(0, 1, 1, "Delete");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mLong_click_selected == -1)
            return true;

        Reminder selected_reminder = reminderArrayList.get(mLong_click_selected);

        switch (item.getItemId()) {
            case 0:
                Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
                intent.putExtra(AddEditReminderActivity.EXTRA_IS_ADD_MODE, false);
                intent.putExtra(AddEditReminderActivity.EXTRA_REMINDER_DATA, selected_reminder);
                startActivityForResult(intent, ADD_REMINDER_REQUEST);
                break;
            case 1:
                FirebaseUser currentUser = mAuth.getCurrentUser();
                DatabaseReference child = reminder_data.child(currentUser.getUid());
                DatabaseReference remind = child.child(selected_reminder.getId() + "");
                remind.removeValue();
                break;
        }

        mLong_click_selected = -1;
        return true;
    }


}
