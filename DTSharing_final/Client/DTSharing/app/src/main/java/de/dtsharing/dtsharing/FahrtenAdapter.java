package de.dtsharing.dtsharing;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FahrtenAdapter extends BaseAdapter{

    private ArrayList<FahrtenEntry> fahrten;
    private Context context_1;
    private FahrtenFragment fahrtenFragment;
    private String userId;

    public class ViewHolder {
        public TextView departureTime, departureName, targetTime, targetName, transitDuration, lineName, badgeCount;
        public Button delete;
    }

    public FahrtenAdapter(Context context, ArrayList<FahrtenEntry> fahrten, String userId, FahrtenFragment fahrtenFragment) {
        this.context_1 = context;
        this.fahrten = fahrten;
        this.userId = userId;
        this.fahrtenFragment = fahrtenFragment;
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
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

        final FahrtenEntry fahrtenEntry = fahrten.get(position);

        viewHolder.departureTime.setText(fahrtenEntry.getDepartureTime());
        viewHolder.departureName.setText(fahrtenEntry.getDepartureName());
        viewHolder.targetTime.setText(fahrtenEntry.getArrivalTime());
        viewHolder.targetName.setText(fahrtenEntry.getTargetName());
        viewHolder.transitDuration.setText(fahrtenEntry.getTravelDuration());
        viewHolder.lineName.setText(fahrtenEntry.getRouteName());
        viewHolder.badgeCount.setText(fahrtenEntry.getNumberPartners());

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(parent.getContext(), R.style.AppTheme_Dialog_Alert);

                //builder.setTitle("Dialog");
                builder.setMessage("Möchtest du die Fahrt wirklich löschen?");
                builder.setPositiveButton("Fahrt löschen", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteTrip(fahrtenEntry.getTripId(), userId, position);
                    }

                });

                builder.setNegativeButton("Abbruch", null);
                builder.show();
            }
        });

        return convertView;
    }

    public void deleteTrip(String dtTripId, String userId, final int position){

        String base_url = context_1.getResources().getString(R.string.base_url);
        final String URI = base_url+"/users/"+userId+"/dt_trips/"+dtTripId;

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.DELETE, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("FahrtenAdapter", response.toString());
                if(response.has("success_message")) {
                    fahrten.remove(position);
                    updateFahrtenList(fahrten);
                }
            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });

        Volley.newRequestQueue(context_1).add(jsonRequest);

    }

    public void updateFahrtenList(ArrayList<FahrtenEntry> newlist) {
        /*fahrten.clear();
        fahrten.addAll(newlist);*/
        fahrtenFragment.refreshList();
    }

}
