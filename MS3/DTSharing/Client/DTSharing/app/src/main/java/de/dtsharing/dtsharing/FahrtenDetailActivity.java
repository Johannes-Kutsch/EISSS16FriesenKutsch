package de.dtsharing.dtsharing;

import android.content.ContentValues;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

public class FahrtenDetailActivity extends AppCompatActivity {

    CoordinatorLayout _main_content;
    TableRow _tableRowDeparture2, _tableRowTarget1, _tableRowSpace2, _tableRowSpace3;
    TextView _departureTime1, _departureTime2, _arrivalTime1, _arrivalTime2, _departureStationName1, _departureStationName2,
            _targetStationName1, _targetStationName2, _departureUserName1, _departureUserName2,_targetUserName1, _targetUserName2,
            mTitle;
    String userId, dtTripId, base_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fahrten_detail);

        /* base_url und userID werden aus den SharedPrefs bezogen */
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(FahrtenDetailActivity.this);
        base_url = sharedPrefsManager.getBaseUrl();
        userId = sharedPrefsManager.getUserIdSharedPrefs();

        /* Toolbar Views werden gesetzt */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);

        /* Custom toolbar wird gesetzt */
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
            /*Zurück Button in der Titelleiste*/
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        /* Views werden erfasst */
        _main_content = (CoordinatorLayout) findViewById(R.id.main_content);
        _main_content.setVisibility(View.GONE);

        _tableRowDeparture2 = (TableRow) findViewById(R.id.trDeparture2);
        _tableRowTarget1 = (TableRow) findViewById(R.id.trTarget1);
        _tableRowSpace2 = (TableRow) findViewById(R.id.trSpace2);
        _tableRowSpace3 = (TableRow) findViewById(R.id.trSpace3);

        _departureTime1 = (TextView) findViewById(R.id.departureTime1);
        _departureTime2 = (TextView) findViewById(R.id.departureTime2);
        _arrivalTime1 = (TextView) findViewById(R.id.arrivalTime1);
        _arrivalTime2 = (TextView) findViewById(R.id.arrivalTime2);
        _departureStationName1 = (TextView) findViewById(R.id.departureStationName1);
        _departureStationName2 = (TextView) findViewById(R.id.departureStationName2);
        _targetStationName1 = (TextView) findViewById(R.id.targetStationName1);
        _targetStationName2 = (TextView) findViewById(R.id.targetStationName2);
        _departureUserName1 = (TextView) findViewById(R.id.departureUserName1);
        _departureUserName2 = (TextView) findViewById(R.id.departureUserName2);
        _targetUserName1 = (TextView) findViewById(R.id.targetUserName1);
        _targetUserName2 = (TextView) findViewById(R.id.targetUserName2);

        /* Extras des Intents werden bezogen */
        Intent mainIntent = getIntent();
        if(mainIntent != null){
            dtTripId = mainIntent.getStringExtra("dtTripId");
        }

        /* Die Trips Daten werden vom Server angefordert */
        getTripData(userId, dtTripId);
    }

    /* GET-Request an den Server. Ein JSONObject wird als Response erwartet. Bei einer erfolgreichen Response
     * werden die Daten den dafür vorgesehenen Views zugewiesen */
    private void getTripData(final String userId, final String dtTripId){

        final String URI = base_url+"/users/"+userId+"/dt_trips/"+dtTripId;

        Log.d("FahrtenDetailActivity", URI);

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject trip = response.getJSONObject("trip"),
                            user = response.getJSONObject("user"),
                            partner = response.getJSONObject("partner");

                    Log.d("FahrtenDetailActivity", "trip: "+trip.toString()+"\nuser: "+user.toString()+"\npartner: "+partner.toString());
                    mTitle.setText(trip.getString("route_name")+" - "+trip.getString("date"));


                    /* Das Layout wurde Tabellarisch aufgebaut, sodass nicht benötigte Spalten, da beide
                     * Teammitglieder denselben Start- und Zielbahnhof haben, oder für diese Fahrt noch kein
                     * Partner vorhanden ist, entfernt werden können.
                     * Die hier dargestellt Fallunterscheidung sorgt für die korrekte Darstellung der
                     * Detaillierten Fahrtübersicht und deckt jeden Fall ab */

                    /* Ist ein Partner für die Fahrt eingetragen */
                    if(!partner.getString("sequence_id_departure_station").equals("null")){

                        /* Beide Teilnehmer haben denselben Startbahnhof */
                        if(user.getInt("sequence_id_departure_station") == partner.getInt("sequence_id_departure_station")){

                            _tableRowDeparture2.setVisibility(View.GONE);
                            _tableRowSpace2.setVisibility(View.GONE);
                            _departureUserName1.setText(user.getString("first_name")+" steigt ein\n"+partner.getString("first_name")+" steigt ein");
                            _departureStationName1.setText(user.getString("departure_station_name"));
                            _departureTime1.setText(user.getString("departure_time"));

                        /* Der Benutzer steigt während der Fahrt zu */
                        } else if(user.getInt("sequence_id_departure_station") > partner.getInt("sequence_id_departure_station")){

                            _departureUserName1.setText(partner.getString("first_name")+" steigt ein");
                            _departureUserName2.setText(user.getString("first_name")+" steigt ein");
                            _departureStationName1.setText(partner.getString("departure_station_name"));
                            _departureStationName2.setText(user.getString("departure_station_name"));
                            _departureTime1.setText(partner.getString("departure_time"));
                            _departureTime2.setText(user.getString("departure_time"));

                        /* Der Partner steigt während der Fahrt zu */
                        } else {

                            _departureUserName1.setText(user.getString("first_name")+" steigt ein");
                            _departureUserName2.setText(partner.getString("first_name")+" steigt ein");
                            _departureStationName1.setText(user.getString("departure_station_name"));
                            _departureStationName2.setText(partner.getString("departure_station_name"));
                            _departureTime1.setText(user.getString("departure_time"));
                            _departureTime2.setText(partner.getString("departure_time"));

                        }

                        /* Beide Teilnehmer haben denselben Zielbahnhof */
                        if(user.getInt("sequence_id_target_station") == partner.getInt("sequence_id_target_station")){

                            _tableRowTarget1.setVisibility(View.GONE);
                            _tableRowSpace3.setVisibility(View.GONE);
                            _targetUserName2.setText(user.getString("first_name")+" steigt aus\n"+partner.getString("first_name")+" steigt aus");
                            _targetStationName2.setText(user.getString("target_station_name"));
                            _arrivalTime2.setText(user.getString("arrival_time"));

                        /* Der Partner steigt früher aus */
                        } else if(user.getInt("sequence_id_target_station") > partner.getInt("sequence_id_target_station")){

                            _targetUserName1.setText(partner.getString("first_name")+" steigt aus");
                            _targetUserName2.setText(user.getString("first_name")+" steigt aus");
                            _targetStationName1.setText(partner.getString("target_station_name"));
                            _targetStationName2.setText(user.getString("target_station_name"));
                            _arrivalTime1.setText(partner.getString("arrival_time"));
                            _arrivalTime2.setText(user.getString("arrival_time"));

                        /* Der Benutzer steigt früher aus */
                        } else if(user.getInt("sequence_id_target_station") < partner.getInt("sequence_id_target_station")){

                            _targetUserName1.setText(user.getString("first_name")+" steigt aus");
                            _targetUserName2.setText(partner.getString("first_name")+" steigt aus");
                            _targetStationName1.setText(user.getString("target_station_name"));
                            _targetStationName2.setText(partner.getString("target_station_name"));
                            _arrivalTime1.setText(user.getString("arrival_time"));
                            _arrivalTime2.setText(partner.getString("arrival_time"));

                        }

                    /* Kein Partner für die Fahrt eingetragen */
                    } else {

                        _tableRowDeparture2.setVisibility(View.GONE);
                        _tableRowSpace2.setVisibility(View.GONE);
                        _departureUserName1.setText(user.getString("first_name")+" steigt ein");
                        _departureStationName1.setText(user.getString("departure_station_name"));
                        _departureTime1.setText(user.getString("departure_time"));

                        _tableRowTarget1.setVisibility(View.GONE);
                        _tableRowSpace3.setVisibility(View.GONE);
                        _targetUserName2.setText(user.getString("first_name")+" steigt aus");
                        _targetStationName2.setText(user.getString("target_station_name"));
                        _arrivalTime2.setText(user.getString("arrival_time"));

                    }

                    /* Wurden die Fallunterscheidungen abgeschlossen mache die View sichtbar */
                    _main_content.setVisibility(View.VISIBLE);

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
