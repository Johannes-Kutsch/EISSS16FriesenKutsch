package de.dtsharing.dtsharing;

import android.content.Intent;
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

import java.util.ArrayList;

public class MatchingActivity extends AppCompatActivity {

    private ListView lvMatches;
    private Button bSubmit;

    private ArrayList<MatchingEntry> matches = new ArrayList<>();
    private MatchingAdapter mAdapter;

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
        }

        if (mTitle != null) {
            mTitle.setText(hasTicket ? "Mitfahrgelegenheit Suchende" : "Mitfahrgelegenheiten");
        }

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvMatches = (ListView) findViewById(R.id.lvMatches);
        bSubmit = (Button) findViewById(R.id.bSubmit);

        bSubmit.setText(hasTicket ? "ALS ANBIETEND EINTRAGEN" : "ALS SUCHEND EINTRAGEN");

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                /*Erzeuge und verbinde Adapter mit der History ListView*/
                mAdapter = new MatchingAdapter(getApplicationContext(), matches);
                lvMatches.setAdapter(mAdapter);
            }
        });

        /*Fülle Array mit Beispieldaten*/
        prepareMatchingData();

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

    //<--           prepareVerlaufData Start          -->
    private void prepareMatchingData(){
        matches.clear();
        String bild = getString(R.string.unknownPerson);
        matches.add(new MatchingEntry("Peter W.", 3.77, "12:23", "Gummersbach Bf", "13:36", "Köln Hbf", bild, hasTicket));
        matches.add(new MatchingEntry("Holger J.", 2.6, "12:23", "Gummersbach Bf", "13:36", "Köln Hbf", bild, hasTicket));

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
