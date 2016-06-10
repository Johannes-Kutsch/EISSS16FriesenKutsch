package de.dtsharing.dtsharing;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class FahrtenAdapter extends BaseAdapter{

    private ArrayList<FahrtenEntry> fahrten;
    private Context context_1;

    public class ViewHolder {
        public TextView departureTime, departureName, targetTime, targetName, transitDuration, lineName, badgeCount;
        public Button delete;
    }

    public FahrtenAdapter(Context context, ArrayList<FahrtenEntry> fahrten) {
        this.context_1 = context;
        this.fahrten = fahrten;
    }

    @Override
    public int getCount() {
        return fahrten.size();
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
            convertView = LayoutInflater.from(context_1).inflate(
                    R.layout.fahrten_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.departureTime = (TextView) convertView.findViewById(R.id.tvDepartureTime);
            viewHolder.departureName = (TextView) convertView.findViewById(R.id.tvDepartureName);
            viewHolder.targetTime = (TextView) convertView.findViewById(R.id.tvTargetTime);
            viewHolder.targetName = (TextView) convertView.findViewById(R.id.tvTargetName);
            viewHolder.transitDuration = (TextView) convertView.findViewById(R.id.tvTransitDuration);
            viewHolder.lineName = (TextView) convertView.findViewById(R.id.tvLineName);
            viewHolder.badgeCount = (TextView) convertView.findViewById(R.id.tvBadge);
            viewHolder.delete = (Button) convertView.findViewById(R.id.bDelete);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FahrtenEntry fahrtenEntry = fahrten.get(position);

        viewHolder.departureTime.setText(fahrtenEntry.getDepartureTime());
        viewHolder.departureName.setText(fahrtenEntry.getDepartureName());
        viewHolder.targetTime.setText(fahrtenEntry.getTargetTime());
        viewHolder.targetName.setText(fahrtenEntry.getTargetName());
        viewHolder.transitDuration.setText(fahrtenEntry.getTransitDuration());
        viewHolder.lineName.setText(fahrtenEntry.getLineName());
        viewHolder.badgeCount.setText(fahrtenEntry.getBadgeCount());

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Fahrt löschen", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        return convertView;
    }

}
