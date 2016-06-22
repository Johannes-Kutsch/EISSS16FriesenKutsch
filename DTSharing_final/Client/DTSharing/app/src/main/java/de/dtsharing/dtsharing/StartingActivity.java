package de.dtsharing.dtsharing;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class StartingActivity extends AppCompatActivity {

    private static final String LOG_TAG = StartingActivity.class.getSimpleName();

    boolean authenticated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authenticated = false;

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                setupDatabase();
            }
        });
        myThread.start();

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


    private void setupDatabase(){

        Log.d(LOG_TAG, "Datenbanken werden erstellt, sofern noch nicht vorhanden");

        SQLiteDatabase db;

        db = openOrCreateDatabase("Stops", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS vrs(stop_name VARCHAR(255),stop_lat NUMERIC,stop_lon NUMERIC);");
        db.close();

        db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS history(departure_station_name VARCHAR(255),target_station_name VARCHAR(255),rating NUMERIC,last_calculated VARCHAR(255),count INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS chats(chat_id VARCHAR(255),key VARCHAR(255));");
        db.execSQL("CREATE TABLE IF NOT EXISTS messages(message_id VARCHAR(255),chat_id VARCHAR(255),author_id VARCHAR(255),message_text VARCHAR(255));");
        //db.execSQL("CREATE TABLE IF NOT EXISTS users(user_id VARCHAR(255),picture_version NUMERIC,picture VARCHAR(255),birth_year VARCHAR(5),first_name VARCHAR(255),last_name VARCHAR(255),gender VARCHAR(10),interests VARCHAR(255),more VARCHAR(255));");
        db.close();

    }
}
