package mobi.devteam.demofalldetector.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.AddEditReminderActivity;
import mobi.devteam.demofalldetector.adapter.ReminderAdapter;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.myInterface.OnRecyclerItemClickListener;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements OnRecyclerItemClickListener{

    private final int ADD_REMINDER_REQUEST = 123;

    private View mView;
    private Unbinder bind;
    private FirebaseAuth mAuth;
    private DatabaseReference reminder_data;

    private ArrayList<Reminder> reminderArrayList;
    private ReminderAdapter reminderAdapter;

    @BindView(R.id.rcv_reminders) RecyclerView rcv_reminders;

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
        bind = ButterKnife.bind(this,mView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rcv_reminders.setLayoutManager(linearLayoutManager);

        initData();

        return mView;
    }

    private void initData() {
        reminderArrayList = new ArrayList<>();
        reminderAdapter = new ReminderAdapter(getActivity(),reminderArrayList,this);
        rcv_reminders.setAdapter(reminderAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        reminder_data = database.getReference("reminders");

        load_firebase_data();
    }

    private void load_firebase_data(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference child = reminder_data.child(currentUser.getUid());

        child.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String,Reminder>> t = new GenericTypeIndicator<HashMap<String,Reminder>>() {};

                HashMap<String,Reminder> value = dataSnapshot.getValue(t);
                reminderArrayList.clear();
                reminderArrayList.addAll(value.values());
                reminderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error when getting data : "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.fab_add) void fab_onclick(){
        Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
        intent.putExtra(AddEditReminderActivity.EXTRA_IS_ADD_MODE,true);
        startActivityForResult(intent,ADD_REMINDER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_REMINDER_REQUEST && resultCode == RESULT_OK){
            load_firebase_data();
        }
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

    }
}
