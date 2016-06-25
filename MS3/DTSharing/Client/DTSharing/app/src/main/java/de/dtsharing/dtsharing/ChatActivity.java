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
    ToznyHelper toznyHelper = null;

    private MyMessageReceiver myReceiver;

    /* Der receiver zum Empfangen neuer Nachrichten */
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

        /* Wird ein Broadcast mit der Action "newMessage" empfangen werden chatId und messageId aus diesem
        * gezogen. Entspricht die chatId der des aktuellen Chats handelt es sich um eine Nachricht aus diesem Chat.
        * Daraufhin wird die Methode getMessages aufgerufen. Da der dritte Parameter nicht null ist, weiß die Methode
        * dass es sich lediglich um den Empfang einer einzelnen Nachricht handelt. */
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

        /* Erfasse toolbar Views */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);
        toolbar_avatar = (ImageView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_avatar) : null);
        toolbar_user_container = (LinearLayout) (toolbar != null ? toolbar.findViewById(R.id.toolbar_user_container) : null);

        /* Views werden erfasst */
        lvMessages = (ListView) findViewById(R.id.lvMessages);
        inputMessage = (EditText) findViewById(R.id.inputMessage);
        bSubmit = (ImageButton) findViewById(R.id.bSubmit);
        cvRatingsContainer = (CardView) findViewById(R.id.cvRatingContainer);
        ratingsContainerText = (TextView) findViewById(R.id.ratingContainerText);

        /* SharedPrefsManager wird erzeugt und base_url, userId sowie userName werden bezogen */
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(ChatActivity.this);
        base_url = sharedPrefsManager.getBaseUrl();
        userId = sharedPrefsManager.getUserIdSharedPrefs();
        userName = sharedPrefsManager.getUserNameSharedPrefs();

        /*Erzeuge und verbinde Adapter mit der FahrtenFragment ListView*/
        mAdapter = new MessagesAdapter(ChatActivity.this, messages, userId);
        lvMessages.setAdapter(mAdapter);

        /* Custom Toolbar wird gesetzt */
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
            /*Zurück Button in der Titelleiste*/
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        /* Kommt der Benutzer von der mainActivity kann der Key zum ent- und verschlüsseln von Nachrichten
         * direkt mitgenommen werden */
        if(getIntent() != null){
            Intent chatsIntent = getIntent();
            chatId = chatsIntent.getStringExtra("chatId");
        }

        /* Es wird Überprüft ob der toznyHelper bereits mit einem korrekten Key erzeugt wurde und maßnahmen getroffen wenn
         * dies nicht der Fall ist */
        checkForTozny();

        /* Rufe allgemeine Informationen zum Partner ab */
        getPartnerDetails();

        /* Wird der Chat betreten sollen alle Messages abgerufen werden,daher wird der dritte Paramteter auf null
         * gesetzt und signalisiert dadurch, dass es sich nicht um das Abrufen einer einzelnen Nachricht handelt */
        getMessages(userId, chatId, null);

        /* onClick für den Nachricht Senden Button. Wurde ein Text eingegeben wird dieser mithilfe des ToznyHelpers
         * verschlüsselt und an den Server gesendet */
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = inputMessage.getText().toString().trim();
                if(!message.equals("")){
                    checkForTozny();
                    toznyHelper.encryptString(message);
                    sendMessage(toznyHelper.getEncryptedString());
                }
            }
        });

        /* onClick für den User_Container in der Toolbar. Wird dieser angetippt gelangt der Benutzer zum Profil des
         * Chatpartners */
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

        /* Der Broadcast Receiver wird registriert */
        IntentFilter filter = new IntentFilter("newMessage");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        myReceiver = new MyMessageReceiver();
        myReceiver.register(ChatActivity.this, filter);

    }

    /* Methode die sich um die Erzeugung des Bewerten Dialogs kümmert
     * Wurde eine Fahrt abgeschlossen wird in der Response zum erhalten alle Nachrichten ein Boolean mit hasVoted mitgegeben
     * Entspricht dieser true, wird lediglich ein Dank für die Bewertung ausgesprochen. Ist dieser false erhält der Nutzer eine nette
     * Aufforderung seine Fahrt zu bewerten. Die Bewertung geschieht über einen Custom Alert Dialog */
    private void setRatingsContainer(boolean hasVoted){

        /* Fahrt wurde bereits Bewertet */
        if(hasVoted){
            ratingsContainerText.setText("Vielen Dank für die Bewertung!");
            cvRatingsContainer.setVisibility(View.VISIBLE);

        /* Fahrt wurde noch nicht Bewertet */
        } else {
            cvRatingsContainer.setVisibility(View.VISIBLE);
            cvRatingsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    /* Angabe der Ressource star Drawables  */
                    final int starFull = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_full_48dp", null, null),
                            starBorder = getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_border_48dp", null, null);

                    /* Final int array, da der Wert aus einer anonymen inneren Funktionn zugewiesen werden muss */
                    final int[] finalRating = new int[1];
                    /* Rating startet bei 3 */
                    finalRating[0] = 3;

                    /* Alert Dialog mit Custom Body für die Darstellung des Rating wie geplant */
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this, R.style.AppTheme_Dialog_Alert);
                    LayoutInflater inflater = (ChatActivity.this).getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.fragment_rating, null);

                    /* Views des Custom Bodys werden erfasst */
                    final ImageButton star1 = (ImageButton) dialogView.findViewById(R.id.ivStar1),
                            star2 = (ImageButton) dialogView.findViewById(R.id.ivStar2),
                            star3 = (ImageButton) dialogView.findViewById(R.id.ivStar3),
                            star4 = (ImageButton) dialogView.findViewById(R.id.ivStar4),
                            star5 = (ImageButton) dialogView.findViewById(R.id.ivStar5);
                    final EditText etComment = (EditText) dialogView.findViewById(R.id.etComment);

                    /* Custom Body wird dem Dialog Builder zugewiesen.  */
                    builder.setView(dialogView)

                            /* Bei onClick des positiven Buttons wird die Bewertung an den Server geschickt */
                            .setPositiveButton("Bewerten", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String comment = etComment.getText().toString().trim();
                                    submitRating(finalRating[0], comment);
                                }
                            })
                            .setNegativeButton("Abbruch", null);

                    /* onClick für die Sterne. Durch die Verwendung des Switch Case und dem Fall dass dieses durchläuft, wenn kein
                     * break gesetzt wird ist es möglich das Rating sehr effizient und in wenigen Zeilen Code zu realisieren */
                    View.OnClickListener starsOnClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            /* Wird ein Stern angetippt werden Rating und Resourcen auf die Ausgangssituation zurückgesetzt */
                            int rating = 0;
                            star5.setImageResource(starBorder);
                            star4.setImageResource(starBorder);
                            star3.setImageResource(starBorder);
                            star2.setImageResource(starBorder);
                            star1.setImageResource(starBorder);

                            /* Anschließend wird über ein Switch Case das Rating bestimmt als auch die ImageResource geändert und abschließend dem finalRating array
                             * zugewiesen */
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

                    /* Setze den onClick Listener für jeden Star */
                    star1.setOnClickListener(starsOnClickListener);
                    star2.setOnClickListener(starsOnClickListener);
                    star3.setOnClickListener(starsOnClickListener);
                    star4.setOnClickListener(starsOnClickListener);
                    star5.setOnClickListener(starsOnClickListener);

                    /* Erzeuge und zeige den Dialog */
                    builder.create();
                    builder.show();
                }
            });
        }

    }

    /* POST-Request an den Server mit den Rating Daten im Body */
    private void submitRating(final int rating, final String comment){

        final String URI = base_url+"/users/"+partnerUserId+"/ratings";

        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        /* Bei Erfolgreicher Response wird der Text des Bewwerten Containers geändert */
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

    /* Es wird Überprüft ob der ToznyHelper bereits mit einem gültigen Key erzeugt wurde. Ist dies nicht der Fall
     * wird geschaut, ob bereits ein Key existiert. Ist auch dies nicht der Fall wird in der Lokalen Datenbank nach
     * dem Key geschaut. Ist auch diese Suche Ergebnislos wird der Key vom Server angefordert und der ToznyHelper erzeugt
     * Durch das kurze Überprüfen des ToznyHelpers vor jeder Ver- und Entschlüsseln Aktion werden nullpointer vermieden die zu einem
     * Absturz der App führen. Ebenso wird dadurch garantiert, dass Nachrichten korrekt Ent- und Verschlüsselt werden */
    private void checkForTozny(){

        /* Existiert der ToznyHelper bereits? */
        if (toznyHelper == null){

            /* Suche in der Lokalen Datenbank nach dem Key */
            if (key == null)
                key = getKey();

            /* Key in der Lokalen Datenbank nicht gefunden => Vom Server anfordern und den ToznyHelper sofort erzeugen*/
            if (key == null)
                requestChatKey(userId, chatId);

            /* Andernfalls erzeuge den ToznyHelper mit dem Key aus der Lokalen Datenbank */
            else
                toznyHelper = new ToznyHelper(key);

        }

    }

    /* Es wird geschaut, ob der Verschlüsselungs Key zugehörig zu diesem Chat bereits in der Lokalen Datenbank
     * vorhanden ist */
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

    /* Hier wird durch ein GET Request an den Server mit UserID und ChatID als Query Parameter der
     * Key für diesen Chat angefordert. Bei einer Erfolgreichen Response wird dieser in die Lokale
     * Datenbank gesichert und der ToznyHelper sofort erzeugt um eine weitere Datenbankabfrage zu ersparen*/
    public void requestChatKey(String userId, final String chatID){

        final String URI = base_url+"/users/"+userId+"/chats/"+chatID+"/key";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.d("ChatActivity", "RESPONSE: "+response);

                /* Der Key wird der JSON Response entnommen */
                String key = null;
                try {
                    key = response.getString("key");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /* Wenn der Key erfolgreich entnommen wurde wird er in die Lokale Datenbank gesichert und
                 * der ToznyHelper wird sofort erzeugt */
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

    /* Es wird ein POST-Request an den Server gesandt, welche die verschlüsselte Nachricht im Body mitliefert. Bei einer Erfolgreichen
     * Response wird die Nachricht sofort (nur Clientseitig) der ArrayListe hinzugefügt und der Adapter benachrichtigt */
    private void sendMessage(final String message){

        String URI = base_url+"/users/"+userId+"/chats/"+chatId+"/messages";

        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if(response.contains("success_message")){

                            /* Aktuelle Uhrzeit und Datum werden ermittelt. Ebenso wird die fortlaufende sequence der Nachricht ermittelt, indem
                             * die der letzten Nachricht um 1 aufaddiert, oder falls keine Nachrichten vorhanden einfach 0 gesetzt wird */
                            String currentTime = new SimpleDateFormat("HH:mm", Locale.GERMANY).format(new Date()),
                                    currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(new Date());
                            int sequence = messages.size() > 0 ? messages.get(messages.size()-1).getSequence()+1 : 0;

                            /* Nachricht wird der ArrayListe hinzugefügt. InputFeld wird geleert und der Adapter wird benachrichtigt */
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

    /* Benutzerdaten des Partners werden über ein GET-Reqeuest mit den Query Parametern userID und chatID an den Server erhalten. Diese Methode
     * wird benötigt um Name, Profilbild und Id des Partner zu erhalten. Diese müssen abgefragt werden, da es 2 Wege gibt in den Chat zu kommen.
     * Über die Chatübersicht könnten diese Daten bereits mitgenommen werden, kommt man hingegen durch Klick auf die Notification fehlen einem diese
     * Daten */
    private void getPartnerDetails(){

        String URI = base_url+"/users/"+userId+"/chats/"+chatId+"/partner";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, URI, null, new Response.Listener<JSONObject>() {

            /* Bei einer Erfolgreichen Response werden die Variablen deklariert und die Informationen in die dafür Vorgesehenen Views eingefügt */
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

                /* Setzen des Namens */
                mTitle.setText(partnerName);

                /* Ist das Bild null wird das default Bild gesetzt. Ansonsten wird das mitgelieferte Bild in eine Bitmap umgewandelt und der ImageView übergeben */
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

    /* Die Methode getMessages ist ein GER-Request an den Server um entweder, wenn keine messageID angegeben wurde, alle Nachrichten zu erhalten, oder mit vorhandener
     * MessageIO, welche dem Broadcast Receiver übergeben wird, einzelne Nachrichten abrufen zu können. Da es sich bei der Response aller Nachrichten um ein JSONArray und
     * bei der Response von einzelnen Nachrichten um ein JSONObject handelt muss bei der Behandlung dieser unterschieden werden */
    private void getMessages(final String userId, final String chatId, final String messageId){

        String URI;

        /* Ist keine MessageID vorhanden wird die URI auf /messages mit dem Query Parameter sequence erzeugt */
        if(messageId == null) {
            URI = Uri.parse(base_url + "/users/" + userId + "/chats/" + chatId + "/messages")
                    .buildUpon()
                    .appendQueryParameter("sequence", messages.size() > 0 ? Integer.toString(messages.get(messages.size() - 1).getSequence() + 1) : "0")
                    .build().toString();

        /* Andernfalls wird das GET auf die URI der einzelnen Nachrichten gemacht */
        } else {
            URI = base_url+"/users/"+userId+"/chats/"+chatId+"/messages/"+messageId;
        }

        Log.d("ChatActivity", "URI: "+URI);

        final StringRequest jsonRequest = new StringRequest(
                Request.Method.GET, URI, new Response.Listener<String>() {

            @Override
            public void onResponse(String stringResponse) {

                /* Ist die messageID null wird ein JSONArray erwartet */
                if(messageId == null){
                    getAllMessages(stringResponse);

                /* Andernfalls wird die behandlung der einzelnen Nachricht initiiert */
                } else {
                    getSingleMessage(stringResponse);
                }

                /* Abschließend wird der Adapter benachrichtigt */
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

    /* Die stringResponse enthält partner Daten und das message array. Es kann, sofern die Fahrt abgeschlossen
     * ist auch einen has_voted boolean enthalten. Die StringResponse wird in dieser Methode entsprechend verarbeitet
     * und die Nachrichten werden abschließend der ArrayList hinzugefügt  */
    private void getAllMessages(String stringResponse){

        try {

            Log.d("ChatActivity", "GET_ALL_MESSAGES: "+stringResponse);

            /* Bei der stringResponse handelt es sich im ein JSONObject, welches ein JSONArray beinhaltet */
            JSONObject response = new JSONObject(stringResponse);

            /* Es wird geprüft ob der boolean has_voted vorhanden ist */
            if(response.has("has_voted")){

                /* Ist dieser vorhanden wird die Methode setRatingsContainer initiiert */
                boolean hasVoted = response.getBoolean("has_voted");
                setRatingsContainer(hasVoted);
            }

            JSONObject partner = response.getJSONObject("partner");
            JSONArray messagesArray = response.getJSONArray("messages");

            partnerName = partner.getString("first_name")+" "+partner.getString("last_name");
            partnerPicture = partner.getString("picture");

            /* Das messages array wird durchlaufen und jede Nachricht der ArrayList hinzugefügt. Dabei ist zu beachten,
             * dass der message_text verschlüsselt ist und erst entschlüsselt werden muss */
            if(messagesArray.length() > 0) {

                for (int i = 0; i < messagesArray.length(); i++) {

                    JSONObject messageData = messagesArray.getJSONObject(i);

                    String authorId = messageData.getString("author_id"),
                            time = messageData.getString("time"),
                            date = messageData.getString("date"),
                            message = messageData.getString("message_text");
                    int sequence = messageData.getInt("sequence");
                    String name = authorId.equals(userId) ? userName : partnerName;

                    /* Falls die partnerUserID noch fehlt wird diese einmalig über die Messages bezogen. Die identifizierun dieser
                     * geschieht durch einen Vergleich mit der eigenen, bereits bekannten, userID */
                    if(partnerUserId == null && !authorId.equals(userId))
                        partnerUserId = authorId;

                    /* Um einer nullpointer vorzubeugen wird vor der entschlüsselung der Nachricht auf einen korrekten toznyHelper überprüft */
                    checkForTozny();
                    toznyHelper.decryptString(message);

                    messages.add(new MessagesEntry(authorId, name, time, date, toznyHelper.getDecryptedString(), sequence));
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /* Bei der stringResponse handelt es sich um ein JSONObject, welches die Informationen einer einzelnen Nachricht aufweist. Auch hier
     * wird die Nachricht vor einfügen in die ArrayList entschlüsselt */
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

            /* Um einer nullpointer vorzubeugen wird vor der entschlüsselung der Nachricht auf einen korrekten toznyHelper überprüft */
            checkForTozny();
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


    /* Wird die Aktivität zerstört muss der receiver abgemeldet werden */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myReceiver.unregister(ChatActivity.this);
    }

}
