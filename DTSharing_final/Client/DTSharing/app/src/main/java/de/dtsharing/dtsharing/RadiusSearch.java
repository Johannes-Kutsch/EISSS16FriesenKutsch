package de.dtsharing.dtsharing;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RadiusSearch {

    Context mContext;

    public RadiusSearch(Context context){
        this.mContext = context;
    }

    public String[] startRadiusSearch(double lat, double lon){

        SQLiteDatabase db;
        db = mContext.openOrCreateDatabase("Stops", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT * FROM vrs", null);

        ArrayList<StopsEntry> stopsInRadius = new ArrayList<>();
        List<String> stopNamesOnly = new ArrayList<>();

        if(cursor.getCount() > 0){

            cursor.moveToFirst();

            while (cursor.moveToNext()){

                String stopName = cursor.getString(0);
                double stop_lat = cursor.getDouble(1),
                        stop_lon = cursor.getDouble(2);

                Location user_location = new Location("User");
                user_location.setLatitude(lat);
                user_location.setLongitude(lon);

                Location stop_location = new Location("Stop");
                stop_location.setLatitude(stop_lat);
                stop_location.setLongitude(stop_lon);

                int distance = Math.round(user_location.distanceTo(stop_location));

                if(distance <= 2000) {
                    stopsInRadius.add(new StopsEntry(stopName, distance));
                }
            }
            cursor.close();
            db.close();

            Collections.sort(stopsInRadius, new Comparator<StopsEntry>() {
                @Override
                public int compare(StopsEntry stop1, StopsEntry stop2) {
                    int distance1 = ((StopsEntry) stop1).getDistance(),
                            distance2 = ((StopsEntry) stop2).getDistance();
                    return distance1 - distance2;
                }
            });

            for (StopsEntry stop : stopsInRadius){
                stopNamesOnly.add(stop.getStopName());
            }
        }
        String[] array = new String[stopNamesOnly.size()];
        stopNamesOnly.toArray(array);
        return array;
    }

    public class StopsEntry {
        public String stopName;
        public int distance;

        public StopsEntry(String stopName, int distance){
            this.stopName = stopName;
            this.distance = distance;
        }

        public int getDistance(){
            return distance;
        }

        public String getStopName(){
            return stopName;
        }
    }

}
