package mobi.devteam.demofalldetector.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 5/30/2017.
 */

public class ReminderViewHolder extends RecyclerView.ViewHolder {

    public ReminderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}
