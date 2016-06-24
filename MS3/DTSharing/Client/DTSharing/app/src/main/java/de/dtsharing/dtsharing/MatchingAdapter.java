package de.dtsharing.dtsharing;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MatchingAdapter extends BaseAdapter{

    private ArrayList<MatchingEntry> matches;
    private ContentValues enteredTripDetails;
    private Context context_1;
    private AesCbcWithIntegrity.SecretKeys key;
    private String base_url;

    public class ViewHolder {
        public TextView userName, departureTime, departureName, targetTime, targetName;
        public Button matchingButton;
        public ImageView picture, star1, star2, star3, star4, star5;
        public RelativeLayout starsContainer;
        public FrameLayout noRatingContainer;

    }

    public MatchingAdapter(Context context, ArrayList<MatchingEntry> matches, ContentValues enteredTripDetails) {
        this.context_1 = context;
        this.matches = matches;
        this.enteredTripDetails = enteredTripDetails;
    }

    /* Gibt die größe der ArrayList aus */
    @Override
    public int getCount() {
        return matches.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /* in getView werden die Views erfasst, dem viewHolder zugewiesen und abschließend mit den Daten der ArrayListe trips
     * angereichert. Die Operationen eines Adapters gelten für jedes Item welches diesem über die ArrayListe hinzugefügt wurde */
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context_1).inflate(R.layout.matching_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.departureTime = (TextView) convertView.findViewById(R.id.tvDepartureTime);
            viewHolder.departureName = (TextView) convertView.findViewById(R.id.tvDepartureName);
            viewHolder.targetTime = (TextView) convertView.findViewById(R.id.tvTargetTime);
            viewHolder.targetName = (TextView) convertView.findViewById(R.id.tvTargetName);
            viewHolder.matchingButton = (Button) convertView.findViewById(R.id.bMatching);
            viewHolder.picture = (ImageView) convertView.findViewById(R.id.ivAvatar);
            viewHolder.starsContainer = (RelativeLayout) convertView.findViewById(R.id.rlStarsContainer);
            viewHolder.noRatingContainer = (FrameLayout) convertView.findViewById(R.id.flNoRatingContainer);
            viewHolder.star1 = (ImageView) convertView.findViewById(R.id.ivStar1);
            viewHolder.star2 = (ImageView) convertView.findViewById(R.id.ivStar2);
            viewHolder.star3 = (ImageView) convertView.findViewById(R.id.ivStar3);
            viewHolder.star4 = (ImageView) convertView.findViewById(R.id.ivStar4);
            viewHolder.star5 = (ImageView) convertView.findViewById(R.id.ivStar5);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final MatchingEntry matchesEntry = matches.get(position);

        viewHolder.userName.setText(matchesEntry.getUserName());
        viewHolder.departureTime.setText(matchesEntry.getDepartureTime());
        viewHolder.departureName.setText(matchesEntry.getDepartureName());
        viewHolder.targetTime.setText(matchesEntry.getTargetTime());
        viewHolder.targetName.setText(matchesEntry.getTargetName());

        if(matchesEntry.getAverageRating() == 0){
            viewHolder.noRatingContainer.setVisibility(View.VISIBLE);
        }else{
            viewHolder.starsContainer.setVisibility(View.VISIBLE);
            setRating(matchesEntry.getAverageRating(), viewHolder);
        }

        Log.d("MatchingAdapter", matchesEntry.getPicture());
        if(matchesEntry.getPicture().equals("null")) {
            Log.d("MatchingAdapter", "PICTURE IS NULL");
            int placeholder = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_account_circle_48dp", null, null);
            viewHolder.picture.setImageResource(placeholder);
        }else {
            Log.d("MatchingAdapter", "PICTURE IS NOT NULL");
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context_1.getResources(), EncodeDecodeBase64.decodeBase64(matchesEntry.getPicture()));
            roundDrawable.setCircular(true);
            viewHolder.picture.setImageDrawable(roundDrawable);
        }

        viewHolder.matchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                String message, positiveButton;
                if(enteredTripDetails.getAsBoolean("hasTicket")) {
                    message = "Möchtest du wirklich " + matchesEntry.getUserName() + " mitnehmen?";
                    positiveButton = "Mitnehmen";
                }else {
                    message = "Möchtest du wirklich bei " + matchesEntry.getUserName() + " mitfahren?";
                    positiveButton ="Mitfahren";
                }

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(parent.getContext(), R.style.AppTheme_Dialog_Alert);

                builder.setMessage(message);
                builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            key = AesCbcWithIntegrity.generateKey();
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                        }

                        commitMatch(matchesEntry, AesCbcWithIntegrity.keyString(key));
                    }

                });

                builder.setNegativeButton("Abbruch", null);
                builder.show();
            }
        });

        return convertView;
    }

    private void commitMatch(final MatchingEntry matchData, final String key){

        base_url = new SharedPrefsManager(context_1).getBaseUrl();
        String URI = base_url+"/users/"+matchData.getOwnerUserId()+"/dt_trips/"+matchData.getDtTripId();
        final String userID = new SharedPrefsManager(context_1).getUserIdSharedPrefs();

        Log.d("MatchingAdapter", URI);

        StringRequest postRequest = new StringRequest(Request.Method.PUT, URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("MatchingActivity", "commitMatch RESPONSE: "+response);

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.has("success_message")) {

                                String chatID = jsonObject.getString("chat_id");

                                if(enteredTripDetails.containsKey("dtTripId")) {
                                    deleteOwnTrip(enteredTripDetails.getAsString("dtTripId"), userID, chatID, matchData, key);
                                } else {
                                    SQLiteDatabase db;
                                    db = context_1.openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

                                    ContentValues values = new ContentValues();
                                    values.put("chat_id", chatID);
                                    values.put("key", key);

                                    db.insert("chats", null, values);
                                    db.close();

                                    Intent mainIntent = new Intent(context_1, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mainIntent.putExtra("matching_success", true);
                                    mainIntent.putExtra("matchName", matchData.getUserName());
                                    context_1.startActivity(mainIntent);
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        })
        {
            /*Daten welche der Post-Request mitgegeben werden*/
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                //params.put("interests", _interests.getText().toString());
                params.put("user_id", userID);
                params.put("departure_time", enteredTripDetails.getAsString("departureTime"));
                params.put("arrival_time", enteredTripDetails.getAsString("arrivalTime"));
                params.put("sequence_id_departure_station", enteredTripDetails.getAsString("departureSequenceId"));
                params.put("sequence_id_target_station", enteredTripDetails.getAsString("targetSequenceId"));
                params.put("departure_station_name", enteredTripDetails.getAsString("departureName"));
                params.put("target_station_name", enteredTripDetails.getAsString("targetName"));
                params.put("key", key);
                Log.d("MatchingAdapter", "Params: "+params);
                return params;
            }

        };

        Volley.newRequestQueue(context_1).add(postRequest);

    }

    private void deleteOwnTrip(String dtTripId, String userId, final String chatId, final MatchingEntry matchData, final String key){

        String URI = base_url+"/users/"+userId+"/dt_trips/"+dtTripId;

        Log.d("MatchingAdapter", URI);

        StringRequest postRequest = new StringRequest(Request.Method.DELETE, URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("MatchingActivity", "deleteOwnTrip RESPONSE: "+response);

                        if (response.contains("success_message")){

                            SQLiteDatabase db;
                            db = context_1.openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

                            ContentValues values = new ContentValues();
                            values.put("chat_id", chatId);
                            values.put("key", key);

                            db.insert("chats", null, values);
                            db.close();

                            Intent mainIntent = new Intent(context_1, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mainIntent.putExtra("matching_success", true);
                            mainIntent.putExtra("matchName", matchData.getUserName());
                            context_1.startActivity(mainIntent);

                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        });

        Volley.newRequestQueue(context_1).add(postRequest);

    }

    private void setRating(final double rating, ViewHolder viewHolder){

        /*Default = starBorder => Somit muss dieser Stern nicht zugewiesen werden*/
        int starFull = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_full_24dp", null, null);
        int starHalf = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_half_24dp", null, null);

        int wholeRating = (int) rating;
        double fractionalRating = rating - wholeRating;

        switch (wholeRating){
            case 5:
                viewHolder.star5.setImageResource(starFull);
            case 4:
                viewHolder.star4.setImageResource(starFull);
            case 3:
                viewHolder.star3.setImageResource(starFull);
            case 2:
                viewHolder.star2.setImageResource(starFull);
            case 1:
                viewHolder.star1.setImageResource(starFull);
        }


        if(fractionalRating > 0.25 && fractionalRating < 0.75){
            switch (wholeRating+1){
                case 1:
                    viewHolder.star1.setImageResource(starHalf);
                    break;
                case 2:
                    viewHolder.star2.setImageResource(starHalf);
                    break;
                case 3:
                    viewHolder.star3.setImageResource(starHalf);
                    break;
                case 4:
                    viewHolder.star4.setImageResource(starHalf);
                    break;
                case 5:
                    viewHolder.star5.setImageResource(starHalf);
                    break;
            }
        }else if(fractionalRating >= 0.75){
            switch (wholeRating+1){
                case 1:
                    viewHolder.star1.setImageResource(starFull);
                    break;
                case 2:
                    viewHolder.star2.setImageResource(starFull);
                    break;
                case 3:
                    viewHolder.star3.setImageResource(starFull);
                    break;
                case 4:
                    viewHolder.star4.setImageResource(starFull);
                    break;
                case 5:
                    viewHolder.star5.setImageResource(starFull);
                    break;
            }
        }
    }
}
