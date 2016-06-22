package de.dtsharing.dtsharing;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Handler;


/**
 * A simple {@link Fragment} subclass.
 */
public class FahrtenFragment extends Fragment {

    RelativeLayout v;

    private ListView lvFahrten;
    private TextView noTripsContainer;

    private ArrayList<FahrtenEntry> fahrten = new ArrayList<>();
    public FahrtenAdapter mAdapter;

    private String userId;

    public FahrtenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_fahrten, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvFahrten = (ListView) v.findViewById(R.id.lvFahrten);
        noTripsContainer = (TextView) v.findViewById(R.id.noTripsContainer);

        userId = new SharedPrefsManager(this.getContext()).getUserIdSharedPrefs();

        /*Erzeuge und verbinde Adapter mit der FahrtenFragment ListView*/
        mAdapter = new FahrtenAdapter(getContext(), fahrten, userId, FahrtenFragment.this);
        lvFahrten.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        getDtTrips();

        lvFahrten.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Erzeuge die Matching Activity und füge Daten hinzu*/
                Intent fahrtDetailIntent = new Intent(v.getContext(), FahrtenDetailActivity.class);
                fahrtDetailIntent.putExtra("dtTripId", fahrten.get(position).getTripId());
                fahrtDetailIntent.putExtra("userId", userId);
                fahrtDetailIntent.putExtra("date", fahrten.get(position).getDate());
                fahrtDetailIntent.putExtra("routeName", fahrten.get(position).getRouteName());

                /*Starte Matching Activity*/
                startActivity(fahrtDetailIntent);
            }
        });

        return v;
    }

    private void getDtTrips(){

        String base_url = getResources().getString(R.string.base_url);
        final String URI = base_url+"/users/"+userId+"/dt_trips";
        fahrten.clear();

        Log.d("FahrtenFragment", "URI: "+URI);

        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                Log.d("FahrtenFragment", "response: "+response.toString());
                if(response.length() > 0) {

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject trip = response.getJSONObject(i);
                            ContentValues tripData = new ContentValues();

                            tripData.put("tripId", trip.getString("_id"));
                            tripData.put("routeName", trip.getString("route_name"));
                            tripData.put("departureName", trip.getString("departure_station_name"));
                            tripData.put("targetName", trip.getString("target_station_name"));
                            tripData.put("departureTime", trip.getString("departure_time"));
                            tripData.put("arrivalTime", trip.getString("arrival_time"));
                            tripData.put("date", trip.getString("date"));
                            tripData.put("numberPartners", trip.getString("number_partners"));

                            fahrten.add(new FahrtenEntry(tripData));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    noTripsContainer.setVisibility(View.VISIBLE);
                }

                mAdapter.notifyDataSetChanged();
                lvFahrten.setVisibility(View.VISIBLE);

            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });

        Volley.newRequestQueue(getContext()).add(jsonRequest);

    }

    public void refreshList(){

        mAdapter.notifyDataSetChanged();
        if (mAdapter.getCount() == 0) {
            noTripsContainer.setVisibility(View.VISIBLE);
        }

    }
}
