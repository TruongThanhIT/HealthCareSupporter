package mobi.devteam.demofalldetector.adapter;

import android.app.Activity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.utils.Tools;

/**
 * Created by DELL on 5/21/2017.
 */

public class RelativeAdapter extends RecyclerView.Adapter<RelativeAdapter.RelativeHolder> {
    private Activity mActivity;
    private RecyclerView mRecyclerView;
    ArrayList<Relative> relatives;

    public RelativeAdapter(Activity mActivity, RecyclerView mRecyclerView, ArrayList<Relative> relatives) {
        this.mActivity = mActivity;
        this.mRecyclerView = mRecyclerView;
        this.relatives = relatives;
    }

    @Override
    public RelativeAdapter.RelativeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_relative_list,
                parent, false);
        RelativeHolder holder = new RelativeHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RelativeAdapter.RelativeHolder holder, final int position) {
        final Relative relative = relatives.get(position);
        // Displaying text view data
        // Convert bytes data into a Bitmap
        holder.imgRelatives.setImageBitmap(Tools.convertByteArrayToBitmap(relative.getAvatar()));
        holder.txtName.setText(relative.getName());
//        Set default relative name
    }
    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(ImageView imgRelative,int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mActivity, imgRelative);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.actions_relatives_list, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }
    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.mnuEdit:
                    Toast.makeText(mActivity, mActivity.getString(R.string.edit),Toast.LENGTH_SHORT).show();
                    return true;

                case R.id.mnuDelete:
                    Toast.makeText(mActivity, mActivity.getString(R.string.delete),Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }
    @Override
    public int getItemCount() {
        return relatives.size();
    }

    public class RelativeHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgRelatives)
        ImageView imgRelatives;
        @BindView(R.id.txtRelativesName)
        TextView txtName;
        @BindView(R.id.imgMenu)
        ImageView imgMenu;

        public RelativeHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            imgMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(imgRelatives,getAdapterPosition());
                }
            });
        }
    }
}
