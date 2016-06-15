package de.dtsharing.dtsharing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultsAdapter extends ArrayAdapter<Result> {

    public ResultsAdapter(Context context, ArrayList<Result> results) {
        super(context, 0, results);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Result result = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.results_list, parent, false);
        }

        TextView tvId = (TextView) convertView.findViewById(R.id.tvId);
        TextView tvStart = (TextView) convertView.findViewById(R.id.tvStart);
        TextView tvDestination = (TextView) convertView.findViewById(R.id.tvDestination);
        TextView tvTicket = (TextView) convertView.findViewById(R.id.tvTicket);
        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);

        tvId.setText(result.id);
        tvStart.setText(result.start);
        tvDestination.setText(result.destination);
        tvTicket.setText(result.ticket);
        tvTime.setText(result.time);
        tvDate.setText(result.date);

        return convertView;
    }
}
