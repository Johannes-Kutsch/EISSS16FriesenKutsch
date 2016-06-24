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

/* Bei dem DatabaseStationService handelt es sich um einen IntentService, welcher beim Login gestartet wird und anhand einer stops_version
 * als Query Parameter beim GET auf die Ressource /stops die eigene Version mitteilt. Unterscheiden sich die Versionen wird die Lokale
 * Datenbank gedroppt und mit den Daten der Response erneut aufgefüllt. Desweiteren wird die Versionsnummer angehoben. Sind die stops_versionen
 * identisch werden keine stops Daten in der Response mitgeschickt
 *
 * Der Clientseitige Zugriff auf die stops verringert die Zugriffszeit und gewährt die Nutzung des AutoCompletes bei der Eingabe der Reisedaten sowie
 * die Nutzung der Umkreissuche auch ohne eine intakte Internetverbindung. Desweiteren wird dadurch traffic gespart, da die stops sich nicht allzu häufig
 * ändern sollten */

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


        /* Die base_url wird aus den SharedPrefs bezogen */
        String base_url = new SharedPrefsManager(DatabaseStationService.this).getBaseUrl();

        /* Es wird eine URI mithilfe des URI Parsers erzeugt, welche mit dem Query Parameter "stops_version" angereichert wird */
        String uri = Uri.parse(base_url+"/stops")
                .buildUpon()
                .appendQueryParameter("stops_version", Integer.toString(getStopsVersion()))
                .build().toString();

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                /* Enthält die Response keine Daten sind die Stops bereits up-to-date und müssen nicht aktualisiert / bezogen werden
                 * Über einen Broadcast wird der aufrufenden Aktivität mitgeteilt, dass das GET auf stops abgeschlossen wurde und somit
                 * der ProgressDialog abgeschlossen werden kann */
                if(response.length() == 0) {
                    Log.d(LOG_TAG, "Stations sind bereits up-to-date");
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(MyStationStatusReceiver.STATUS_RESPONSE);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra("finished", true);
                    sendBroadcast(broadcastIntent);

                /* Andernfalls werden die Daten in die lokale Datenbank übertragen. Abschließend wird der aufrufenden Aktivität durch
                 * einen Broadcast mitgeteilt, dass das GET auf stops abgeschlossen wurde und somit der ProgressDialog abgeschlossen werden kann */
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

                        /* Bei einem Fehler wird die aufrufende Aktivität ebenfalls über einen Broadcast darauf aufmerksam gemacht, sodass der blockierende
                         * ProgressDialog abgeschlossen und auf den Fehler aufmerksam gemacht werden kann */
                        error.printStackTrace();
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(MyStationStatusReceiver.STATUS_RESPONSE);
                        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        broadcastIntent.putExtra("error", true);
                        sendBroadcast(broadcastIntent);
                    }

                });

        /* Request wird in die Volley Queue eingereiht und ausgeführt */
        Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
        return true;

    }

    /* Die stops_version wird aus den SharedPrefs bezogen */
    public int getStopsVersion(){
        //Lese aktuelle stops_version aus, falls keine vorhanden 0
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("stops_version", 0);
    }

    /* Die stops_version wird aktualisiert / gesetzt */
    public void setStopsVersion(int stops_version){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt("stops_version", stops_version);
        editor.apply();
    }

    /* Falls stops eingefügt / aktualisiert werden müssen wird die Methode inserStationData ausgeführt */
    private void insertStationData(JSONObject response){
        try {

            final JSONArray stops = response.getJSONArray("stops");

            Log.d(LOG_TAG, stops.length()+" Stationen müssen in die Datenbank eingefügt werden");

            /* es wird ein Worker Thread gestartet, da das einfügen von ~7 Tausend stops eine sehr intensive
             * Aufgabe ist und somit das UI für ungefähr 30 Sekunden blockiert würde. Diesem wird somit entgegen
             * gewirkt und das Einfügen der stops dauert nur noch maximal 2 Sekunden */
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

                    }

                    db.close();
                }
            });
            myThread.start();

            Log.d(LOG_TAG, "Stationen wurden in die Datenbank eingetragen");
            Log.d(LOG_TAG, "Die Stops Version wird aktualisiert. Alt: "+getStopsVersion()+" Neu: "+response.getInt("stops_version"));

            /* Die stops_version wird auf die des Servers gesetzt */
            setStopsVersion(response.getInt("stops_version"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}