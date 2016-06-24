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

    /* Wird direkt nach dem Login mit den vom Server erhaltenen Benutzerdaten angereichert und somit müssen diese nicht mehr vom Server angefordert werden */
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

    /* Wird nach erhalt eines neuen FCM Tokens gesetzt */
    public void setFCMToken(String token){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.apply();
    }

    /* Wird im LoginFragment gesetzt und ermöglich somit eine Benutzung der Applikation in unterschiedlichen Umgebungen ohne dass diese jedes mal für die IP neu
     * compiled werden muss */
    public void setBaseUrl(String baseIP){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("baseUrl", "http://"+baseIP+":3000");
        editor.putString("baseIP", baseIP);
        editor.apply();
    }

    /* Wird im LoginFragment aufgerufen und ins EditText eingetragen */
    public String getBaseIP(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("baseIP", "192.168.0.15");
    }

    /* Wird bei jeder Aktivität die eine Request an den Server sendet aufgerufen und stellt die Basis URL dar */
    public String getBaseUrl(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("baseUrl", "http://192.168.0.15:3000");
    }

    /* Wird beim Login abgerufen und in der Request an den Server mitgegeben. Dieser fügt diese in der User Datenbank dem Benutzer hinzu und ermöglicht somit das Senden
     * von Nachrichten an gezielte Geräte */
    public String getFCMToken(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("token", null);
    }

    /* Wird beim Editieren des Profils aufgerufen und aktualisiert die Benutzerdaten */
    public void setEditProfileSharedPrefs(String picture, String interests, String more){
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("picture", picture);
        editor.putString("interests", interests);
        editor.putString("more", more);
        editor.apply();
    }

    /* Wird beim Editieren des Profils aufgerufen und ermöglicht es somit die EditTexts + Profilbild mit den derzeitigen Daten zu füllen */
    public ContentValues getEditProfileSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);

        ContentValues data = new ContentValues();
        data.put("user_id", prefs.getString("user_id", null));
        data.put("interests", prefs.getString("interests", null));
        data.put("more", prefs.getString("more", null));
        data.put("picture", prefs.getString("picture", null));

        return data;
    }

    /* Wird überall dort aufgerufen, wo der Name des Benutzers benötigt wird */
    public String getUserNameSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getString("firstName", null)+" "+prefs.getString("lastName", null);
    }

    /* Wird überall dort aufgerufen, wo die UserID des Benutzers benötigt wird */
    public String getUserIdSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }

    /* Wird derzeit nicht Verwendet, liefert jedoch das aktuelle Profilbild des Benutzers */
    public String getProfilePictureSharedPrefs(){
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("picture", null);
    }

    /* Liefert die aktuelle Version der stops, welche einmalig vom Server abgerufen und in der Lokalen Datenbank gesichert werden */
    public int getStopsVersion() {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("stops_version", 0);
    }

    /* Wird beim Logout aufgerufen und entfernt alle Benutzerspezifischen Daten aus den SharedPrefs */
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
