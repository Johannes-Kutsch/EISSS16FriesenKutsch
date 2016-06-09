package de.dtsharing.dtsharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MatchingActivity extends AppCompatActivity {

    private ListView lvMatches;

    private ArrayList<MatchingEntry> matches = new ArrayList<>();
    private MatchingAdapter mAdapter;

    boolean hasTicket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_matching);

        /*Adding Toolbar to Main screen*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /*Zurück Button in der Titelleiste*/
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        /*Sichere die Empfangenen Daten in Variablen*/
        Intent tripsIntent = getIntent();
        if (tripsIntent != null) {
            hasTicket = tripsIntent.getBooleanExtra("hasTicket", false);
        }

        if(hasTicket)
            mTitle.setText("Mitfahrgelegenheit Suchende");
        else
            mTitle.setText("Mitfahrgelegenheiten");

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvMatches = (ListView) findViewById(R.id.lvMatches);

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        Runnable matchesRunnable = new Runnable() {
            @Override
            public void run() {
                /*Erzeuge und verbinde Adapter mit der History ListView*/
                mAdapter = new MatchingAdapter(getApplication().getBaseContext(), matches);
                lvMatches.setAdapter(mAdapter);
            }
        };
        Thread mythread = new Thread(matchesRunnable);
        mythread.start();

        /*Fülle Array mit Beispieldaten*/
        prepareVerlaufData();

        lvMatches.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, matches.get(position).getDepartureTime(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    //<--           prepareVerlaufData Start          -->
    private void prepareVerlaufData(){
        matches.clear();
        String bild = getString(R.string.unknownPerson);
        matches.add(new MatchingEntry("Peter W.", 0.3, "12:23", "Gummersbach Bf", "13:36", "Köln Hbf", bild));
        matches.add(new MatchingEntry("Holger J.", 0.7, "12:23", "Gummersbach Bf", "13:36", "Köln Hbf", bild));

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        Runnable matchesRunnable = new Runnable() {

            @Override
            public void run() {
                /*Benachrichtige Adapter über Änderungen*/
                mAdapter.notifyDataSetChanged();
            }

        };
        Thread mythread = new Thread(matchesRunnable);
        mythread.start();
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
