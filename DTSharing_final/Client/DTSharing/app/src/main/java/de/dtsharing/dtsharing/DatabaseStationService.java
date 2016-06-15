package de.dtsharing.dtsharing;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import de.dtsharing.dtsharing.LoginActivity.MyStationStatusReceiver;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DatabaseStationService extends IntentService {

    private static final String LOG_TAG = DatabaseStationService.class.getSimpleName();
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public DatabaseStationService() {
        super("DatabaseStationService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        setupStopsDatabase();
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

                if(response.length() == 0) {
                    Log.d(LOG_TAG, "Stations sind bereits up-to-date");
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(MyStationStatusReceiver.STATUS_RESPONSE);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra("finished", true);
                    sendBroadcast(broadcastIntent);
                } else {
                    insertStationData(response);
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(MyStationStatusReceiver.STATUS_RESPONSE);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra("finished", true);
                    sendBroadcast(broadcastIntent);
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
        return true;

    }

    public int getStopsVersion(){
        //Lese aktuelle stops_version aus, falls keine vorhanden 0
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("stops_version", 0);
    }

    public void setStopsVersion(int stops_version){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt("stops_version", stops_version);
        editor.apply();
    }

    private void insertStationData(JSONObject response){
        try {

            final JSONArray stops = response.getJSONArray("stops");

            Log.d(LOG_TAG, stops.length()+" Stationen müssen in die Datenbank eingefügt werden");

            Thread myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase db;
                    db = openOrCreateDatabase("Stops", Context.MODE_PRIVATE, null);
                    db.delete("vrs", null, null); /*Leere Table, damit keine duplikate*/

                    String stop_name = null;
                    double stop_lat = 0, stop_lon = 0;

                    for (int i = 0; i < stops.length(); i++) {

                        try {
                            stop_name = stops.getJSONObject(i).getString("stop_name");
                            stop_lat = stops.getJSONObject(i).getDouble("stop_lat");
                            stop_lon = stops.getJSONObject(i).getDouble("stop_lon");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ContentValues values = new ContentValues();
                        values.put("stop_name", stop_name);
                        values.put("stop_lat", stop_lat);
                        values.put("stop_lon", stop_lon);

                        db.insert("vrs", null, values);

                        //db.execSQL("INSERT INTO vrs VALUES('"+stop_name+"','"+stop_lat+"','"+stop_lon+"');");
                    }

                    db.close();
                }
            });
            myThread.start();

            Log.d(LOG_TAG, "Stationen wurden in die Datenbank eingetragen");
            Log.d(LOG_TAG, "Die Stops Version wird aktualisiert. Alt: "+getStopsVersion()+" Neu: "+response.getInt("stops_version"));
            setStopsVersion(response.getInt("stops_version"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}