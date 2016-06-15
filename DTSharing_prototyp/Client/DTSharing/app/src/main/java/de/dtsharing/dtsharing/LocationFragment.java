package de.dtsharing.dtsharing;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment {

    Button bLocation;
    TextView tvLocation, tvLocationName;
    private String url;

    String urlBase = "http://10.0.2.2:3000/";
    //String urlBase = "http://192.168.0.15:3000/";

    int fine_location_permission, coarse_location_permission;

    public LocationFragment() {
        // Required empty public constructor
    }


    //<--           OnCreateView Start          -->
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_location Layout*/
        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.fragment_location, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        bLocation = (Button) v.findViewById(R.id.bLocation);
        tvLocation = (TextView) v.findViewById(R.id.tvLocation);
        tvLocationName = (TextView) v.findViewById(R.id.tvLocationName);

        /*getGPS Button onClick Listener*/
        bLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.bLocation:
                        /*Falls Button gedrückt, wird getGPS() aufgerufen und das double Array ins TextView geschrieben
                        * Zusätzlich wird getLocationName() aufgerufen, womit die Haltestellen in der nähe ermittelt werden*/
                        double[] gpsData = getGPS();
                        tvLocation.setText("Latitude: "+String.format("%1$,.6f", gpsData[0])+"\nLongitude: "+String.format("%1$,.6f", gpsData[1]));
                        getLocationName(gpsData[0], gpsData[1]);
                }
            }
        });

        return v;
    }
    //<--           OnCreateView End            -->

    //<--           checkPermission Start           -->
    private void checkPermission(){
        /*Überprüfe ob benötigte Rechte genehmigt wurden*/
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            }else{
                /*Rechte nicht genehmigt -> Anfordern*/
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        fine_location_permission);
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            }else{
                /*Rechte nicht genehmigt -> Anfordern*/
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        coarse_location_permission);
            }
        }
    }
    //<--           checkPermission End             -->
    /*http://stackoverflow.com/questions/4735942/android-get-location-only-one-time*/
    private double[] getGPS() {
        LocationManager lm = (LocationManager) getActivity().getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location l = null;

        /*Durchlaufe die Provider Schleife und wenn eine Akkurate Position ermittelt wurde unterbrechen*/
        for (int i = providers.size() - 1; i >= 0; i--) {
            /*Überprüfe ob die für GPS benötigten Rechte genehmigt wurden*/
            checkPermission();
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        double[] gps = new double[2];
        if (l != null) {
            /*Wenn eine Akkurate Position ermittelt werden konnte speichere Lat und Lon*/
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }else{
            /*Fixe GPS Daten, da der Emulator Probleme mit dem ermitteln der Position hat
            * Auf dem Smartphone funktioniert es tadellos*/
            gps[0] = 51.025473;
            gps[1] = 7.564739;
        }
        return gps;
    }

    //<--           getLocationName Start           -->
    public void getLocationName(double latitude, double longitude) {

        url = urlBase+"get/stations/nearby/"+latitude+"/"+longitude;
        /*Resete TextView*/
        tvLocationName.setText("");

        /*Get Request an den Server*/
        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                try {
                    /*Quick and Dirty Lösung zum eintragen der gefundenen Haltestellen ins TextView*/
                    for(int i=0;i<response.length();i++){
                        JSONObject json_data = response.getJSONObject(i);
                        tvLocationName.setText(tvLocationName.getText()+"\n"+json_data.getString("stop_name"));
                    }
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
                }
        );
        Volley.newRequestQueue(getActivity().getApplicationContext()).add(jsonRequest);
    }
    //<--           getLocationName End             -->
}