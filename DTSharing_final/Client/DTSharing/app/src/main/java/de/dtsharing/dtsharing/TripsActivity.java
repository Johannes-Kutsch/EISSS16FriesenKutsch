package de.dtsharing.dtsharing;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class TripsActivity extends AppCompatActivity {

    private ListView lvTrips;

    private ArrayList<TripsEntry> trips = new ArrayList<>();
    private TripsAdapter mAdapter;

    String departureName, targetName, date, time;
    Boolean hasTicket;

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
            date = tripsIntent.getStringExtra("date");
            time = tripsIntent.getStringExtra("time");
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
            }
        });

        /*Fülle Array mit Beispieldaten*/
        prepareTripsData();

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

    //<--           prepareVerlaufData Start          -->
    private void prepareTripsData(){
        trips.clear();
        trips.add(new TripsEntry("13:23", "Gummersbach Bf", "14:36", "Köln Hbf", "1:13", "RB11549", "2"));
        trips.add(new TripsEntry("13:53", "Gummersbach Bf", "15:06", "Köln Hbf", "1:13", "RB11549", "0"));
        trips.add(new TripsEntry("14:23", "Gummersbach Bf", "15:36", "Köln Hbf", "1:13", "RB11549", "1"));
        trips.add(new TripsEntry("15:23", "Gummersbach Bf", "16:36", "Köln Hbf", "1:13", "RB11549", "3"));
        trips.add(new TripsEntry("16:23", "Gummersbach Bf", "17:36", "Köln Hbf", "1:13", "RB11549", "0"));
        trips.add(new TripsEntry("16:53", "Gummersbach Bf", "18:06", "Köln Hbf", "1:13", "RB11549", "0"));

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
