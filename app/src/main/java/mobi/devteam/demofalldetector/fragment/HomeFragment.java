package mobi.devteam.demofalldetector.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.AddEditReminderActivity;
import mobi.devteam.demofalldetector.model.Reminder;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private final int ADD_REMINDER_REQUEST = 123;

    private View mView;
    private Unbinder bind;
    private DatabaseReference mDatabase;

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

        initData();

        return mView;
    }

    private void initData() {
        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }

    @OnClick(R.id.fab_add) void fab_onclick(){
        Intent intent = new Intent(getActivity(), AddEditReminderActivity.class);
        intent.putExtra(AddEditReminderActivity.EXTRA_IS_ADD_MODE,true);
        startActivityForResult(intent,ADD_REMINDER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_REMINDER_REQUEST && resultCode == RESULT_OK && data != null){
            //TODO: handler added data here
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }
}
