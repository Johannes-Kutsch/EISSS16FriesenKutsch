package de.dtsharing.dtsharing;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MatchingActivity extends AppCompatActivity {

    private ListView lvMatches;
    private Button bSubmit;

    private ArrayList<MatchingEntry> matches = new ArrayList<>();
    private MatchingAdapter mAdapter;

    String uniqueTripId;
    int departureSequenceId, targetSequenceId;
    boolean hasTicket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_matching);

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
            hasTicket = tripsIntent.getBooleanExtra("hasTicket", false);
            uniqueTripId = tripsIntent.getStringExtra("uniqueTripId");
            departureSequenceId = tripsIntent.getIntExtra("departureSequenceId", 0);
            targetSequenceId = tripsIntent.getIntExtra("targetSequenceId", 0);
        }

        if (mTitle != null) {
            mTitle.setText(hasTicket ? "Mitfahrgelegenheit Suchende" : "Mitfahrgelegenheiten");
        }

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvMatches = (ListView) findViewById(R.id.lvMatches);
        bSubmit = (Button) findViewById(R.id.bSubmit);

        bSubmit.setText(hasTicket ? "ALS ANBIETEND EINTRAGEN" : "ALS SUCHEND EINTRAGEN");

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new MatchingAdapter(getApplicationContext(), matches);
        lvMatches.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        getMatchingData(uniqueTripId, departureSequenceId, targetSequenceId, hasTicket);
        //prepareMatchingData();

        lvMatches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Erzeuge die Userprofile Activity und füge Daten hinzu*/
                Intent userProfileIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                userProfileIntent.putExtra("profilePicture", getString(R.string.unknownPerson));
                userProfileIntent.putExtra("userName", matches.get(position).getUserName());
                /*Starte Matching Activity*/
                startActivity(userProfileIntent);
            }
        });
    }

    private void getMatchingData(String uniqueTripId, int departureSequenceId, int targetSequenceId, boolean hasTicket){

        final String uri = Uri.parse("http://192.168.0.15:3000/matches")
                .buildUpon()
                .appendQueryParameter("unique_trip_id", uniqueTripId)
                .appendQueryParameter("has_season_ticket", Boolean.toString(hasTicket))
                .appendQueryParameter("sequence_id_departure_station", Integer.toString(departureSequenceId))
                .appendQueryParameter("sequence_id_target_station", Integer.toString(targetSequenceId))
                .appendQueryParameter("user_id", "oh1mann2wie3ist4")
                .build().toString();

        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, uri, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                if(response.length() > 0)
                    addMatchingData(response);
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
    private void addMatchingData(final JSONArray data){
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < data.length(); i++) {
                    try {
                        String userId = data.getJSONObject(i).getString("user_id"),
                                picture = data.getJSONObject(i).getString("picture"),
                                name = data.getJSONObject(i).getString("user_name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        myThread.start();


        matches.clear();
        String bild = getString(R.string.unknownPerson);
        matches.add(new MatchingEntry("Peter W.", 3.77, "12:23", "Gummersbach Bf", "13:36", "Köln Hbf", bild, hasTicket));
        matches.add(new MatchingEntry("Holger J.", 2.6, "12:23", "Gummersbach Bf", "13:36", "Köln Hbf", bild, hasTicket));

        /*Benachrichtige Adapter über Änderungen*/
        mAdapter.notifyDataSetChanged();
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
