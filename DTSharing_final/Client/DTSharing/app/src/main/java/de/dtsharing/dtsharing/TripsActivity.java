package de.dtsharing.dtsharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
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

import java.util.ArrayList;

public class TripsActivity extends AppCompatActivity {

    private ListView lvTrips;
    private CardView cvContainer;
    private CoordinatorLayout coordinatorLayout;

    private ArrayList<TripsEntry> trips = new ArrayList<>();
    private TripsAdapter mAdapter;

    private String departureName, targetName, departureDate, departureTime;
    private Boolean hasTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_trips);



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

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                /*Erzeuge und verbinde Adapter mit der History ListView*/
                mAdapter = new TripsAdapter(getApplicationContext(), trips);
                lvTrips.setAdapter(mAdapter);
            }
        });

        /*Fülle Array mit Beispieldaten*/
        getTripsData(departureName, targetName, departureTime, departureDate);
        //getTripsData(departureName, targetName, departureTime, departureDate);
        //prepareTripsData();

        lvTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Erzeuge die Matching Activity und füge Daten hinzu*/
                Intent matchingIntent = new Intent(TripsActivity.this, MatchingActivity.class);
                matchingIntent.putExtra("hasTicket", hasTicket);
                /*Starte Matching Activity*/
                startActivity(matchingIntent);
            }
        });
    }

    private void getTripsData(final String departureName, final String targetName, final String time, final String date){


        final ProgressDialog progressDialog = new ProgressDialog(TripsActivity.this,
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_circle, null));
        //progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Trips werden ermitteln...");
        progressDialog.show();

        String uri = Uri.parse("http://192.168.0.15:3000/trips")
                .buildUpon()
                .appendQueryParameter("departureStationName", departureName)
                .appendQueryParameter("targetStationName", targetName)
                .appendQueryParameter("departureTime", departureTime+":00")
                .appendQueryParameter("departureDate", departureDate)
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

                            String tripID = response.getJSONObject(i).getString("tripID"),
                                    uniqueTripID = response.getJSONObject(i).getString("uniqueTripID"),
                                    departureTime = response.getJSONObject(i).getString("departureTime"),
                                    arrivalTime = response.getJSONObject(i).getString("arrivalTime"),
                                    departureDate = response.getJSONObject(i).getString("departureDate"),
                                    routeName = "Platzhalter",
                                    travelDuration = "1:13";

                            int departureSequence = response.getJSONObject(i).getInt("departureSequence"),
                                    targetSequence = response.getJSONObject(i).getInt("targetSequence"),
                                    numberMatches = response.getJSONObject(i).getInt("numberMatches");

                            trips.add(new TripsEntry(tripID, uniqueTripID, departureSequence, departureTime.substring(0, 5), departureDate, departureName, targetSequence, arrivalTime.substring(0, 5), targetName, travelDuration, routeName, numberMatches));
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
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
    }

    //<--           prepareVerlaufData Start          -->
    private void prepareTripsData(){
        trips.clear();
        trips.add(new TripsEntry("12345", "123456", 2,"13:23", "11-06-2016", "Gummersbach Bf", 5, "14:36", "Köln Hbf", "1:13", "RB11549", 2));
        trips.add(new TripsEntry("12345", "123456", 2,"13:23", "11-06-2016", "Gummersbach Bf", 5, "14:36", "Köln Hbf", "1:13", "RB11549", 2));
        trips.add(new TripsEntry("12345", "123456", 2,"13:23", "11-06-2016", "Gummersbach Bf", 5, "14:36", "Köln Hbf", "1:13", "RB11549", 2));
        trips.add(new TripsEntry("12345", "123456", 2,"13:23", "11-06-2016", "Gummersbach Bf", 5, "14:36", "Köln Hbf", "1:13", "RB11549", 2));

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                /*Benachrichtige Adapter über Änderungen*/
                mAdapter.notifyDataSetChanged();
            }

        });
    }
    //<--           prepareVerlaufData End            -->

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
