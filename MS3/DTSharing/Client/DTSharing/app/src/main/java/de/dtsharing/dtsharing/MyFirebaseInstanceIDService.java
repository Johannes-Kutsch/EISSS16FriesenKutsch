package de.dtsharing.dtsharing;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

//Class extending FirebaseInstanceIdService
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    public String token = null;

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        /* Wurde ein neues FCM Token generiert, wird der TokenReceiver über einen Broadcast benachrichtigt
         * und sichert dieses in den SharedPrefs */
        if (refreshedToken != null) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("OnTokenRefresh");
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("token", refreshedToken);
            sendBroadcast(broadcastIntent);
        }

    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    }
}
