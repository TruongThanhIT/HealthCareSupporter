package mobi.devteam.demofalldetector.adapter;

import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.activity.CreateUpdateRelativeActivity;
import mobi.devteam.demofalldetector.model.Relative;

/**
 * Created by DELL on 5/21/2017.
 */

public class RelativeAdapter extends RecyclerView.Adapter<RelativeAdapter.RelativeHolder> {
    private Context context;
    private RecyclerView mRecyclerView;
    ArrayList<Relative> relatives;

    public RelativeAdapter(Context context, RecyclerView mRecyclerView, ArrayList<Relative> relatives) {
        this.context = context;
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
        Relative relative = relatives.get(position);
        // Displaying text view data
        // Convert bytes data into a Bitmap
        if (relative != null) {
            if (relative.getThumb() == null) {
                Picasso.with(context)
                        .load(R.drawable.image_user_login)
                        .into(holder.imgRelatives);
            } else {
                Picasso.with(context)
                        .load(relative.getThumb())
                        .resize(200, 200)
                        .centerCrop()
                        .placeholder(R.drawable.image_user_login)
                        .into(holder.imgRelatives);
            }

            holder.txtName.setText(relative.getName());
        }
//        Set default relative name
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(ImageView imgRelative, int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, imgRelative);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.actions_relatives_list, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(relatives.get(position)));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private Relative mRelative;

        public MyMenuItemClickListener(Relative r) {
            this.mRelative = r;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            DatabaseReference relative_data = FirebaseDatabase.getInstance().getReference("relatives");
            DatabaseReference child = relative_data.child(currentUser.getUid()).child(mRelative.getId() + "");

            switch (menuItem.getItemId()) {
                case R.id.mnuEdit:
                    Intent intent = new Intent(context, CreateUpdateRelativeActivity.class);
                    intent.putExtra(CreateUpdateRelativeActivity.EXTRA_RELATIVE_DATA,mRelative);
                    intent.putExtra(CreateUpdateRelativeActivity.EXTRA_IS_ADD_MODE,false);
                    Toast.makeText(context, context.getString(R.string.edit), Toast.LENGTH_SHORT).show();
                    return true;

                case R.id.mnuDelete:
                    child.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Toast.makeText(context, context.getString(R.string.delete), Toast.LENGTH_SHORT).show();
                        }
                    });
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
                    showPopupMenu(imgRelatives, getAdapterPosition());
                }
            });
        }
    }
}
