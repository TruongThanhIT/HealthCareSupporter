package mobi.devteam.demofalldetector.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.CreateRelativeActivity;
import mobi.devteam.demofalldetector.activity.MainActivity;
import mobi.devteam.demofalldetector.adapter.RelativeAdapter;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.utils.Tools;

public class RelativeListFragment extends Fragment {
    @BindView(R.id.rccv_relative_list)
    RecyclerView recyclerViewRelatives;

    private RelativeAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private ArrayList<Relative> relatives;

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
        getActivity().setTitle(R.string.tittle_manage_relatives);
//        ((MainActivity) getActivity()).showFab();
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_relative_list, container, false);
        ButterKnife.bind(this, view);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerViewRelatives.setLayoutManager(mLayoutManager);
        recyclerViewRelatives.setHasFixedSize(true);
        relatives = new ArrayList<>();
        setDummyData();
        mAdapter = new RelativeAdapter(getActivity(), recyclerViewRelatives, relatives);
        recyclerViewRelatives.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        return view;
    }

    private void setDummyData() {
        Relative relative = new Relative();
        relative.setId(Calendar.getInstance().getTimeInMillis());
        relative.setName("Thái Thanh");
        Bitmap bitmapImg = BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.image_user_login);
        relative.setAvatar(Tools.convertBitmapToByteAray(bitmapImg));
        relative.setPhone("01234567890");

        Relative relative1 = new Relative();
        relative1.setId(Calendar.getInstance().getTimeInMillis());
        relative1.setName("Văn Minh");
        Bitmap bitmapImg1 = BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.image_user_login);
        relative1.setAvatar(Tools.convertBitmapToByteAray(bitmapImg1));
        relative1.setPhone("090909567890");

        relatives.add(relative);
        relatives.add(relative1);
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
    public void add(){
        Intent intent = new Intent(this.getContext(), CreateRelativeActivity.class);
        startActivity(intent);
    }

}
