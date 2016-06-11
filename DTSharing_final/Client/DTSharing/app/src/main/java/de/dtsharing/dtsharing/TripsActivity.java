package de.dtsharing.dtsharing;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
        lvTrips = (ListView) findViewById(R.id.lvTrips);

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                /*Erzeuge und verbinde Adapter mit der History ListView*/
                mAdapter = new TripsAdapter(getApplicationContext(), trips);
                lvTrips.setAdapter(mAdapter);
                getTripsData(departureName, targetName, departureTime, departureDate);
            }
        });

        /*Fülle Array mit Beispieldaten*/
        //getTripsData(departureName, targetName, departureTime, departureDate);
        //prepareTripsData();

        lvTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Erzeuge die Matching Activity und füge Daten hinzu*/
                Intent matchingIntent = new Intent(getApplicationContext(), MatchingActivity.class);
                matchingIntent.putExtra("hasTicket", hasTicket);
                /*Starte Matching Activity*/
                startActivity(matchingIntent);
            }
        });
    }

    private void getTripsData(final String departureName, final String targetName, final String time, final String date){

        /*String  param1 = "departureStationName="+departureName,
                param2 = "targetStationName="+targetName,
                param3 = "departureTime="+departureTime,
                param4 = "departureDate="+departureDate;
        String URI = "http://10.0.2.2:3000/trips?"+param1+"&"+param2+"&"+param3+"&"+param4;*/

        String uri = Uri.parse("http://10.0.2.2:3000/trips")
                .buildUpon()
                .appendQueryParameter("departureStationName", departureName)
                .appendQueryParameter("targetStationName", targetName)
                .appendQueryParameter("departureTime", departureTime)
                .appendQueryParameter("departureDate", departureDate)
                .build().toString();

        Log.i("url", uri);

        trips.clear();

        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, uri, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                        /*Fülle den Adapter mit den Station Namen*/
                    for (int i = 0; i < response.length(); i++) {

                        String  tripID = response.getJSONObject(i).getString("tripID"),
                                uniqueTripID = response.getJSONObject(i).getString("uniqueTripID"),
                                departureTime = response.getJSONObject(i).getString("departureTime"),
                                arrivalTime = response.getJSONObject(i).getString("arrivalTime"),
                                departureDate = response.getJSONObject(i).getString("departureDate"),
                                routeName = "Platzhalter",
                                travelDuration = "1:13";

                        int     departureSequence = response.getJSONObject(i).getInt("departureSequence"),
                                targetSequence = response.getJSONObject(i).getInt("targetSequence"),
                                numberMatches = response.getJSONObject(i).getInt("numberMatches");

                        trips.add(new TripsEntry(tripID, uniqueTripID, departureSequence, departureTime, departureDate, departureName, targetSequence, arrivalTime, targetName, travelDuration, routeName, numberMatches));
                    }
                        /*Benachrichtige den Adapter dass neue Daten vorliegen*/
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
        new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

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
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           OnOptionsItemSelected End         -->

}
