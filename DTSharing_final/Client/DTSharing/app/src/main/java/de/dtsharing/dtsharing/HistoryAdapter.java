package de.dtsharing.dtsharing;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryAdapter extends BaseAdapter{

    private ArrayList<HistoryEntry> transit;
    private Context context_1;

    public class ViewHolder {
        public TextView departure;
        public TextView target;

    }

    public HistoryAdapter(Context context, ArrayList<HistoryEntry> transit) {
        this.context_1 = context;
        this.transit = transit;
    }

    /* Gibt die größe der ArrayList aus */
    @Override
    public int getCount() {
        return transit.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /* in getView werden die Views erfasst, dem viewHolder zugewiesen und abschließend mit den Daten der ArrayListe trips
     * angereichert. Die Operationen eines Adapters gelten für jedes Item welches diesem über die ArrayListe hinzugefügt wurde */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context_1).inflate(R.layout.history_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.departure = (TextView) convertView.findViewById(R.id.tvDeparture);
            viewHolder.target = (TextView) convertView.findViewById(R.id.tvTarget);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        /* Daten der Zeile "position" werden aus der ArrayList genommen und den Views zugewiesen */
        HistoryEntry historyEntry = transit.get(position);

        viewHolder.departure.setText(historyEntry.getDeparture());
        viewHolder.target.setText(historyEntry.getTarget());

        return convertView;
    }
}
