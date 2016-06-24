package de.dtsharing.dtsharing;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    Context context;

    public SharedPrefsManager(Context context){
        this.context = context;
    }

    public void setLoggedInSharedPrefs(String user_id, String picture, String firstName, String lastName, String interests, String more){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("user_id", user_id);
        editor.putString("picture", picture);
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("interests", interests);
        editor.putString("more", more);
        editor.apply();
    }

    public void setFCMToken(String token){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.apply();
    }

    public void setBaseUrl(String baseIP){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("baseUrl", "http://"+baseIP+":3000");
        editor.putString("baseIP", baseIP);
        editor.apply();
    }

    public String getBaseIP(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("baseIP", "192.168.0.15");
    }

    public String getBaseUrl(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("baseUrl", "http://192.168.0.15:3000");
    }

    public String getFCMToken(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("token", null);
    }

    public void setEditProfileSharedPrefs(String picture, String interests, String more){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("picture", picture);
        editor.putString("interests", interests);
        editor.putString("more", more);
        editor.apply();
    }

    public ContentValues getEditProfileSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);

        ContentValues data = new ContentValues();
        data.put("user_id", prefs.getString("user_id", null));
        data.put("interests", prefs.getString("interests", null));
        data.put("more", prefs.getString("more", null));
        data.put("picture", prefs.getString("picture", null));

        return data;
    }

    public String getUserNameSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getString("firstName", null)+" "+prefs.getString("lastName", null);
    }

    public String getUserIdSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }
    public String getProfilePictureSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("picture", null);
    }
    public int getStopsVersion() {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("stops_version", 0);
    }

    public void setLoggedOutSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove("user_id").apply();
        prefs.edit().remove("picture").apply();
        prefs.edit().remove("firstName").apply();
        prefs.edit().remove("lastName").apply();
        prefs.edit().remove("interests").apply();
        prefs.edit().remove("more").apply();
    }

}
