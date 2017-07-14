package mobi.devteam.demofalldetector.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.CreateUpdateRelativeActivity;
import mobi.devteam.demofalldetector.adapter.RelativeAdapter;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.myInterface.OnRecyclerItemClickListener;

public class RelativeListFragment extends Fragment implements OnRecyclerItemClickListener {
    @BindView(R.id.rccv_relative_list)
    RecyclerView recyclerViewRelatives;

    @BindView(R.id.progressBarRelative)
    ProgressBar progressBarRelative;

    private RelativeAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private ArrayList<Relative> relatives;

    private FirebaseAuth mAuth;
    private DatabaseReference relative_data;

    public RelativeListFragment() {
        // Required empty public constructor
    }

    public static RelativeListFragment newInstance() {
        RelativeListFragment fragment = new RelativeListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.nav_relatives_list);

//        ((MainActivity) getActivity()).showFab();
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_relative_list, container, false);
        ButterKnife.bind(this, view);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerViewRelatives.setLayoutManager(mLayoutManager);
        recyclerViewRelatives.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        relative_data = FirebaseDatabase.getInstance().getReference("relatives");

        ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Collections.swap(relatives, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {

                }
            }


        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(_ithCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewRelatives);

        initData();

        return view;
    }

    private void initData() {
        relatives = new ArrayList<>();
        //setDummyData();
        mAdapter = new RelativeAdapter(getActivity(), relatives, this);
        recyclerViewRelatives.setAdapter(mAdapter);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        progressBarRelative.setVisibility(View.VISIBLE);
        relative_data.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isAdded())
                    return;

                GenericTypeIndicator<HashMap<String, Relative>> t = new GenericTypeIndicator<HashMap<String, Relative>>() {
                };
                HashMap<String, Relative> value = dataSnapshot.getValue(t);

                relatives.clear();
                if (value != null) {
                    relatives.addAll(value.values());
                }

                mAdapter.notifyDataSetChanged();
                progressBarRelative.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error when getting data : " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressBarRelative.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.fab_add_relative)
    public void add() {
        Intent intent = new Intent(this.getContext(), CreateUpdateRelativeActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRecyclerItemClick(int position) {
        Relative relative = relatives.get(position);
        if (relative != null) {
            Uri call = Uri.parse("tel:" + relative.getPhone());
            Intent surf = new Intent(Intent.ACTION_CALL, call);
            startActivity(surf);
        }
    }

    @Override
    public void onRecyclerItemLongClick(int position) {

    }


}
