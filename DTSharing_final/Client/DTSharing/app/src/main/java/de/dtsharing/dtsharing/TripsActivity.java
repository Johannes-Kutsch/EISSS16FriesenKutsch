package de.dtsharing.dtsharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TripsActivity extends AppCompatActivity {

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    private ListView lvTrips;
    private CardView cvContainer;
    private CoordinatorLayout coordinatorLayout;

    private ArrayList<TripsEntry> trips = new ArrayList<>();
    private TripsAdapter mAdapter;

    private String departureName, targetName, departureDate, departureTime, userId;
    private Boolean hasTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_trips);

        userId = new SharedPrefsManager(getApplicationContext()).getUserIdSharedPrefs();

        /*Adding Toolbar to Main screen*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
            /*Zurück Button in der Titelleiste*/
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        /*Sichere die Empfangenen Daten in Variablen*/
        Intent tripsIntent = getIntent();
        if (tripsIntent != null) {
            departureName = tripsIntent.getStringExtra("departureName");
            targetName = tripsIntent.getStringExtra("targetName");
            hasTicket = tripsIntent.getBooleanExtra("hasTicket", false);
            departureDate = tripsIntent.getStringExtra("date");
            departureTime = tripsIntent.getStringExtra("time");
        }

        /*Erfassen der Views mit denen interagiert werden soll*/
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        lvTrips = (ListView) findViewById(R.id.lvTrips);
        cvContainer = (CardView) findViewById(R.id.cvContainer);
        cvContainer.setVisibility(View.INVISIBLE);

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new TripsAdapter(getApplicationContext(), trips);
        lvTrips.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        getTripsData(departureName, targetName, departureTime, departureDate, hasTicket);
        //getTripsData(departureName, targetName, departureTime, departureDate);
        //prepareTripsData();

        lvTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Erzeuge die Matching Activity und füge Daten hinzu*/
                Intent matchingIntent = new Intent(TripsActivity.this, MatchingActivity.class);
                matchingIntent.putExtra("hasTicket", hasTicket);
                matchingIntent.putExtra("uniqueTripId", trips.get(position).getUniqueTripID());
                matchingIntent.putExtra("tripId", trips.get(position).getTripID());
                matchingIntent.putExtra("departureDate", trips.get(position).getDepartureDate());
                matchingIntent.putExtra("departureName", trips.get(position).getDepartureName());
                matchingIntent.putExtra("departureTime", trips.get(position).getDepartureTime());
                matchingIntent.putExtra("targetName", trips.get(position).getTargetName());
                matchingIntent.putExtra("arrivalTime", trips.get(position).getArrivalTime());
                matchingIntent.putExtra("routeName", trips.get(position).getRouteName());
                matchingIntent.putExtra("departureSequenceId", trips.get(position).getDepartureSequence());
                matchingIntent.putExtra("targetSequenceId", trips.get(position).getTargetSequence());
                /*Starte Matching Activity*/
                startActivity(matchingIntent);
            }
        });
    }

    private void getTripsData(final String departureName, final String targetName, final String time, final String date, final boolean hasTicket){

        final ProgressDialog progressDialog = new ProgressDialog(TripsActivity.this,
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_circle, null));
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Trips werden ermittelt...");
        progressDialog.show();

        String base_url = getResources().getString(R.string.base_url);
        final String uri = Uri.parse(base_url+"/trips")
                .buildUpon()
                .appendQueryParameter("departure_station_name", departureName)
                .appendQueryParameter("target_station_name", targetName)
                .appendQueryParameter("departure_time", departureTime)
                .appendQueryParameter("departure_date", departureDate)
                .appendQueryParameter("has_season_ticket", Boolean.toString(hasTicket))
                .appendQueryParameter("user_id", userId)
                .build().toString();


        trips.clear();

        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, uri, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                if(response.length() == 0){
                    Intent message = new Intent();
                    message.putExtra("message", "Es konnten keine Verbindungen gefunden werden. Bitte überprüfe deine Eingaben.");
                    setResult(Activity.RESULT_CANCELED, message);
                    finish();
                }else {
                    try {
                        /*Fülle den Adapter mit den Station Namen*/
                        for (int i = 0; i < response.length(); i++) {

                            String tripID = response.getJSONObject(i).getString("trip_id"),
                                    uniqueTripID = response.getJSONObject(i).getString("unique_trip_id"),
                                    departureTime = response.getJSONObject(i).getString("departure_time"),
                                    arrivalTime = response.getJSONObject(i).getString("arrival_time"),
                                    departureDate = response.getJSONObject(i).getString("departure_date"),
                                    routeName = response.getJSONObject(i).getString("route_name"),
                                    departureName = response.getJSONObject(i).getString("departure_station_name"),
                                    targetName = response.getJSONObject(i).getString("target_station_name");

                            String travelDuration = new CalculateTravelDuration().getHoursMinutes(departureTime, arrivalTime);

                            int departureSequence = response.getJSONObject(i).getInt("sequence_id_departure_station"),
                                    targetSequence = response.getJSONObject(i).getInt("sequence_id_target_station"),
                                    numberMatches = response.getJSONObject(i).getInt("number_matches");

                            trips.add(new TripsEntry(tripID, uniqueTripID, departureSequence, departureTime, departureDate, departureName, targetSequence, arrivalTime, targetName, travelDuration, routeName, numberMatches));
                        }
                        /*Benachrichtige den Adapter dass neue Daten vorliegen*/
                        cvContainer.setVisibility(View.VISIBLE);
                        mAdapter.notifyDataSetChanged();
                        progressDialog.dismiss();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        },
        new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressDialog.dismiss();
                Intent message = new Intent();
                message.putExtra("message", "Verbindung zum Sever nicht möglich!");
                setResult(Activity.RESULT_CANCELED, message);
                finish();
            }
        });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
    }

    //<--           OnOptionsItemSelected Start         -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*Zurück Button geklickt*/
            case android.R.id.home:
                /*Schließe Aktivität ab und kehre zurück*/
                setResult(Activity.RESULT_OK);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           OnOptionsItemSelected End         -->

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

}
