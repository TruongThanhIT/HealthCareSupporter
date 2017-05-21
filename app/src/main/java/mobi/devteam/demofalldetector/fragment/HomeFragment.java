package mobi.devteam.demofalldetector.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;

import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.AddEditReminderActivity;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private final int ADD_REMINDER_REQUEST = 123;

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
        return inflater.inflate(R.layout.fragment_home, container, false);
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
}
