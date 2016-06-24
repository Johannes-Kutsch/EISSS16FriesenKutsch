package de.dtsharing.dtsharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    RelativeLayout v;

    String token = null, base_url;

    public ProgressDialog progressDialog;

    Button _signin, _submitBaseUrl;
    TextView _forgotPassword, _signup, toolbar_title;
    EditText _mail, _password, _inputBaseUrl;

    private MyTokenReceiver receiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_login, container, false);

        /* Der SharedPrefs Helper wird erzeugt und die base_url deklariert */
        final SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(v.getContext());
        base_url = sharedPrefsManager.getBaseUrl();

        /* Views werden erfasst */
        _signup = (TextView) v.findViewById(R.id.tvSignup);
        _forgotPassword = (TextView) v.findViewById(R.id.tvForgotPassword);
        _signin = (Button) v.findViewById(R.id.bSignin);
        _mail = (EditText) v.findViewById(R.id.etMail);
        _password = (EditText) v.findViewById(R.id.etPassword);
        _inputBaseUrl = (EditText) v.findViewById(R.id.inputBaseUrl);
        _submitBaseUrl = (Button) v.findViewById(R.id.submitBaseUrl);

        /* ViewPager wird bei onClick auf Registrieren auf Seite 2 (rechts) gestellt */
        _signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivity)getActivity()).setCurrentPage(2);
            }
        });

        /* ViewPager wird bei onClick auf Kennwort vergessen auf Seite 0 (links) gestellt */
        _forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivity)getActivity()).setCurrentPage(0);
            }
        });

        /* Bei onClick auf Anmelden werden die Daten verifiziert und abschließend findet die Anmeldung statt */
        _signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Software Tastatur wird eingefahren */
                hideSoftKeyboard(getActivity(), view);

                /* Auslesen von E-Mail und Kennwort */
                final String mail = _mail.getText().toString().trim(),
                        password = _password.getText().toString();

                /* E-Mail und Kennwort werden auf formale Korrektheit überprüft */
                if(verifyInput(mail, password)){

                    /* E-Mail und md5 gehashtes Kennwort werden an den Server gesendet */
                    submitData(mail, HashString.md5(password));
                }
            }
        });

        /* Aktuelle base_ip wird aus den SharedPrefs ausgelesen und in das zugehörige Feld eingetragen */
        _inputBaseUrl.setText(sharedPrefsManager.getBaseIP());

        /* Bei onClick auf submitBaseUrl wird die base_url in den SharedPrefs aktualisiert */
        _submitBaseUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Es wird überprüft ob das Eingabefeld leer ist */
                if(!_inputBaseUrl.getText().toString().trim().equals("")){

                    /* SharedPrefs Helper übernimmt die Aktualisierung der base_url */
                    sharedPrefsManager.setBaseUrl(_inputBaseUrl.getText().toString().trim());

                    /* Software Tastatur wird eingefahren */
                    hideSoftKeyboard(getActivity(), view);

                    /* Snackbar mit Erfolgsnachricht und bitte zum neustart wird angezeigt */
                    Snackbar snackbar = Snackbar.make(v, "IP-Adresse geändert\nBitte Applikation neustarten", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.positive));
                    TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    snackbar.show();

                }
            }
        });

        /* Der Broadcast receiver wird registriert und durch den Filter für den Empfang des FCM Tokens festgelegt */
        IntentFilter filter = new IntentFilter("OnTokenRefresh");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyTokenReceiver();
        receiver.register(v.getContext(), filter);

        return v;

    }

    /* E-Mail und Kennwort werden verifiziert */
    private boolean verifyInput(String mail, String password){
        boolean valid = true;

        /* E-Mail wird auf Merkmale einer typischen E-Mail überprüft */
        if(!isValidEmail(mail)){
            valid = false;
            _mail.setError("Keine gültige E-Mail Adresse");
        }

        /* Kennwort wird auf die mindestlänge von 6 Zeichen überprüft */
        if(!isValidPassword(password)){
            valid = false;
            _password.setError("Ungültiges Kennwort");
        }

        return valid;
    }

    /* E-Mail und md5 gehashtes Kennwort werden an den Server gesendet */
    private void submitData(final String mail, final String password){

        /* URI wird spezifiziert */
        final String URI = base_url+"/sessions";

        /* ProgressDialog wird vorbereitet und dargestellt */
        progressDialog = new ProgressDialog(v.getContext(),
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_circle, null));
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Es wird ein Preis für deine Daten ermittelt...");
        progressDialog.show();

        /* Das FCM Token wird aus den SharedPrefs ausgelesen */
        token = new SharedPrefsManager(v.getContext()).getFCMToken();

        /* Ein POST-Request an die URI mit dem Body E-Mail, Kennwort und FCM Token */
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
                new Response.Listener<String>() {

                    /* Bei erfolgreichem Login werden dem Benutzer zugehörige Daten aus der Datenbank zurückgegeben */
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String user_id = jsonObject.getString("_id"),
                                    picture = jsonObject.getString("picture"),
                                    firstName = jsonObject.getString("first_name"),
                                    lastName = jsonObject.getString("last_name"),
                                    more = jsonObject.getString("more"),
                                    interests = jsonObject.getString("interests");

                            /* Ein neues Intent wird erzeugt. Ziel: MainActivity */
                            Intent mainIntent = new Intent(v.getContext(), MainActivity.class);

                            /* Intent wird mit Daten aus der Response angereichert */
                            mainIntent.putExtra("cameFromLogin", true);
                            mainIntent.putExtra("user_id", user_id);
                            mainIntent.putExtra("picture", picture);
                            mainIntent.putExtra("firstName", firstName);
                            mainIntent.putExtra("lastName", lastName);
                            mainIntent.putExtra("more", more);
                            mainIntent.putExtra("interests", interests);
                            startActivity(mainIntent);

                            /* Login Aktivität wird durch finish() terminiert um zu vermeiden, dass der Benutzer durch drücken
                             * auf Zurück zu dieser zurück gelangt */
                            getActivity().finish();

                            /* ProgressDialog wird beendet */
                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

                int  statusCode = 0;
                if(error.networkResponse != null)
                    statusCode = error.networkResponse.statusCode;

                /* Bei einem Fehler wird der ProgressDialog, falls noch offen, beendet */
                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();

                /* Anschließend wird anhand des StatusCodes auf die Art des Fehler hingewiesen */
                switch (statusCode) {

                    /* E-Mail Kennwort Kombination ist nicht korrekt */
                    case 403:
                        _password.setError("Die E-Mail und Kennwort Kombination ist nicht im System vorhanden");
                        break;

                    /* Serverseitiger Fehler. Es wird nur eine Snackbar mit dem Hinweis erzeugt */
                    case 500:
                        Snackbar snackbar = Snackbar.make(v, "Fehler im System. Versuche es später erneut", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", null);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        snackbar.show();
                        break;
                }

            }
        })
        {
            /*Daten welche der Post-Request mitgegeben werden*/
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("email", mail);
                params.put("pass", password);
                params.put("token", token);

                Log.d("LoginFragment", "FCM TOKEN: "+params.toString());

                return params;
            }

        };

        /* Request wird in die Queue von Volley eingeführt */
        Volley.newRequestQueue(getActivity()).add(postRequest);
    }

    /* Methode zum verstecken der Software Tastatur */
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    /* http://stackoverflow.com/a/7882950
    * Methode zur einfachen Überprüfung einer validen E-Mail */
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /* Kennwort wird lediglich auf Vorhandenheit und mindestlänge überprüft */
    public final static boolean isValidPassword(CharSequence target) {
        return !TextUtils.isEmpty(target) && target.length() >= 6;
    }

    /* Wenn die Aktivität durch finish() abgeschlossen wird muss der Broadcast Receiver abgemeldet werden */
    @Override
    public void onDestroy() {
        super.onDestroy();
        receiver.unregister(v.getContext());
    }

    /* CustomReceiver zum empfangen des FCM Tokens */
    public class MyTokenReceiver extends BroadcastReceiver {
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

        /* Entspricht die Aktion des Broadcasts der hier spezifizierten wird der erhaltene FCM Token in die
        * SharedPrefs gespeichert */
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("OnTokenRefresh")) {
                token = intent.getStringExtra("token");
                new SharedPrefsManager(v.getContext()).setFCMToken(token);
            }
        }
    }

}
