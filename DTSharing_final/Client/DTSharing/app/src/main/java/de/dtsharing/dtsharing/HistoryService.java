package de.dtsharing.dtsharing;

import android.app.IntentService;
import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HistoryService extends IntentService {
    private static final String ACTION_AddToHistory = "de.dtsharing.dtsharing.action.AddToHistory";
    private static final String ACTION_DeleteHistory = "de.dtsharing.dtsharing.action.DeleteHistory";

    private static final String EXTRA_DepartureStationName = "de.dtsharing.dtsharing.extra.DepartureStationName";
    private static final String EXTRA_TargetStationName = "de.dtsharing.dtsharing.extra.TargetStationName";

    public HistoryService() {
        super("HistoryService");
    }

    /* Methode um die Aktion AddToHistory zu starten */
    public static void startActionAddToHistory(Context context, String departureStationName, String targetStationName) {
        Intent intent = new Intent(context, HistoryService.class);
        intent.setAction(ACTION_AddToHistory);
        intent.putExtra(EXTRA_DepartureStationName, departureStationName);
        intent.putExtra(EXTRA_TargetStationName, targetStationName);
        context.startService(intent);
    }

    /* Methode um das Löschen der History einzuleiten. Wird nicht verwendet und ist nur für Entwicklungszwecke
     * implementiert, damit man zum Löschen des Verlaufs nicht alle App Daten löschen muss */
    public static void startActionDeleteHistory(Context context) {
        Intent intent = new Intent(context, HistoryService.class);
        intent.setAction(ACTION_DeleteHistory);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_AddToHistory.equals(action)) {
                final String departureStationName = intent.getStringExtra(EXTRA_DepartureStationName);
                final String targetStationName = intent.getStringExtra(EXTRA_TargetStationName);
                handleActionAddToHistory(departureStationName, targetStationName);
                handleActionRecalculateHistory();
            } else if (ACTION_DeleteHistory.equals(action)){
                handleActionDeleteHistory();
            }
        }
    }

    private void handleActionDeleteHistory() {

        SQLiteDatabase db;
        db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);
        db.delete("history", null, null);

    }

    private void handleActionRecalculateHistory(){

        /* Es wird das aktuelle Datum bezogen und in ein bestimmtes Format gebracht */
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY);
        final String currentDate = df.format(c.getTime());

        /* Die Lokale Datenbank wird geöffnet */
        SQLiteDatabase db;
        db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

        /* Es wird ein Query gestartet, welcher alle Daten in history ausgeben soll */
        Cursor cursor = db.rawQuery("SELECT * FROM history", null);

        /* Es sind Einträge vorhanden */
        if(cursor.getCount() > 0){

            cursor.moveToFirst();

            /* Gehe alle Einträge durch */
            while (cursor.moveToNext()){

                /* Auslesen der Daten des derzeitigen Eintrags */
                String departureStationName = cursor.getString(0),
                        targetStationName = cursor.getString(1),
                        last_calculated = cursor.getString(3);
                double rating = cursor.getDouble(2);

                /* Ermittlung der Anzahl an Tagen die zwischen dem aktuellen Datum und dem last_calculated Datum liegen */
                int daysBetween = daysBetween(stringToDate(last_calculated), stringToDate(currentDate));

                if (daysBetween > 0) {

                    /* Verringere das Rating um 7,5% pro Tag */
                    rating = rating * Math.pow(0.925, daysBetween);

                    ContentValues values = new ContentValues();
                    values.put("rating", rating);
                    values.put("last_calculated", currentDate);

                    /* Update des Eintrages */
                    db.update("history", values, "departure_station_name=? AND target_station_name= ?", new String[] {departureStationName, targetStationName});

                }

            }

        }

        /* Schlie0en des Cursors + Datenbank */
        cursor.close();
        db.close();

    }

    private void handleActionAddToHistory(String departureStationName, String targetStationName) {

        /* Ermittlung des aktuellen Datum in einem bestimmten Format */
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY);
        final String currentDate = df.format(c.getTime());

        /* Öffnen der Lokalen Datenbank */
        SQLiteDatabase db;
        db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

        /* Query, welches überprüft ob die Eingaben bereits in der Datenbank vorhanden sind */
        Cursor cursor = db.rawQuery("SELECT * FROM history WHERE departure_station_name=? AND target_station_name=?", new String[] {departureStationName, targetStationName});

        /* Eintrag bereits vorhanden */
        if(cursor.moveToFirst()){

            /* Auslesen der Werte des Eintrages */
            String last_calculated = cursor.getString(3);
            double rating = cursor.getDouble(2);
            int count = cursor.getInt(4);

            /* Rating wird um eins erhöhr */
            rating++;

            ContentValues values = new ContentValues();
            values.put("rating", rating);
            values.put("last_calculated", currentDate);
            values.put("count", ++count);

            /* Eintrag wird aktualisiert */
            db.update("history", values, "departure_station_name=? AND target_station_name= ?", new String[] {departureStationName, targetStationName});

            Log.d("SuchmaskeFragment", values.toString());

        /* Eintrag noch nicht vorhanden */
        }else{

            /* Ein neuer Eintrag wird erzeugt: Folgende Standardwerte */
            ContentValues values = new ContentValues();
            values.put("departure_station_name", departureStationName);
            values.put("target_station_name", targetStationName);
            values.put("last_calculated", currentDate);
            values.put("rating", 1);
            values.put("count", 1);

            /* Eintrag wird in die Datenbank eingefügt */
            db.insert("history", null, values);

            Log.d("SuchmaskeFragment", "Eintrag angelegt: "+values);

        }

        cursor.close();
        db.close();
    }

    /* Für die Ermittlung von daysBetween wird ein Datum benötigt. Somit muss ein Datum welches als String
     * vorhanden ist erstmal in ein Date umgewandelt werden */
    public static Date stringToDate(String date){

        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY);
        Date newDate = null;

        try {
            newDate = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return newDate;
    }

    /*http://stackoverflow.com/a/6406294*/
    /* Die Anzahl an Tagen zwischen zwei Daten wird ermittelt und ausgegeben */
    public static int daysBetween(Date startDate, Date endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);

        int daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    /*http://stackoverflow.com/a/6406294*/
    public static Calendar getDatePart(Date date){
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }
}
