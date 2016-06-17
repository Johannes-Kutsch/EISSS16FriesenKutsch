package de.dtsharing.dtsharing;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    Context context;

    public SharedPrefsManager(Context context){
        this.context = context;
    }

    public void setUserIdSharedPrefs(String user_id){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("user_id", user_id);
        editor.apply();
    }

    public String getUserIdSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }

}
