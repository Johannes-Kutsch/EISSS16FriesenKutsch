package de.dtsharing.dtsharing;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MatchingActivity extends AppCompatActivity {

    private static final String LOG_TAG = MatchingActivity.class.getSimpleName();

    private ListView lvMatches;
    private Button bSubmit;
    private CardView cvContainer;
    private TextView tvNoMatch;

    private ArrayList<MatchingEntry> matches = new ArrayList<>();
    private MatchingAdapter mAdapter;

    String role, userId;
    ContentValues tripData = new ContentValues();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_matching);

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
            tripData.put("hasTicket", tripsIntent.getBooleanExtra("hasTicket", false));
            tripData.put("uniqueTripId", tripsIntent.getStringExtra("uniqueTripId"));
            tripData.put("tripId", tripsIntent.getStringExtra("tripId"));
            tripData.put("departureDate", tripsIntent.getStringExtra("departureDate"));
            tripData.put("departureTime", tripsIntent.getStringExtra("departureTime"));
            tripData.put("departureName", tripsIntent.getStringExtra("departureName"));
            tripData.put("targetName", tripsIntent.getStringExtra("targetName"));
            tripData.put("arrivalTime", tripsIntent.getStringExtra("arrivalTime"));
            tripData.put("departureSequenceId", tripsIntent.getIntExtra("departureSequenceId", 0));
            tripData.put("targetSequenceId", tripsIntent.getIntExtra("targetSequenceId", 0));
        }

        if (mTitle != null) {
            mTitle.setText(tripData.getAsBoolean("hasTicket") ? "Mitfahrgelegenheit Suchende" : "Mitfahrgelegenheiten");
        }

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvMatches = (ListView) findViewById(R.id.lvMatches);
        bSubmit = (Button) findViewById(R.id.bSubmit);
        cvContainer = (CardView) findViewById(R.id.cvContainer);
        tvNoMatch = (TextView) findViewById(R.id.tvNoMatch);

        role = tripData.getAsBoolean("hasTicket") ? "Anbietend" : "Suchend";
        bSubmit.setText("ALS "+role.toUpperCase()+" EINTRAGEN");
        String noMatchFound = getResources().getString(R.string.noMatch, role);
        System.out.println(noMatchFound);
        tvNoMatch.setText(noMatchFound);

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new MatchingAdapter(getApplicationContext(), matches);
        lvMatches.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        getMatchingData(tripData);
        //prepareMatchingData();

        lvMatches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Erzeuge die Userprofile Activity und füge Daten hinzu*/
                Intent userProfileIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                userProfileIntent.putExtra("userId", matches.get(position).getUserId());
                /*Starte Matching Activity*/
                startActivity(userProfileIntent);
            }
        });

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(view.getContext(), R.style.AppTheme_Dialog_Alert);

                builder.setMessage("Möchtest du dich wirklich als "+role+" eintragen?");
                builder.setPositiveButton("Eintragen", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        addDtTrip(tripData);

                    }

                });

                builder.setNegativeButton("Abbruch", null);
                builder.show();
            }
        });
    }

    private void addDtTrip(final ContentValues tripData){

        String base_url = getResources().getString(R.string.base_url);
        final String URI = base_url+"/users/"+userId+"/dt_trips";

        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "SERVER RESPONSE: "+response);

                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        mainIntent.putExtra("trip_created", true);
                        startActivity(mainIntent);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

                int  statusCode = error.networkResponse.statusCode;

                switch (statusCode) {
                    case 409:
                        break;
                    case 500:
                        break;
                }

            }
        })
        {
            /*Daten welche der Post-Request mitgegeben werden*/
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("unique_trip_id", tripData.getAsString("uniqueTripId"));
                params.put("trip_id", tripData.getAsString("tripId"));
                params.put("date", tripData.getAsString("departureDate"));
                params.put("departure_time", tripData.getAsString("departureTime"));
                params.put("arrival_time", tripData.getAsString("arrivalTime"));
                params.put("sequence_id_departure_station", tripData.getAsString("departureSequenceId"));
                params.put("sequence_id_target_station", tripData.getAsString("targetSequenceId"));
                params.put("departure_station_name", tripData.getAsString("departureName"));
                params.put("target_station_name", tripData.getAsString("targetName"));
                params.put("has_season_ticket", tripData.getAsString("hasTicket"));
                Log.d(LOG_TAG, "PARAMS: " + params);
                return params;
            }

        };

        Volley.newRequestQueue(getApplicationContext()).add(postRequest);

    }

    private void getMatchingData(ContentValues tripData){

        String base_url = getResources().getString(R.string.base_url);

        final String uri = Uri.parse(base_url+"/matches")
                .buildUpon()
                .appendQueryParameter("unique_trip_id", tripData.getAsString("uniqueTripId"))
                .appendQueryParameter("has_season_ticket", tripData.getAsString("hasTicket"))
                .appendQueryParameter("sequence_id_departure_station", tripData.getAsString("departureSequenceId"))
                .appendQueryParameter("sequence_id_target_station", tripData.getAsString("targetSequenceId"))
                .appendQueryParameter("user_id", userId)
                .build().toString();
        Log.d(LOG_TAG, uri);

        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, uri, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                if(response.length() > 0) {
                    addMatchingData(response);
                    cvContainer.setVisibility(View.VISIBLE);
                }
            }
        },

        new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                NetworkResponse response = error.networkResponse;
                if(response.statusCode == 404){
                    lvMatches.setVisibility(View.GONE);
                    cvContainer.setVisibility(View.VISIBLE);
                    tvNoMatch.setVisibility(View.VISIBLE);
                }
            }
        });

        Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
    }

    //<--           prepareVerlaufData Start          -->
    private void addMatchingData(final JSONArray data){

        matches.clear();

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < data.length(); i++) {
                    try {
                        JSONObject match = data.getJSONObject(i).getJSONObject("match"),
                                owner = data.getJSONObject(i).getJSONObject("owner");

                        String profileUserId = owner.getString("_id"),
                                first_name = owner.getString("first_name"),
                                last_name = owner.getString("last_name"),
                                picture = owner.getString("picture"),
                                departureName = match.getString("owner_departure_station_name"),
                                targetName = match.getString("owner_target_station_name"),
                                departureTime = match.getString("owner_departure_time"),
                                arrivalTime = match.getString("owner_arrival_time"),
                                name = first_name+" "+last_name;

                        double averageRating = owner.getDouble("average_rating");
                        Log.d(LOG_TAG, first_name);
                        Log.d(LOG_TAG, last_name);
                        Log.d(LOG_TAG, departureName);
                        Log.d(LOG_TAG, targetName);
                        Log.d(LOG_TAG, departureTime);
                        Log.d(LOG_TAG, arrivalTime);
                        Log.d(LOG_TAG, name);
                        Log.d(LOG_TAG, Double.toString(averageRating));



                        matches.add(new MatchingEntry(name, averageRating, departureTime, departureName, arrivalTime, targetName, picture, tripData.getAsBoolean("hasTicket"), profileUserId));
                        Log.d(LOG_TAG, "MATCH DETAILS: "+match.toString());
                        Log.d(LOG_TAG, "OWNER DETAILS: "+owner.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        });
        myThread.start();

        /*Benachrichtige Adapter über Änderungen*/
        //mAdapter.notifyDataSetChanged();
    }
    //<--           prepareVerlaufData End            -->

    //<--           OnOptionsItemSelected Start         -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*Zurück Button geklickt*/
            case android.R.id.home:
                /*Schließe Aktivität ab und kehre zurück*/
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           OnOptionsItemSelected End         -->

}
