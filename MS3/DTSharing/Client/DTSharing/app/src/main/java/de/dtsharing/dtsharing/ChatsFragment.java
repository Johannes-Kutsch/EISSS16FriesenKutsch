package de.dtsharing.dtsharing;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Handler;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    RelativeLayout v;

    private ListView lvChats;
    private TextView noChatsContainer;

    private ArrayList<ChatsEntry> chats = new ArrayList<>();
    private ChatsAdapter mAdapter;

    private String userId, base_url;

    private MyMessageReceiver myReceiver;


    /* Broadcast receiver, welcher in der Chatsübersicht die last_message eines Chats aktualisiert, wenn
     * eine neue Nachricht reinkommt */
    public class MyMessageReceiver extends BroadcastReceiver {
        public boolean isRegistered;

        /*http://stackoverflow.com/a/29836639
        * Da es keine andere Möglichkeit gibt zu überprüfen ob der receiver registriert ist
        * und einen unregistrierten Receiver zu entfernen eine FATAL EXCEPTION wirft*/
        public Intent register(Context context, IntentFilter filter) {
            isRegistered = true;
            return context.registerReceiver(this, filter);
        }

        public boolean unregister(Context context) {
            if (isRegistered) {
                context.unregisterReceiver(this);
                isRegistered = false;
                return true;
            }
            return false;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("newMessage")) {
                String receivedChatId = intent.getStringExtra("chatId");
                String receivedMessageId = intent.getStringExtra("messageId");
                getMessage(receivedChatId, receivedMessageId);
            }
        }
    }


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_chats, container, false);

        /* Die base_url sowie userId werden aus den SharedPrefs bezogen */
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(v.getContext());
        base_url = sharedPrefsManager.getBaseUrl();
        userId = sharedPrefsManager.getUserIdSharedPrefs();

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvChats = (ListView) v.findViewById(R.id.lvChats);
        noChatsContainer = (TextView) v.findViewById(R.id.noChatsContainer);

        /*Erzeuge und verbinde Adapter mit der ChatsFragment ListView*/
        mAdapter = new ChatsAdapter(getContext(), chats);
        lvChats.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        getChatData();

        /* onItemClickListener für die ListView Chats. Durch klick auf einen Chat gelangt man in die ChatActivity
         * Alle für die Darstellung des richtigen Chats benötigten Informationen werden mitgegeben */
        lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                /*Erzeuge die Matching Activity und füge Daten hinzu*/
                Intent messagesIntent = new Intent(v.getContext(), ChatActivity.class);
                messagesIntent.putExtra("comesFromMain", true);
                messagesIntent.putExtra("chatId", chats.get(position).getChatId());
                messagesIntent.putExtra("picture", chats.get(position).getPicture());
                messagesIntent.putExtra("name", chats.get(position).getName());

                /*Starte Matching Activity*/
                startActivity(messagesIntent);
            }
        });

        /* Der Broadcast receiver wird registriert */
        IntentFilter filter = new IntentFilter("newMessage");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        myReceiver = new MyMessageReceiver();
        myReceiver.register(v.getContext(), filter);

        return v;
    }

    /* GET Request um die last_message eines chats zu erhalten und zu aktualisieren */
    private void getMessage(final String chatID, final String messageID){

        String URI = base_url+"/users/"+userId+"/chats/"+chatID+"/messages/"+messageID;

        Log.d("ChatsFragment", "URI: "+URI);

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {

                    String newDate = response.getString("date");
                    String newMessage = response.getString("message_text");

                    /* die ArrayListe wird durchlaufen um den Eintrag mit der der übereinstimmenden
                     * ChatID zu finden. Anschließend kann die last_message + datum aktualisiert werden.
                      * Abschließend wird die forEach durch ein break vorzeitig verlassen und der Adapter über Änderungen informiert*/
                    for (ChatsEntry str : chats){
                        if(str.getChatId().equals(chatID)){
                            str.setLastMessageAndDate(newMessage, newDate);
                            break;
                        }
                    }
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });

        Volley.newRequestQueue(v.getContext()).add(jsonRequest);

    }

    /* GET Request um die für den Benutzer aktiven Chats zu erhalten*/
    private void getChatData(){

        final String URI = base_url+"/users/"+userId+"/chats";
        chats.clear();

        /* Als Response wird ein JSONArray erwartet */
        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                Log.d("FahrtenFragment", "response: "+response.toString());

                /* Befinden sich Daten im Array wird dieses durchlaufen und jeder Eintrag der ArrayListe
                 * hinzugefügt. Abschließend wird der Adapter über Änderungen Informiert */
                if(response.length() > 0) {

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject chat = response.getJSONObject(i);
                            ContentValues chatData = new ContentValues();

                            chatData.put("chatId", chat.getString("_id"));
                            chatData.put("firstName", chat.getString("first_name"));
                            chatData.put("lastName", chat.getString("last_name"));
                            chatData.put("date", chat.getString("date"));
                            chatData.put("departureStationName", chat.getString("departure_station_name"));
                            chatData.put("targetStationName", chat.getString("target_station_name"));
                            chatData.put("picture", chat.getString("picture"));
                            chatData.put("lastMessage", chat.getString("last_message"));

                            /* Bei jedem Chat der hinzugefügt wird, wird überprüft ob der Benutzer bereits den Key
                             * in der Lokalen Datenbank enthält */
                            checkForKey(chat.getString("_id"));

                            chats.add(new ChatsEntry(chatData));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                /* Befinden sich keine Daten im Array wird der Container angezeigt, welcher den Benutzer
                 * darauf hinweist, dass er derzeit für keine Chats eingetragen ist */
                }else{
                    noChatsContainer.setVisibility(View.VISIBLE);
                }

                mAdapter.notifyDataSetChanged();
                lvChats.setVisibility(View.VISIBLE);

            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        int statuscode = 0;
                        if (error.networkResponse != null) {
                            statuscode = error.networkResponse.statusCode;
                        }

                        /* Wird ein 404 geworfen wird ebenfalls der Container für keine aktiven Chats angezeigt */
                        if(statuscode == 404){
                            noChatsContainer.setVisibility(View.VISIBLE);
                        }
                    }

                });

        Volley.newRequestQueue(getContext()).add(jsonRequest);

    }

    /* Methode um zu Überprüfen ob in der Lokalen Datenbank bereits ein Eintrag mit chatID und Key existiert.
     * Ist dies nicht der fall wird der Key zum ver- und entschlüsseln der Nachrichten vom Server angefordert */
    private void checkForKey(String chatID){

        SQLiteDatabase db;
        db = v.getContext().openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

        Cursor cursor = db.rawQuery("SELECT * FROM chats WHERE chat_id=?", new String[] {chatID});

        if (!cursor.moveToFirst()){
            getChatKey(userId, chatID);
        }

        cursor.close();
        db.close();

    }

    /* GET Request an den Server um den Key zum ver- und entschlüsseln von Nachrichten zu erhalten
    * Dieser wird anschließend in der Lokalen Datenbank gesichert und sollte nur wieder angefordert werden
    * wenn der Benutzer sein Gerät wechselt oder die App Daten löscht / App deinstalliert und neu installiert */
    public void getChatKey(String userId, final String chatID){

        final String URI = base_url+"/users/"+userId+"/chats/"+chatID+"/key";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                /* Key wird als String erhalten */
                String key = null;
                try {
                    key = response.getString("key");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /* Wenn der Key nicht null ist soll dieser lokal in der Datenbank des Geräts gesichert werden */
                if(key != null) {
                    SQLiteDatabase db;
                    db = v.getContext().openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

                    ContentValues values = new ContentValues();
                    values.put("chat_id", chatID);
                    values.put("key", key);

                    db.insert("chats", null, values);

                    db.close();
                }

            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });

        Volley.newRequestQueue(getContext()).add(jsonRequest);

    }


    /* Beim beenden der View muss der receiver abgemeldet werden */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myReceiver.unregister(v.getContext());
    }
}
