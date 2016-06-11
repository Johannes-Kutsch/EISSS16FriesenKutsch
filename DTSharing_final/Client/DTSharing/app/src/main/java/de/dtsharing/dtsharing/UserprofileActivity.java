package de.dtsharing.dtsharing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private TextView mTitle, tvUserAge, tvUserHobbys, tvUserDescription, tvCountOfferer, tvCountPassenger, tvAverageRating, tvCountRating;
    private ImageView userAvatar, ivStar1, ivStar2, ivStar3, ivStar4, ivStar5;
    private ListView lvRatings;

    private ArrayList<RatingsEntry> ratings = new ArrayList<>();
    private RatingsAdapter mAdapter = null;

    private String userProfilePicture, userName, userHobbys, userDescription;
    private int userAge, userCountOfferer, userCountPassenger;

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

        /*Erfassen der Views mit denen interagiert werden soll*/
        tvUserAge = (TextView) findViewById(R.id.tvUserAge);
        tvUserHobbys = (TextView) findViewById(R.id.tvUserHobbys);
        tvUserDescription = (TextView) findViewById(R.id.tvUserDescription);
        tvCountOfferer = (TextView) findViewById(R.id.tvCountOfferer);
        tvCountPassenger = (TextView) findViewById(R.id.tvCountPassenger);
        tvCountRating = (TextView) findViewById(R.id.tvCountRatings);
        tvAverageRating = (TextView) findViewById(R.id.tvAverageRating);

        ivStar1 = (ImageView) findViewById(R.id.ivStar1);
        ivStar2 = (ImageView) findViewById(R.id.ivStar2);
        ivStar3 = (ImageView) findViewById(R.id.ivStar3);
        ivStar4 = (ImageView) findViewById(R.id.ivStar4);
        ivStar5 = (ImageView) findViewById(R.id.ivStar5);

        lvRatings = (ListView) findViewById(R.id.lvRatings);


        /*Sichere die Empfangenen Daten in Variablen*/
        Intent userProfileIntent = getIntent();
        if (userProfileIntent != null) {
            userName = userProfileIntent.getStringExtra("userName");
            userProfilePicture = userProfileIntent.getStringExtra("profilePicture");
        }

        prepareUserProfileData();
        setupUserProfile(userProfilePicture, userName, userAge, userHobbys, userDescription, userCountOfferer, userCountPassenger, 2.5, 2);

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new RatingsAdapter(getApplicationContext(), ratings);
        lvRatings.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        prepareRatingData();

    }

    private void prepareUserProfileData(){

        userAge = 23;
        userHobbys = "Kellerkind. Sagt alles, oder?";
        userDescription = "Tja was soll ich groß sagen? Ich fahre gerne Zug und Zug fährt gerne mich.. Win Win. Gelacht wird später.";
        userCountOfferer = 4;
        userCountPassenger = 11;

    }

    private void setupUserProfile(String picture, String name, int age, String hobbys, String description, int countOfferer, int countPassenger, double averageRating, int countRatings){
        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), EncodeDecodeBase64.decodeBase64(picture));
        roundDrawable.setCircular(true);
        userAvatar.setImageDrawable(roundDrawable);

        mTitle.setText(name);
        tvUserAge.setText(age + " Jahre");
        tvUserHobbys.setText(hobbys);
        tvUserDescription.setText(description);
        tvCountOfferer.setText(Integer.toString(countOfferer));
        tvCountPassenger.setText(Integer.toString(countPassenger));
        tvAverageRating.setText(Double.toString(averageRating));
        tvCountRating.setText("("+Integer.toString(countRatings)+")");

        setRating(averageRating);
    }

    private void setRating(double averageRating){

        /*Default = starBorder => Somit muss dieser Stern nicht zugewiesen werden*/
        int starFull = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_full_24dp", null, null);
        int starHalf = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_half_24dp", null, null);

        int wholeRating = (int) averageRating;
        double fractionalRating = averageRating - wholeRating;

        switch (wholeRating){
            case 5:
                ivStar5.setImageResource(starFull);
            case 4:
                ivStar4.setImageResource(starFull);
            case 3:
                ivStar3.setImageResource(starFull);
            case 2:
                ivStar2.setImageResource(starFull);
            case 1:
                ivStar1.setImageResource(starFull);
        }


        if(fractionalRating > 0.25 && fractionalRating < 0.75){
            switch (wholeRating+1){
                case 1:
                    ivStar1.setImageResource(starHalf);
                    break;
                case 2:
                    ivStar2.setImageResource(starHalf);
                    break;
                case 3:
                    ivStar3.setImageResource(starHalf);
                    break;
                case 4:
                    ivStar4.setImageResource(starHalf);
                    break;
                case 5:
                    ivStar5.setImageResource(starHalf);
                    break;
            }
        }else if(fractionalRating >= 0.75){
            switch (wholeRating+1){
                case 1:
                    ivStar1.setImageResource(starFull);
                    break;
                case 2:
                    ivStar2.setImageResource(starFull);
                    break;
                case 3:
                    ivStar3.setImageResource(starFull);
                    break;
                case 4:
                    ivStar4.setImageResource(starFull);
                    break;
                case 5:
                    ivStar5.setImageResource(starFull);
                    break;
            }
        }

    }

    //<--           prepareRatingData Start          -->
    private void prepareRatingData(){
        ratings.clear();
        String bild = getString(R.string.unknownPerson);
        String message1 = "Eine Unterhaltung gestaltet sich schwierig. Ziel jedoch lebend erreicht! :)",
                message2 = "Die Fahrt war der Hammer! Bin ja auch etwas verrückt aber der Peter übertrifft alles! Immer wieder gerne!";
        ratings.add(new RatingsEntry("Angela M.", "16.05.2016", message1, bild, 1));
        ratings.add(new RatingsEntry("Werner R.", "13.05.2016", message2, bild, 4));
        ratings.add(new RatingsEntry("Werner R.", "13.05.2016", message2, bild, 4));

        /*Benachrichtige Adapter über Änderungen*/
        mAdapter.notifyDataSetChanged();

    }
    //<--           prepareRatingData End            -->

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
