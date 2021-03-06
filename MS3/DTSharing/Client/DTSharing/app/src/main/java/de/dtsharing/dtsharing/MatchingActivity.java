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
import java.util.List;
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

    String role, userId, base_url;
    ContentValues tripData = new ContentValues();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_matching);

        /* Der SharedPrefsManager wird erzeugt und anschließend werden base_url und userId ausgelesen */
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(MatchingActivity.this);
        base_url = sharedPrefsManager.getBaseUrl();
        userId = sharedPrefsManager.getUserIdSharedPrefs();

        /* Toolbar und Title werden erfasst */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvMatches = (ListView) findViewById(R.id.lvMatches);
        bSubmit = (Button) findViewById(R.id.bSubmit);
        cvContainer = (CardView) findViewById(R.id.cvContainer);
        tvNoMatch = (TextView) findViewById(R.id.tvNoMatch);

        /* Custom Toolbar wird gesetzt */
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
            /*Zurück Button in der Titelleiste*/
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        /* Fallunterscheidung beim beziehen der Daten des Intents. Diese Unterscheiden sich in sofern, dass
         * beim erreichen der MatchActivity über die Notification der submit Button ausgeblendet wird und bereits
         * eine dt_trip_id vorhanden ist, welche der des Benutzers entspricht, da dieser Trip gelöscht werden muss,
         * wenn der Benutzer sich entschließen sollte sich mit dem potentiellen Partner zu matchen */
        if(getIntent().getBooleanExtra("comesFromTrips", false)) {
            Intent tripsIntent = getIntent();
            tripData.put("hasTicket", tripsIntent.getBooleanExtra("hasTicket", false));
            tripData.put("uniqueTripId", tripsIntent.getStringExtra("uniqueTripId"));
            tripData.put("tripId", tripsIntent.getStringExtra("tripId"));
            tripData.put("departureDate", tripsIntent.getStringExtra("departureDate"));
            tripData.put("departureTime", tripsIntent.getStringExtra("departureTime"));
            tripData.put("departureName", tripsIntent.getStringExtra("departureName"));
            tripData.put("targetName", tripsIntent.getStringExtra("targetName"));
            tripData.put("arrivalTime", tripsIntent.getStringExtra("arrivalTime"));
            tripData.put("routeName", tripsIntent.getStringExtra("routeName"));
            tripData.put("departureSequenceId", tripsIntent.getIntExtra("departureSequenceId", 0));
            tripData.put("targetSequenceId", tripsIntent.getIntExtra("targetSequenceId", 0));
        }
        if(getIntent().getBooleanExtra("comesFromNotification", false)){
            Intent notificationIntent = getIntent();
            tripData.put("dtTripId", notificationIntent.getStringExtra("dtTripId"));
            tripData.put("uniqueTripId", notificationIntent.getStringExtra("uniqueTripId"));
            tripData.put("hasTicket", notificationIntent.getStringExtra("hasSeasonTicket"));
            tripData.put("departureSequenceId", notificationIntent.getStringExtra("sequenceIdDepartureStation"));
            tripData.put("targetSequenceId", notificationIntent.getStringExtra("sequenceIdTargetStation"));
            tripData.put("departureTime", notificationIntent.getStringExtra("departureTime"));
            tripData.put("arrivalTime", notificationIntent.getStringExtra("arrivalTime"));
            tripData.put("departureName", notificationIntent.getStringExtra("departureStationName"));
            tripData.put("targetName", notificationIntent.getStringExtra("targetStationName"));
            bSubmit.setVisibility(View.GONE);
        }


        /* Der Toolbar Title wird entsprechend des booleans "hasTicket" gesetzt */
        if (mTitle != null) {
            mTitle.setText(tripData.getAsBoolean("hasTicket") ? "Mitfahrgelegenheit Suchende" : "Mitfahrgelegenheiten");
        }

        /* Auch die Beschriftung des Buttons wird entsprechend des booleans "hasTicket" vorgenmmmen. Ebenso der Text, wenn kein
         * Match gefunden wurde */
        role = tripData.getAsBoolean("hasTicket") ? "Anbietend" : "Suchend";
        bSubmit.setText("ALS "+role.toUpperCase()+" EINTRAGEN");
        String noMatchFound = getResources().getString(R.string.noMatch, role);
        tvNoMatch.setText(noMatchFound);

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new MatchingAdapter(getApplicationContext(), matches, tripData);
        lvMatches.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        getMatchingData(tripData);
        //prepareMatchingData();

        /* Es wird ein onItemClick Listener für die ListView Matches erzeugt, welcher den Benutzer zum UserProfile des Matches bringt */
        lvMatches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Erzeuge die Userprofile Activity und füge Daten hinzu*/
                Intent userProfileIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                userProfileIntent.putExtra("userId", matches.get(position).getOwnerUserId());
                /*Starte Matching Activity*/
                startActivity(userProfileIntent);
            }
        });

        /* Es wird ein onClickListener für den submit Button erstellt */
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Es wird ein Dialog erzeugt, welcher eine Bestätigung des Benutzers anfordert */
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(view.getContext(), R.style.AppTheme_Dialog_Alert);

                builder.setMessage("Möchtest du dich wirklich als "+role+" eintragen?");

                /* Wird der Dialog positiv bestötigt, werden die Daten an den Server gesandt um einen Trip zu erzeugen */
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

        /* Festlegung der URI */
        final String URI = base_url+"/users/"+userId+"/dt_trips";

        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
                new Response.Listener<String>() {

                    /* Bei einer positiven response wird der Benutzer zurück zur MainActivity (Suchmaske) und durch den
                     * Boolean "trip_created" auf Seite 2 (Fahrten) geführt. Anschließend erhält er dort eine positive
                     * Snackbar, dass der Trip erfolgreich erstellt wurde */
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
                params.put("route_name", tripData.getAsString("routeName"));
                Log.d(LOG_TAG, "PARAMS: " + params);
                return params;
            }

        };

        Volley.newRequestQueue(getApplicationContext()).add(postRequest);

    }

    /* Ein GET Request an den Server, welcher über Query Paramter mit Daten angereichert wird und als Response
     * potentielle Matches enthält, oder die TextView "tvNoMatch" einblendet */
    private void getMatchingData(ContentValues tripData){

        /* Die URI wird erstellt und durch Query Parameter angereichert */
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

                /* Wenn die response Daten enthält, werden diese der ArrayList hinzugefügt und abschließend
                 * der Container angezeigt */
                if(response.length() > 0) {
                    addMatchingData(response);
                    cvContainer.setVisibility(View.VISIBLE);
                }
            }
        },

        new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                /* Gibt es keine potentiellen Matches wird der StatusCode 404 geworfen, welcher dafür sorgt
                 * dass die entsprechende TextView für eine Erfolglose Suche angezeigt wird */
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

        /* ArrayList wird geleert */
        matches.clear();

        /* ArrayList wird in einem Worker Thread mit Daten gefüllt */
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < data.length(); i++) {
                    try {
                        JSONObject match = data.getJSONObject(i).getJSONObject("match"),
                                owner = data.getJSONObject(i).getJSONObject("owner");

                        ContentValues ownerTripDetails = new ContentValues(),
                                ownerDetails = new ContentValues();

                        ownerTripDetails.put("dtTripId", match.getString("_id"));
                        ownerTripDetails.put("date", match.getString("date"));
                        ownerTripDetails.put("departureTime", match.getString("owner_departure_time"));
                        ownerTripDetails.put("arrivalTime", match.getString("owner_arrival_time"));
                        ownerTripDetails.put("departureName", match.getString("owner_departure_station_name"));
                        ownerTripDetails.put("targetName", match.getString("owner_target_station_name"));

                        ownerDetails.put("ownerUserId", owner.getString("_id"));
                        ownerDetails.put("firstName", owner.getString("first_name"));
                        ownerDetails.put("lastName", owner.getString("last_name"));
                        ownerDetails.put("picture", owner.getString("picture"));
                        ownerDetails.put("averageRating", owner.getDouble("average_rating"));

                        matches.add(new MatchingEntry(ownerTripDetails, ownerDetails));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                /* Im UI Thread wird der Adapter über Änderungen informiert */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        });
        myThread.start();
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
