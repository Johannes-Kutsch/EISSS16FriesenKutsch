package de.dtsharing.dtsharing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    Context context;

    private String partnerName, partnerPicture, partnerUserId, chatId, userId, userName;
    private ArrayList<MessagesEntry> messages = new ArrayList<>();
    private EditText inputMessage;
    private LinearLayout toolbar_user_container;
    private ImageButton bSubmit;
    public MessagesAdapter mAdapter;
    public ListView lvMessages;
    Toolbar toolbar;
    TextView mTitle;
    ImageView toolbar_avatar;

    String key;
    ToznyHelper toznyHelper;

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
                if(receivedChatId.equals(chatId))
                    getMessages(userId, chatId, receivedMessageId);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = ChatActivity.this;

        /*Adding Toolbar to Main screen*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);
        toolbar_avatar = (ImageView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_avatar) : null);
        toolbar_user_container = (LinearLayout) (toolbar != null ? toolbar.findViewById(R.id.toolbar_user_container) : null);

        lvMessages = (ListView) findViewById(R.id.lvMessages);
        inputMessage = (EditText) findViewById(R.id.inputMessage);
        bSubmit = (ImageButton) findViewById(R.id.bSubmit);

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(ChatActivity.this);
        userId = sharedPrefsManager.getUserIdSharedPrefs();
        userName = sharedPrefsManager.getUserNameSharedPrefs();

        /*Erzeuge und verbinde Adapter mit der FahrtenFragment ListView*/
        mAdapter = new MessagesAdapter(ChatActivity.this, messages, userId);
        lvMessages.setAdapter(mAdapter);

        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
            /*Zurück Button in der Titelleiste*/
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }


        if(getIntent().getBooleanExtra("comesFromMain", false)){
            Intent chatsIntent = getIntent();
            partnerName = chatsIntent.getStringExtra("name");
            partnerPicture = chatsIntent.getStringExtra("picture");
            chatId = chatsIntent.getStringExtra("chatId");
            getMessages(userId, chatId, null);
        }
        if(getIntent().getBooleanExtra("comesFromNotification", false)){
            Intent chatsIntent = getIntent();
            chatId = chatsIntent.getStringExtra("chatId");
            getMessages(userId, chatId, null);
        }

        key = getKey();
        Log.d("ChatActivity", "KEY: "+key);
        toznyHelper = new ToznyHelper(key);

        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = inputMessage.getText().toString().trim();
                if(!message.equals("")){
                    toznyHelper.encryptString(message);
                    sendMessage(toznyHelper.getEncryptedString());
                }
            }
        });

        toolbar_user_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Erzeuge die Userprofile Activity und füge Daten hinzu*/
                Intent userProfileIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                userProfileIntent.putExtra("userId", partnerUserId);
                /*Starte Matching Activity*/
                startActivity(userProfileIntent);
            }
        });

        IntentFilter filter = new IntentFilter("newMessage");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        myReceiver = new MyMessageReceiver();
        myReceiver.register(ChatActivity.this, filter);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("ChatActivity", "ON NEW INTENT CALLING!!: ");
    }

    private String getKey(){

        String keyString = null;

        SQLiteDatabase db;
        db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

        Cursor cursor = db.rawQuery("SELECT key FROM chats WHERE chat_id=?", new String[] {chatId});

        if(cursor.moveToFirst()){
            keyString = cursor.getString(0);
        }

        cursor.close();
        db.close();

        return keyString;
    }

    private void sendMessage(final String message){

        String base_url = getResources().getString(R.string.base_url);
        String url = base_url+"/users/"+userId+"/chats/"+chatId+"/messages";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.contains("success_message")){

                            String currentTime = new SimpleDateFormat("HH:mm", Locale.GERMANY).format(new Date()),
                                    currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(new Date());
                            int sequence = messages.size() > 0 ? messages.get(messages.size()-1).getSequence() : 0;


                            messages.add(new MessagesEntry(userId, userName, currentTime, currentDate, inputMessage.getText().toString().trim(), sequence));
                            inputMessage.setText("");
                            mAdapter.notifyDataSetChanged();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        })
        {
            /*Daten welche der Post-Request mitgegeben werden*/
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("message_text", message);

                Log.d("MatchingAdapter", "Params: "+params);
                return params;
            }

        };

        Volley.newRequestQueue(ChatActivity.this).add(postRequest);

    }

    private void getMessages(final String userId, final String chatId, final String messageId){

        String base_url = getResources().getString(R.string.base_url);
        String URI;

        if(messageId == null) {
            URI = Uri.parse(base_url + "/users/" + userId + "/chats/" + chatId + "/messages")
                    .buildUpon()
                    .appendQueryParameter("sequence", messages.size() > 0 ? Integer.toString(messages.get(messages.size() - 1).getSequence() + 1) : "0")
                    .build().toString();
        } else {
            URI = base_url+"/users/"+userId+"/chats/"+chatId+"/messages/"+messageId;
        }

        Log.d("ChatActivity", "URI: "+URI);

        final StringRequest jsonRequest = new StringRequest(
                Request.Method.GET, URI, new Response.Listener<String>() {

            @Override
            public void onResponse(String stringResponse) {

                if(messageId == null){
                    getAllMessages(stringResponse);
                } else {
                    getSingleMessage(stringResponse);
                }

                mTitle.setText(partnerName);

                if(partnerPicture.equals("null") || partnerPicture == null){
                    int placeholder = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_account_circle_48dp", null, null);
                    toolbar_avatar.setImageResource(placeholder);
                }else{
                    RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), EncodeDecodeBase64.decodeBase64(partnerPicture));
                    roundDrawable.setCircular(true);
                    toolbar_avatar.setImageDrawable(roundDrawable);
                }

                mAdapter.notifyDataSetChanged();

            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });

        Volley.newRequestQueue(ChatActivity.this).add(jsonRequest);

    }

    private void getAllMessages(String stringResponse){

        try {

            Log.d("ChatActivity", "GET_ALL_MESSAGES: "+stringResponse);

            JSONObject response = new JSONObject(stringResponse);

            JSONObject partner = response.getJSONObject("partner");
            JSONArray messagesArray = response.getJSONArray("messages");

            partnerName = partner.getString("first_name")+" "+partner.getString("last_name");
            partnerPicture = partner.getString("picture");

            if(messagesArray.length() > 0) {

                for (int i = 0; i < messagesArray.length(); i++) {

                    JSONObject messageData = messagesArray.getJSONObject(i);

                    String authorId = messageData.getString("author_id"),
                            time = messageData.getString("time"),
                            date = messageData.getString("date"),
                            message = messageData.getString("message_text");
                    int sequence = messageData.getInt("sequence");
                    String name = authorId.equals(userId) ? userName : partnerName;

                    if(partnerUserId == null && !authorId.equals(userId))
                        partnerUserId = authorId;

                    toznyHelper.decryptString(message);

                    messages.add(new MessagesEntry(authorId, name, time, date, toznyHelper.getDecryptedString(), sequence));
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getSingleMessage(String stringResponse){

        try {

            Log.d("ChatActivity", "GET_SINGLE_MESSAGE: "+stringResponse);
            JSONObject messageData = new JSONObject(stringResponse);

            String authorId = messageData.getString("author_id"),
                    time = messageData.getString("time"),
                    date = messageData.getString("date"),
                    message = messageData.getString("message_text");
            int sequence = messageData.getInt("sequence");
            String name = authorId.equals(userId) ? userName : partnerName;

            toznyHelper.decryptString(message);

            messages.add(new MessagesEntry(authorId, name, time, date, toznyHelper.getDecryptedString(), sequence));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //<--           OnOptionsItemSelected Start         -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*Zurück Button geklickt*/
            case android.R.id.home:
                /*Schließe Aktivität ab und kehre zurück*/
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           OnOptionsItemSelected End         -->


    @Override
    protected void onDestroy() {
        super.onDestroy();
        myReceiver.unregister(ChatActivity.this);
    }

}
