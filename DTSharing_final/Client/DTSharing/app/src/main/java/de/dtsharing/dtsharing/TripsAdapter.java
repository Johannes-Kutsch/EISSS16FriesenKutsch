package de.dtsharing.dtsharing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TripsAdapter extends BaseAdapter{

    private ArrayList<TripsEntry> trips;
    private Context context_1;

    public class ViewHolder {
        public TextView departureTime;
        public TextView departureName;
        public TextView targetTime;
        public TextView targetName;
        public TextView transitDuration;
        public TextView lineName;
        public TextView badgeCount;

    }

    public TripsAdapter(Context context, ArrayList<TripsEntry> trips) {
        this.context_1 = context;
        this.trips = trips;
    }

    @Override
    public int getCount() {
        return trips.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context_1).inflate(R.layout.trips_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.departureTime = (TextView) convertView.findViewById(R.id.tvDepartureTime);
            viewHolder.departureName = (TextView) convertView.findViewById(R.id.tvDepartureName);
            viewHolder.targetTime = (TextView) convertView.findViewById(R.id.tvTargetTime);
            viewHolder.targetName = (TextView) convertView.findViewById(R.id.tvTargetName);
            viewHolder.transitDuration = (TextView) convertView.findViewById(R.id.tvTransitDuration);
            viewHolder.lineName = (TextView) convertView.findViewById(R.id.tvLineName);
            viewHolder.badgeCount = (TextView) convertView.findViewById(R.id.tvBadge);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TripsEntry tripsEntry = trips.get(position);

        viewHolder.departureTime.setText(tripsEntry.getDepartureTime());
        viewHolder.departureName.setText(tripsEntry.getDepartureName());
        viewHolder.targetTime.setText(tripsEntry.getTargetTime());
        viewHolder.targetName.setText(tripsEntry.getTargetName());
        viewHolder.transitDuration.setText(tripsEntry.getTransitDuration());
        viewHolder.lineName.setText(tripsEntry.getLineName());
        viewHolder.badgeCount.setText(tripsEntry.getBadgeCount());

        return convertView;
    }

}
