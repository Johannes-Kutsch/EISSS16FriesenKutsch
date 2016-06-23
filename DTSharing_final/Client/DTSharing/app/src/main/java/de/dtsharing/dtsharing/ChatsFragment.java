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

    private String userId;

    private MyMessageReceiver myReceiver;


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

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvChats = (ListView) v.findViewById(R.id.lvChats);
        noChatsContainer = (TextView) v.findViewById(R.id.noChatsContainer);

        userId = new SharedPrefsManager(this.getContext()).getUserIdSharedPrefs();

        /*Erzeuge und verbinde Adapter mit der ChatsFragment ListView*/
        mAdapter = new ChatsAdapter(getContext(), chats);
        lvChats.setAdapter(mAdapter);

        /*Fülle Array mit Beispieldaten*/
        getChatData();

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

        IntentFilter filter = new IntentFilter("newMessage");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        myReceiver = new MyMessageReceiver();
        myReceiver.register(v.getContext(), filter);

        return v;
    }

    private void getMessage(final String chatID, final String messageID){

        String base_url = getResources().getString(R.string.base_url);

        String URI = base_url+"/users/"+userId+"/chats/"+chatID+"/messages/"+messageID;

        Log.d("ChatsFragment", "URI: "+URI);

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {

                    String newDate = response.getString("date");
                    String newMessage = response.getString("message_text");

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

    private void getChatData(){

        String base_url = getResources().getString(R.string.base_url);
        final String URI = base_url+"/users/"+userId+"/chats";
        chats.clear();

        final JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                Log.d("FahrtenFragment", "response: "+response.toString());
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

                            checkForKey(chat.getString("_id"));

                            chats.add(new ChatsEntry(chatData));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
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
                        int statuscode = error.networkResponse.statusCode;

                        if(statuscode == 404){
                            noChatsContainer.setVisibility(View.VISIBLE);
                        }
                    }

                });

        Volley.newRequestQueue(getContext()).add(jsonRequest);

    }

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

    public void getChatKey(String userId, final String chatID){

        String base_url = getResources().getString(R.string.base_url);
        final String URI = base_url+"/users/"+userId+"/chats/"+chatID+"/key";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                String key = null;
                try {
                    key = response.getString("key");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myReceiver.unregister(v.getContext());
    }
}
