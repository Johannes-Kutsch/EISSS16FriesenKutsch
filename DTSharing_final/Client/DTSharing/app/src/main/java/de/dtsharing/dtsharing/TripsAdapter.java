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
        public TextView departureTime, departureDate, departureName, targetTime, targetName, transitDuration, lineName, badgeCount;

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
            viewHolder.departureDate = (TextView) convertView.findViewById(R.id.tvDepartureDate);
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
        viewHolder.departureDate.setText(tripsEntry.getDepartureDate());
        viewHolder.departureName.setText(tripsEntry.getDepartureName());
        viewHolder.targetTime.setText(tripsEntry.getArrivalTime());
        viewHolder.targetName.setText(tripsEntry.getTargetName());
        viewHolder.transitDuration.setText(tripsEntry.getTravelDuration());
        viewHolder.lineName.setText(tripsEntry.getRouteName());
        viewHolder.badgeCount.setText(Integer.toString(tripsEntry.getNumberMatches()));

        return convertView;
    }

}
