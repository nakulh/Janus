package AdapterClasses;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.bike.janus.R;

import java.util.List;

public class RoutesFeedAdapter extends RecyclerView.Adapter<RoutesFeedAdapter.ViewHolder> {

    private List<RoutesFeed> routesFeed;

    public RoutesFeedAdapter (List<RoutesFeed> routesFeed){
        this.routesFeed = routesFeed;
    }

    @Override
    public RoutesFeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userProfileFeedView = inflater.inflate(R.layout.item_routes_feed, parent, false);

        ViewHolder viewHolder = new ViewHolder(userProfileFeedView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RoutesFeedAdapter.ViewHolder holder, int position) {
        holder.startLocationTextView.setText(routesFeed.get(position).startLocationText);
        holder.endLocationTextView.setText(routesFeed.get(position).endLocationText);
        holder.dateTimeTextView.setText(routesFeed.get(position).dateTimeText);
        holder.distanceTextView.setText(routesFeed.get(position).distanceText);
        holder.timeTravelledTextView.setText(routesFeed.get(position).timeTravelledText);
    }

    @Override
    public int getItemCount() {
        return routesFeed.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView startLocationTextView;
        public TextView endLocationTextView;
        public TextView dateTimeTextView;
        public TextView distanceTextView;
        public TextView timeTravelledTextView;
        public CardView routesFeedCardView;

        public ViewHolder (View itemView){
            super(itemView);

            routesFeedCardView = (CardView) itemView.findViewById(R.id.routesFeedCardView);
            startLocationTextView = (TextView) itemView.findViewById(R.id.startLocationTextView);
            endLocationTextView = (TextView) itemView.findViewById(R.id.endLocationTextView);
            dateTimeTextView = (TextView) itemView.findViewById(R.id.dateTimeTextView);
            distanceTextView = (TextView) itemView.findViewById(R.id.distanceTextView);
            timeTravelledTextView = (TextView) itemView.findViewById(R.id.timeTravelledTextView);
        }
    }
}
