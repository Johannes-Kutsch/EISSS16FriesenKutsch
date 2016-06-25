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

    /* Anhand der gegebenen Latitude und Longitude Parameter werden die stops gefiltert und abschließend
     * alle stops nach nach Distanz sortiert und in einem Umkreis 2 km befindend ausgegeben */
    public String[] startRadiusSearch(double lat, double lon){

        /* Alle stops werden aus der Lokalen Datenbank ausgelesen. Diese enthalten stop_name, latitude und longitude */
        SQLiteDatabase db;
        db = mContext.openOrCreateDatabase("Stops", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT * FROM vrs", null);

        /* Es wird eine ArrayListe erzeugt, welche im weiteren Verlauf mit Daten von stops im Umkreis gefüllt werden. Zugehörig
         * zum Namen enthalten die Einträge die Distanz */
        ArrayList<StopsEntry> stopsInRadius = new ArrayList<>();

        /* Abschließend wird die erste Liste aufsteigend sortiert und die Namen der stops in die finale Liste gepusht */
        List<String> stopNamesOnly = new ArrayList<>();

        /* Wenn stops gefunden wurden */
        if(cursor.getCount() > 0){

            cursor.moveToFirst();

            /* Solange Einträge vorhanden sind vergleiche position der stops mit der position des Benutzers und pushe nur
             * Einträge in die erste Liste, welche in einem Umkreis von 2 km liegen */
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

            /* Abschließend schließe die Verbindungen um Datenlecks zu vermeiden */
            cursor.close();
            db.close();

            /* Da die Liste sowohl aus einem String als auch einem int besteht wird diese über einen
             * Comparator sortiert. Als Key wird die Distanz gewählt und die Einträge werden der Distanz
             * aufsteigend sortiert*/
            Collections.sort(stopsInRadius, new Comparator<StopsEntry>() {
                @Override
                public int compare(StopsEntry stop1, StopsEntry stop2) {
                    int distance1 = ((StopsEntry) stop1).getDistance(),
                            distance2 = ((StopsEntry) stop2).getDistance();
                    return distance1 - distance2;
                }
            });

            /* Abschließend pushe nur die Namen in die dafür vorgesehene Liste */
            for (StopsEntry stop : stopsInRadius){
                stopNamesOnly.add(stop.getStopName());
            }
        }

        /* Der ProgressDialog fordert als Datenquelle ein Array. Somit wird die Liste in ein Array umgewandelt */
        String[] array = new String[stopNamesOnly.size()];
        stopNamesOnly.toArray(array);
        return array;
    }

    /* CustomEntry für die ArrayListe um String (stop_name) und Int (Distanz) zu speichern */
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
