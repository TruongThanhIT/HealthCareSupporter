package mobi.devteam.demofalldetector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Reminder;
import mobi.devteam.demofalldetector.myInterface.OnRecyclerItemClickListener;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final ColorGenerator generator;
    private Context context;
    private ArrayList<Reminder> reminderArrayList;
    private OnRecyclerItemClickListener mListener;

    public ReminderAdapter(Context context, ArrayList<Reminder> reminderArrayList, OnRecyclerItemClickListener listener) {
        this.context = context;
        this.reminderArrayList = reminderArrayList;
        this.mListener = listener;
        generator = ColorGenerator.MATERIAL;
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_reminder_list, parent, false);
        return new ReminderViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {
        Reminder reminder = reminderArrayList.get(position);
        if (reminder != null) {
            holder.txtReminder.setText(reminder.getName());
            holder.txtRepeat.setText(reminder.getRepeat_type() + "");
            holder.txtTime.setText(reminder.getStart() + "");

            if (reminder.getName().length() > 0) {
                TextDrawable textDrawable = TextDrawable.builder()
                        .beginConfig()
                        .width(100)
                        .height(100)
                        .endConfig()
                        .buildRound(reminder.getName().substring(0, 1).toUpperCase(), generator.getRandomColor());
                if (reminder.getThumb() == null) {
                    holder.imgThumb.setImageDrawable(textDrawable);
                } else {
                    Picasso.with(context)
                            .load(reminder.getThumb())
                            .resize(100, 100)
                            .placeholder(textDrawable)
                            .into(holder.imgThumb);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return reminderArrayList.size();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb)
        CircleImageView imgThumb;
        @BindView(R.id.txtReminder)
        TextView txtReminder;
        @BindView(R.id.txtTime)
        TextView txtTime;
        @BindView(R.id.txtRepeat)
        TextView txtRepeat;

        public ReminderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onRecyclerItemClick(getAdapterPosition());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        mListener.onRecyclerItemLongClick(getAdapterPosition());
                    }

                    return false;
                }
            });
        }
    }

}