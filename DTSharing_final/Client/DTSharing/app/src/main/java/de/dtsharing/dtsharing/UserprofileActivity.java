package de.dtsharing.dtsharing;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private TextView mTitle, tvUserBirth, tvUserInterests, tvUserDescription, tvCountOfferer, tvCountPassenger, tvAverageRating, tvCountRating;
    private ImageView userAvatar, ivStar1, ivStar2, ivStar3, ivStar4, ivStar5;
    private ListView lvRatings;
    private FrameLayout noRatingsContainer;
    private RelativeLayout mainContent;

    private ArrayList<RatingsEntry> ratingsList = new ArrayList<>();
    private RatingsAdapter mAdapter = null;

    private String userId;

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
        tvUserBirth = (TextView) findViewById(R.id.tvUserBirth);
        tvUserInterests = (TextView) findViewById(R.id.tvUserInterests);
        tvUserDescription = (TextView) findViewById(R.id.tvUserDescription);
        tvCountOfferer = (TextView) findViewById(R.id.tvCountOfferer);
        tvCountPassenger = (TextView) findViewById(R.id.tvCountPassenger);
        tvCountRating = (TextView) findViewById(R.id.tvCountRatings);
        tvAverageRating = (TextView) findViewById(R.id.tvAverageRating);
        noRatingsContainer = (FrameLayout) findViewById(R.id.noRatingsContainer);
        mainContent = (RelativeLayout) findViewById(R.id.main_content);

        ivStar1 = (ImageView) findViewById(R.id.ivStar1);
        ivStar2 = (ImageView) findViewById(R.id.ivStar2);
        ivStar3 = (ImageView) findViewById(R.id.ivStar3);
        ivStar4 = (ImageView) findViewById(R.id.ivStar4);
        ivStar5 = (ImageView) findViewById(R.id.ivStar5);

        lvRatings = (ListView) findViewById(R.id.lvRatings);

        mainContent.setVisibility(View.GONE);

        /*Sichere die Empfangenen Daten in Variablen*/
        Intent userProfileIntent = getIntent();
        if (userProfileIntent != null) {
            userId = userProfileIntent.getStringExtra("userId");
        }

        getUserProfileData();
        getRatingData();
        //setupUserProfile(userProfilePicture, userName, userAge, userInterests, userDescription, userCountOfferer, userCountPassenger, 2.5, 2);

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new RatingsAdapter(getApplicationContext(), ratingsList);
        lvRatings.setAdapter(mAdapter);
    }

    public void getUserProfileData(){

        String base_url = getResources().getString(R.string.base_url);
        final String URI = base_url+"/users/"+userId;

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                ContentValues profileData = new ContentValues();
                try {
                    profileData.put("name",             response.getString("first_name")+" "+response.getString("last_name"));
                    profileData.put("birthYear",        response.getString("birth_year"));
                    profileData.put("gender",           response.getString("gender"));
                    profileData.put("interests",        response.getString("interests"));
                    profileData.put("more",             response.getString("more"));
                    profileData.put("picture",          response.getString("picture"));
                    profileData.put("countOfferer",     response.getInt("count_offerer"));
                    profileData.put("countPassenger",   response.getInt("count_passenger"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addUserProfileData(profileData);
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

    private void addUserProfileData(ContentValues profileData){

        if(profileData.getAsString("picture").equals("null")){
            int placeholder = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_account_circle_48dp", null, null);
            userAvatar.setImageResource(placeholder);
        }else{
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), EncodeDecodeBase64.decodeBase64(profileData.getAsString("picture")));
            roundDrawable.setCircular(true);
            userAvatar.setImageDrawable(roundDrawable);
        }

        mTitle              .setText(profileData.getAsString("name"));
        tvUserBirth         .setText(profileData.getAsString("birthYear"));
        tvUserInterests     .setText(profileData.getAsString("interests"));
        tvUserDescription   .setText(profileData.getAsString("more"));
        tvCountOfferer      .setText(profileData.getAsString("countOfferer"));
        tvCountPassenger    .setText(profileData.getAsString("countPassenger"));

        if(tvUserInterests.getText().equals("")) {
            tvUserInterests.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
            tvUserInterests.setText("Keine Angabe");
        }
        if(tvUserDescription.getText().equals("")) {
            tvUserDescription.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
            tvUserDescription.setText("Keine Angabe");
        }
    }

    private void getRatingData(){

        String base_url = getResources().getString(R.string.base_url);
        final String URI = base_url+"/users/"+userId+"/ratings";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                final ContentValues container = new ContentValues();

                Log.d("UserProfileActivity", "RATING RESPONSE: "+response);

                try {
                    JSONObject userData = response.getJSONObject("user_data");
                    JSONArray ratings = response.getJSONArray("ratings");

                    container.put("averageRating", userData.getDouble("average_rating"));
                    container.put("countRatings", ratings.length());

                    for(int i = 0; i < ratings.length(); i++){

                        JSONObject author = ratings.getJSONObject(i).getJSONObject("author"),
                                rating = ratings.getJSONObject(i).getJSONObject("rating");

                        String name = author.getString("first_name")+" "+author.getString("last_name"),
                                picture = author.getString("picture"),
                                date = rating.getString("date"),
                                message = rating.getString("comment");
                        int stars = rating.getInt("stars");

                        ratingsList.add(new RatingsEntry(name, date, message, picture, stars));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mainContent.setVisibility(View.VISIBLE);
                lvRatings.setVisibility(View.VISIBLE);
                tvAverageRating.setText(container.getAsString("averageRating"));
                tvCountRating.setText("("+container.getAsString("countRatings")+")");
                if(container.getAsDouble("averageRating") != 0)
                    setRating(container.getAsDouble("averageRating"));

                mAdapter.notifyDataSetChanged();

            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        switch (error.networkResponse.statusCode){
                            case 404:
                                mainContent.setVisibility(View.VISIBLE);
                                noRatingsContainer.setVisibility(View.VISIBLE);
                                tvAverageRating.setText("0");
                                tvCountRating.setText("(0)");
                                break;
                        }
                    }

                });

        Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);

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
