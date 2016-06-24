package de.dtsharing.dtsharing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    Context context;

    private String partnerName, partnerPicture, partnerUserId, chatId, userId, userName, base_url;
    private ArrayList<MessagesEntry> messages = new ArrayList<>();
    private EditText inputMessage;
    private LinearLayout toolbar_user_container;
    private ImageButton bSubmit;
    public MessagesAdapter mAdapter;
    public ListView lvMessages;
    private CardView cvRatingsContainer;
    Toolbar toolbar;
    TextView mTitle, ratingsContainerText;
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
        base_url = new SharedPrefsManager(context).getBaseUrl();

        /*Adding Toolbar to Main screen*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);
        toolbar_avatar = (ImageView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_avatar) : null);
        toolbar_user_container = (LinearLayout) (toolbar != null ? toolbar.findViewById(R.id.toolbar_user_container) : null);

        lvMessages = (ListView) findViewById(R.id.lvMessages);
        inputMessage = (EditText) findViewById(R.id.inputMessage);
        bSubmit = (ImageButton) findViewById(R.id.bSubmit);
        cvRatingsContainer = (CardView) findViewById(R.id.cvRatingContainer);
        ratingsContainerText = (TextView) findViewById(R.id.ratingContainerText);

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
            key = getKey();
            toznyHelper = new ToznyHelper(key);
        }
        if(getIntent().getBooleanExtra("comesFromNotification", false)){
            Intent chatsIntent = getIntent();
            chatId = chatsIntent.getStringExtra("chatId");
            requestChatKey(userId, chatId);
        }


        getPartnerDetails();
        getMessages(userId, chatId, null);

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

    private void setRatingsContainer(boolean hasVoted){

        if(hasVoted){
            ratingsContainerText.setText("Vielen Dank für die Bewertung!");
            cvRatingsContainer.setVisibility(View.VISIBLE);
        } else {
            cvRatingsContainer.setVisibility(View.VISIBLE);
            cvRatingsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int starFull = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_full_48dp", null, null),
                            starBorder = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_border_48dp", null, null);
                    final int[] finalRating = new int[1];
                    finalRating[0] = 3;

                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this, R.style.AppTheme_Dialog_Alert);
                    LayoutInflater inflater = (ChatActivity.this).getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.fragment_rating, null);

                    final ImageButton star1 = (ImageButton) dialogView.findViewById(R.id.ivStar1),
                            star2 = (ImageButton) dialogView.findViewById(R.id.ivStar2),
                            star3 = (ImageButton) dialogView.findViewById(R.id.ivStar3),
                            star4 = (ImageButton) dialogView.findViewById(R.id.ivStar4),
                            star5 = (ImageButton) dialogView.findViewById(R.id.ivStar5);
                    final EditText etComment = (EditText) dialogView.findViewById(R.id.etComment);

                    builder.setView(dialogView)
                            .setPositiveButton("Bewerten", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String comment = etComment.getText().toString().trim();
                                    submitRating(finalRating[0], comment);
                                }
                            })
                            .setNegativeButton("Abbruch", null);

                    View.OnClickListener starsOnClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int rating = 0;
                            star5.setImageResource(starBorder);
                            star4.setImageResource(starBorder);
                            star3.setImageResource(starBorder);
                            star2.setImageResource(starBorder);
                            star1.setImageResource(starBorder);

                            switch (view.getId()){
                                case R.id.ivStar5:
                                    star5.setImageResource(starFull);
                                    ++rating;
                                case R.id.ivStar4:
                                    star4.setImageResource(starFull);
                                    ++rating;
                                case R.id.ivStar3:
                                    star3.setImageResource(starFull);
                                    ++rating;
                                case R.id.ivStar2:
                                    star2.setImageResource(starFull);
                                    ++rating;
                                case R.id.ivStar1:
                                    star1.setImageResource(starFull);
                                    finalRating[0] = ++rating;
                            }
                        }
                    };

                    star1.setOnClickListener(starsOnClickListener);
                    star2.setOnClickListener(starsOnClickListener);
                    star3.setOnClickListener(starsOnClickListener);
                    star4.setOnClickListener(starsOnClickListener);
                    star5.setOnClickListener(starsOnClickListener);

                    builder.create();
                    builder.show();
                }
            });
        }

    }

    private void submitRating(final int rating, final String comment){

        final String URI = base_url+"/users/"+partnerUserId+"/ratings";

        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.contains("success_message")){
                            ratingsContainerText.setText("Vielen Dank für die Bewertung!");
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
                params.put("author_id", userId);
                params.put("stars", Integer.toString(rating));
                params.put("comment", comment);
                params.put("chat_id", chatId);

                Log.d("MatchingAdapter", "Params: "+params);
                return params;
            }

        };

        Volley.newRequestQueue(ChatActivity.this).add(postRequest);

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

    public void requestChatKey(String userId, final String chatID){

        final String URI = base_url+"/users/"+userId+"/chats/"+chatID+"/key";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.d("ChatActivity", "RESPONSE: "+response);
                String key = null;
                try {
                    key = response.getString("key");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(key != null) {

                    SQLiteDatabase db;
                    db = openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);

                    ContentValues values = new ContentValues();
                    values.put("chat_id", chatID);
                    values.put("key", key);

                    db.insert("chats", null, values);

                    db.close();

                    toznyHelper = new ToznyHelper(key);
                }

            }

        },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }

                });

        Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
    }

    private void sendMessage(final String message){

        String URI = base_url+"/users/"+userId+"/chats/"+chatId+"/messages";

        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
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

    private void getPartnerDetails(){

        String URI = base_url+"/users/"+userId+"/chats/"+chatId+"/partner";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject partner = response.getJSONObject("partner");
                    partnerName = partner.getString("first_name")+" "+partner.getString("last_name");
                    partnerPicture = partner.getString("picture");
                    partnerUserId = partner.getString("_id");
                } catch (JSONException e) {
                    e.printStackTrace();
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

    private void getMessages(final String userId, final String chatId, final String messageId){

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

            if(response.has("has_voted")){
                boolean hasVoted = response.getBoolean("has_voted");
                setRatingsContainer(hasVoted);
            }

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
