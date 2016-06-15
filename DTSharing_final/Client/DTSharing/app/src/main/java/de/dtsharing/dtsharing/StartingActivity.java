package de.dtsharing.dtsharing;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StartingActivity extends AppCompatActivity {

    private static final String LOG_TAG = StartingActivity.class.getSimpleName();
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    boolean authenticated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authenticated = false;

        new CheckStationDataAsyncTask().execute((Void) null);

        if(authenticated){
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainIntent);
            finish();
        }else{
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    class CheckStationDataAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private void setupDatabase(){

            Log.d(LOG_TAG, "Datenbanken werden erstellt, sofern noch nicht vorhanden");

            SQLiteDatabase db;

            db = openOrCreateDatabase("Stops", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS vrs(stop_name VARCHAR(255),stop_lat NUMERIC,stop_lon NUMERIC);");
            db.close();

            db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS history(departure_station_name VARCHAR(255),target_station_name VARCHAR(255),date VARCHAR(255),rating NUMERIC,last_calculated VARCHAR(255));");
            db.execSQL("CREATE TABLE IF NOT EXISTS chats(chat_id VARCHAR(255),user_one_id VARCHAR(255),user_two_id VARCHAR(255),key VARCHAR(255));");
            db.execSQL("CREATE TABLE IF NOT EXISTS messages(message_id VARCHAR(255),chat_id VARCHAR(255),author_id VARCHAR(255),message_text VARCHAR(255));");
            db.execSQL("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR(255),picture_version NUMERIC,picture VARCHAR(255),birth_year VARCHAR(5),first_name VARCHAR(255),last_name VARCHAR(255),gender VARCHAR(10),interests VARCHAR(255),more VARCHAR(255));");
            db.close();

        }

        public boolean setupStopsDatabase(){

            String uri = Uri.parse("http://192.168.0.15:3000/stops")
                    .buildUpon()
                    .appendQueryParameter("stops_version", Integer.toString(getStopsVersion()))
                    .build().toString();

            final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                    Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    if(response.length() == 0)
                        Log.d(LOG_TAG, "Stations sind bereits up-to-date");

                    else insertStationData(response);

                }

            },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }

                    });

            Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
            return true;

        }

        public int getStopsVersion(){
            //Lese aktuelle stops_version aus, falls keine vorhanden 0
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            return prefs.getInt("stops_version", 0);
        }

        public void setStopsVersion(int stops_version){
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putInt("stops_version", stops_version);
            editor.apply();
        }

        private void insertStationData(JSONObject response){
            try {

                JSONArray stops = response.getJSONArray("stops");

                Log.d(LOG_TAG, stops.length()+" Stationen müssen in die Datenbank eingefügt werden");

                SQLiteDatabase db;
                db = openOrCreateDatabase("Stops", Context.MODE_PRIVATE, null);
                db.delete("vrs", null, null); /*Leere Table, damit keine duplikate*/

                for (int i = 0; i < stops.length(); i++) {

                    String stop_name = stops.getJSONObject(i).getString("stop_name");
                    double stop_lat = stops.getJSONObject(i).getDouble("stop_lat"),
                            stop_lon = stops.getJSONObject(i).getDouble("stop_lon");

                    ContentValues values = new ContentValues();
                    values.put("stop_name", stop_name);
                    values.put("stop_lat", stop_lat);
                    values.put("stop_lon", stop_lon);

                    db.insert("vrs", null, values);

                    //db.execSQL("INSERT INTO vrs VALUES('"+stop_name+"','"+stop_lat+"','"+stop_lon+"');");
                }

                db.close();

                Log.d(LOG_TAG, "Stationen wurden in die Datenbank eingetragen");
                Log.d(LOG_TAG, "Die Stops Version wird aktualisiert. Alt: "+getStopsVersion()+" Neu: "+response.getInt("stops_version"));
                setStopsVersion(response.getInt("stops_version"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            setupDatabase();
            setupStopsDatabase();
            return null;
        }
    }

}
