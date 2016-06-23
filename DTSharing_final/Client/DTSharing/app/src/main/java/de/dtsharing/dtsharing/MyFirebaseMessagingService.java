package de.dtsharing.dtsharing;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().toString());

        if (remoteMessage.getData() != null) {
            if(remoteMessage.getData().get("type").equals("chat_message")) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("newMessage");
                broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                broadcastIntent.putExtra("chatId", remoteMessage.getData().get("chat_id"));
                broadcastIntent.putExtra("messageId", remoteMessage.getData().get("message_id"));
                sendBroadcast(broadcastIntent);
            }
        }

        //Calling method to generate notification
        sendNotification(remoteMessage);
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = null;
        if(remoteMessage.getData().get("type").equals("chat_message") || remoteMessage.getData().get("type").equals("new_partner")) {
            intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("comesFromNotification", true);
            intent.putExtra("chatId", remoteMessage.getData().get("chat_id"));
        }
        if(remoteMessage.getData().get("type").equals("search_agent")) {
            intent = new Intent(getApplicationContext(), MatchingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("comesFromNotification", true);
            intent.putExtra("dtTripId", remoteMessage.getData().get("dt_trip_id"));
            intent.putExtra("uniqueTripId", remoteMessage.getData().get("unique_trip_id"));
            intent.putExtra("departureTime", remoteMessage.getData().get("departure_time"));
            intent.putExtra("arrivalTime", remoteMessage.getData().get("arrival_time"));
            intent.putExtra("departureStationName", remoteMessage.getData().get("departure_station_name"));
            intent.putExtra("targetStationName", remoteMessage.getData().get("target_station_name"));
            intent.putExtra("sequenceIdDepartureStation", remoteMessage.getData().get("sequence_id_departure_station"));
            intent.putExtra("sequenceIdTargetStation", remoteMessage.getData().get("sequence_id_target_station"));
            intent.putExtra("hasSeasonTicket", remoteMessage.getData().get("has_season_ticket"));
        }
        if(remoteMessage.getData().get("type").equals("delete")) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.dtsharing_icon)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
