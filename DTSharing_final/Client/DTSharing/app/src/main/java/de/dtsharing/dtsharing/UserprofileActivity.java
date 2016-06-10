package de.dtsharing.dtsharing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class UserprofileActivity extends AppCompatActivity {

    private TextView mTitle;
    private ImageView userAvatar;
    private ListView lvRatings;

    private ArrayList<RatingsEntry> ratings = new ArrayList<>();
    private RatingsAdapter mAdapter;

    String userName, profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_userprofile);

        /*Adding Toolbar to Main screen*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);
        userAvatar = (ImageView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_avatar) : null);

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
        Intent ratingsIntent = getIntent();
        if (ratingsIntent != null) {
            userName = ratingsIntent.getStringExtra("userName");
            profilePicture = ratingsIntent.getStringExtra("profilePicture");
        }

        setupUserProfile();

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvRatings = (ListView) findViewById(R.id.lvRatings);

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new RatingsAdapter(getApplicationContext(), ratings);
        lvRatings.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        prepareTripsData();

    }

    private void setupUserProfile(){
        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), EncodeDecodeBase64.decodeBase64(profilePicture));
        roundDrawable.setCircular(true);
        userAvatar.setImageDrawable(roundDrawable);

        mTitle.setText(userName);

    }

    //<--           prepareVerlaufData Start          -->
    private void prepareTripsData(){
        ratings.clear();
        String bild = getString(R.string.unknownPerson);
        String message1 = "Eine Unterhaltung gestaltet sich schwierig. Ziel jedoch lebend erreicht! :)",
                message2 = "Die Fahrt war der Hammer! Bin ja auch etwas verrückt aber der Peter übertrifft alles! Immer wieder gerne!";
        ratings.add(new RatingsEntry("Angela M.", "16.05.2016", message1, bild, 2));
        ratings.add(new RatingsEntry("Werner R.", "13.05.2016", message2, bild, 4));

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
