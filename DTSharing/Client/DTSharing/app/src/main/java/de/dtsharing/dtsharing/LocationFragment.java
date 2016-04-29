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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.fragment_location, container, false);

        bLocation = (Button) v.findViewById(R.id.bLocation);
        tvLocation = (TextView) v.findViewById(R.id.tvLocation);
        tvLocationName = (TextView) v.findViewById(R.id.tvLocationName);

        bLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.bLocation:
                        double[] gpsData = getGPS();
                        tvLocation.setText("Latitude: "+String.format("%1$,.6f", gpsData[0])+"\nLongitude: "+String.format("%1$,.6f", gpsData[1]));
                        getLocationName(gpsData[0], gpsData[1]);
                }
            }
        });

        return v;
    }

    private void checkPermission(){
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            }else{
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        fine_location_permission);
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            }else{
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        coarse_location_permission);
            }
        }
    }

    /*http://stackoverflow.com/questions/4735942/android-get-location-only-one-time*/
    private double[] getGPS() {
        LocationManager lm = (LocationManager) getActivity().getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            checkPermission();
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

    public void getLocationName(double latitude, double longitude) {
        url = urlBase+"get/stations/nearby/"+latitude+"/"+longitude;
        tvLocationName.setText("");

        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                try {
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
}