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

    public static void startActionAddToHistory(Context context, String departureStationName, String targetStationName) {
        Intent intent = new Intent(context, HistoryService.class);
        intent.setAction(ACTION_AddToHistory);
        intent.putExtra(EXTRA_DepartureStationName, departureStationName);
        intent.putExtra(EXTRA_TargetStationName, targetStationName);
        context.startService(intent);
    }

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

        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY);
        final String currentDate = df.format(c.getTime());

        SQLiteDatabase db;
        db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT * FROM history", null);

        if(cursor.getCount() > 0){

            cursor.moveToFirst();

            while (cursor.moveToNext()){

                String departureStationName = cursor.getString(0),
                        targetStationName = cursor.getString(1),
                        last_calculated = cursor.getString(3);
                double rating = cursor.getDouble(2);

                int daysBetween = daysBetween(stringToDate(last_calculated), stringToDate(currentDate));

                if (daysBetween > 0) {

                    rating = rating * Math.pow(0.95, daysBetween);

                    ContentValues values = new ContentValues();
                    values.put("rating", rating);
                    values.put("last_calculated", currentDate);

                    db.update("history", values, "departure_station_name=? AND target_station_name= ?", new String[] {departureStationName, targetStationName});

                }

            }

        }

        cursor.close();
        db.close();

    }

    private void handleActionAddToHistory(String departureStationName, String targetStationName) {

        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.GERMANY);
        final String currentDate = df.format(c.getTime());

        SQLiteDatabase db;
        db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT * FROM history WHERE departure_station_name=? AND target_station_name=?", new String[] {departureStationName, targetStationName});

        if(cursor.getCount() > 0){

            cursor.moveToFirst();
            String last_calculated = cursor.getString(3);
            double rating = cursor.getDouble(2);
            int count = cursor.getInt(4);

            int daysBetween = daysBetween(stringToDate(last_calculated), stringToDate(currentDate));

            rating = Math.log(count+3)+1;

            ContentValues values = new ContentValues();
            values.put("rating", rating);
            values.put("last_calculated", currentDate);
            values.put("count", ++count);

            db.update("history", values, "departure_station_name=? AND target_station_name= ?", new String[] {departureStationName, targetStationName});

            Log.d("SuchmaskeFragment", "DaysBetween: "+daysBetween);
            Log.d("SuchmaskeFragment", values.toString());

        }else{

            ContentValues values = new ContentValues();
            values.put("departure_station_name", departureStationName);
            values.put("target_station_name", targetStationName);
            values.put("last_calculated", currentDate);
            values.put("rating", 1);
            values.put("count", 1);

            db.insert("history", null, values);

            Log.d("SuchmaskeFragment", "Eintrag angelegt: "+values);

        }

        cursor.close();
        db.close();
    }

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
