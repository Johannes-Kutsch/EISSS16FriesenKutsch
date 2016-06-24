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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private String departureName, targetName, departureDate, departureTime, userId, base_url;
    private Boolean hasTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_trips);

        /* Der SharedPrefs Helper wird erzeugt und base_url sowie userID werden ausgelesen */
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(TripsActivity.this);
        base_url = sharedPrefsManager.getBaseUrl();
        userId = sharedPrefsManager.getUserIdSharedPrefs();

        /* Toolbar und Title View werden erfasst */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

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

        /* Request an den Server mit Reisedaten als Query Parameter */
        getTripsData(departureName, targetName, departureTime, departureDate, hasTicket);

        /* Bei Auswahl eines Trips wird die MatchingActivity gestartet und mit allen benötigten Extras angereichert */
        lvTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*Erzeuge die Matching Activity und füge Daten hinzu*/
                Intent matchingIntent = new Intent(TripsActivity.this, MatchingActivity.class);
                matchingIntent.putExtra("comesFromTrips", true);
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

    /* Request an den Server mit Reisedaten als Query Parameter. Als Response werden die Trips erwartet */
    private void getTripsData(final String departureName, final String targetName, final String time, final String date, final boolean hasTicket){

        /* Es wird ein ProgressDialog erzeugt,welcher anhält bis die Daten erhalten wurden oder ein Fehler auftritt */
        final ProgressDialog progressDialog = new ProgressDialog(TripsActivity.this,
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_circle, null));
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Trips werden ermittelt...");
        progressDialog.show();

        /* Es wird eine URI erzeugt, welche die Reisedaten als Query Parameter beinhaltet */
        final String uri = Uri.parse(base_url+"/trips")
                .buildUpon()
                .appendQueryParameter("departure_station_name", departureName)
                .appendQueryParameter("target_station_name", targetName)
                .appendQueryParameter("departure_time", departureTime)
                .appendQueryParameter("departure_date", departureDate)
                .appendQueryParameter("has_season_ticket", Boolean.toString(hasTicket))
                .appendQueryParameter("user_id", userId)
                .build().toString();

        /* Die ArrayListe wird geleert, falls die Methode aufgerufen wird und diese noch Daten beinhaltet */
        trips.clear();

        /* Es wird eine Request an den Server gesandt. Als Antwort wird bei einem Fehler ein JSONObject, bei einer Erfolgreichen Response ein JSONArray erwartet */
        final StringRequest jsonRequest = new StringRequest(
                Request.Method.GET, uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                /* Bei einem Fehler wird die Error Message des Servers ausgelesen und der Benutzer zurück zur Suchmaske "geworfen".
                 * Dort wird dieser dann über eine negative Snackbar angereichert mit der Message des Servers über die Art des Fehlers informiert */
                if(response.contains("error_message")){
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("error_message");
                        progressDialog.dismiss();
                        Intent messageIntent = new Intent();
                        messageIntent.putExtra("message", message);
                        setResult(Activity.RESULT_CANCELED, messageIntent);
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                /* Bei einer Erfolgreichen Response werden die Daten der ArrayListe<TripsEntry> hinzugefügt */
                } else {
                    try {
                        JSONArray jsonResponse = new JSONArray(response);
                        /*Fülle den Adapter mit den Station Namen*/
                        for (int i = 0; i < jsonResponse.length(); i++) {

                            /* Erhalten aller relevanten Daten */
                            String tripID = jsonResponse.getJSONObject(i).getString("trip_id"),
                                    uniqueTripID = jsonResponse.getJSONObject(i).getString("unique_trip_id"),
                                    departureTime = jsonResponse.getJSONObject(i).getString("departure_time"),
                                    arrivalTime = jsonResponse.getJSONObject(i).getString("arrival_time"),
                                    departureDate = jsonResponse.getJSONObject(i).getString("departure_date"),
                                    routeName = jsonResponse.getJSONObject(i).getString("route_name"),
                                    departureName = jsonResponse.getJSONObject(i).getString("departure_station_name"),
                                    targetName = jsonResponse.getJSONObject(i).getString("target_station_name");

                            String travelDuration = new CalculateTravelDuration().getHoursMinutes(departureTime, arrivalTime);

                            int departureSequence = jsonResponse.getJSONObject(i).getInt("sequence_id_departure_station"),
                                    targetSequence = jsonResponse.getJSONObject(i).getInt("sequence_id_target_station"),
                                    numberMatches = jsonResponse.getJSONObject(i).getInt("number_matches");

                            /* ArrayListe wird mit den Daten angereichert */
                            trips.add(new TripsEntry(tripID, uniqueTripID, departureSequence, departureTime, departureDate, departureName, targetSequence, arrivalTime, targetName, travelDuration, routeName, numberMatches));
                        }
                        /* Zeige den Container, benachrichtige den Adapter dass neue Daten vorliegen und beende den ProgressDialog*/
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

                /* Bei einem Fehlerhaften StatusCode wird der ProgressDialog beendet und der Benutzer zurück zur Suchmaske "geworfen"
                 * Anschließend wird er über eine negative Snackbar darüber Informiert, dass keine Verbindung zum Server möglich ist */
                progressDialog.dismiss();
                Intent message = new Intent();
                message.putExtra("message", "Verbindung zum Sever nicht möglich!");
                setResult(Activity.RESULT_CANCELED, message);
                finish();
            }
        });

        /* Das ermitteln der Trips kann bei vielen Verbindungen etwas länger dauern. Daraufhin wurde der Timeout angehoben,
         * da der Standard Timeout von Volley so kurz war */
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        /* Request wird der Volley Queue hinzugefügt und abgearbeitet */
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

    /* Hardware Zurück Button gedrückt muss hier erweitert werden, da die Suchmaske einen Code erwartet und bei nicht erhalt einen Fehler wirft
     * und die Applikation crasht */
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

}
