package AdapterClasses;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.bike.janus.R;

import java.util.List;

public class PeopleFeedAdapter extends RecyclerView.Adapter<PeopleFeedAdapter.ViewHolder> {

    private List<PeopleFeed> peopleFeeds;

    public PeopleFeedAdapter (List<PeopleFeed> peopleFeeds){
        this.peopleFeeds = peopleFeeds;
    }

    @Override
    public PeopleFeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userProfileFeedView = inflater.inflate(R.layout.item_profile_feed, parent, false);

        ViewHolder viewHolder = new ViewHolder(userProfileFeedView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PeopleFeedAdapter.ViewHolder holder, int position) {
        holder.profilePhotoImageView.setImageResource(peopleFeeds.get(position).photo);
        holder.nameTextView.setText(peopleFeeds.get(position).name);
    }

    @Override
    public int getItemCount() {
        return peopleFeeds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profilePhotoImageView;
        public TextView nameTextView;

        public ViewHolder (View itemView){
            super(itemView);

            profilePhotoImageView = (ImageView) itemView.findViewById(R.id.profilePhotoImageView);
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        }
    }
}
