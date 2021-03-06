package de.dtsharing.dtsharing;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SignupFragment extends Fragment {

    ScrollView v;

    EditText _mail, _password1, _password2, _firstName, _lastName;
    AutoCompleteTextView _birthYear;
    RadioButton _male, _female;
    RadioGroup _gender;
    Button _signup;

    List<String> birthYears = new ArrayList<String>();
    int currentTicketIndex = -1;
    String base_url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (ScrollView) inflater.inflate(R.layout.fragment_signup, container, false);

        /* base_url wird aus den SharedPrefs bezogen */
        base_url = new SharedPrefsManager(v.getContext()).getBaseUrl();

        /* Views werden erfasst */
        _mail = (EditText) v.findViewById(R.id.etMail);
        _password1 = (EditText) v.findViewById(R.id.etPassword);
        _password2 = (EditText) v.findViewById(R.id.etPasswordRepeat);
        _firstName = (EditText) v.findViewById(R.id.etFirstName);
        _lastName = (EditText) v.findViewById(R.id.etLastName);
        _birthYear = (AutoCompleteTextView) v.findViewById(R.id.etBirthYear);
        _gender = (RadioGroup) v.findViewById(R.id.rgGender);
        _signup = (Button) v.findViewById(R.id.bSignup);

        /* Falls die List<String> keine Einträge enthält, wird diese gefüllt */
        if(birthYears.isEmpty()){
            addBirthYears();
        }

        /* Der Adapter für das AutoComplete EditText birthYear wird erzeugt. Ein Standard Layout wird zugewiesen. Ebenso die Datenquelle (List<String> birthYears)
         * Abschließend wird das AutoComplete EditText mit dem Adapter verbunden */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, birthYears);
        _birthYear.setThreshold(1);
        _birthYear.setAdapter(adapter);

        /* Bei Auswahl eines Items aus dem AutoComplete wird diese in das Input Feld eingetragen.
         * Software Tastatur wird versteckt für eine freie Sicht auf das Formular */
        _birthYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View convertView, int position, long l) {
                _birthYear.setText(parent.getItemAtPosition(position).toString());
                LoginFragment.hideSoftKeyboard(getActivity(), convertView);
            }
        });

        /* onClick für den Registrieren Button */
        _signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final JSONObject data = new JSONObject();

                /* Ein JSONObject wird mit den Daten des Formulars vorbereitet */
                try {
                    data.put("mail", _mail.getText().toString().trim());
                    data.put("password1", _password1.getText().toString());
                    data.put("password2", _password2.getText().toString());
                    data.put("firstName", _firstName.getText().toString().trim());
                    data.put("lastName", _lastName.getText().toString().trim());
                    data.put("birthYear", _birthYear.getText().toString());
                    data.put("gender", _gender.getCheckedRadioButtonId() == R.id.rbMale ? "Männlich" : "Weiblich");
                } catch (JSONException e) {
                    e.printStackTrace(); //sollte nicht eintreten
                }

                /* Das data object wird verifiziert und abschließend an den Server gesandt */
                if(data.length() > 0)
                    if(verifyInput(data)){
                        submitData(data);
                    }
            }
        });

        return v;
    }

    /* Simple Methode zum füllen der birthYear Liste. Das aktuelle Datum - 100 Jahre wird durchlaufen */
    private void addBirthYears(){
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = currentYear; i >= currentYear - 100; i--){
            birthYears.add(Integer.toString(i));
        }
    }

    /* Methode zum verifizieren alle eingegebenen Daten
     * Entspricht eine Eingabe nicht den Richtlinien wird diese entsprechend markiert und der Benutzer wird auf die Art
     * des Fehlers hingewiesen */
    private boolean verifyInput(JSONObject data){
        boolean valid = true;

        try {

            if (data.getString("mail").equals("")) {
                valid = false;
                _mail.setError(getString(R.string.notEmpty));
            } else {
                if(!LoginFragment.isValidEmail(data.getString("mail"))) {
                    valid = false;
                    _mail.setError("Keine gültige E-Mail Adresse");
                }
            }

            if (data.getString("password1").equals("")) {
                valid = false;
                _password1.setError(getString(R.string.notEmpty));
            }

            if (data.getString("password2").equals("")) {
                valid = false;
                _password2.setError(getString(R.string.notEmpty));
            }

            if (data.getString("firstName").equals("")) {
                valid = false;
                _firstName.setError(getString(R.string.notEmpty));
            }

            if (data.getString("lastName").equals("")) {
                valid = false;
                _lastName.setError(getString(R.string.notEmpty));
            }

            if (data.getString("birthYear").equals("")) {
                valid = false;
                _birthYear.setError(getString(R.string.notEmpty));
            } else {
                if(!birthYears.contains(data.getString("birthYear"))) {
                    valid = false;
                    _birthYear.setError("Kein gültiges Geburtsjahr");
                }
            }

            /* Es wird überprüft ob beide Kennwörter angegeben wurden */
            if((!data.getString("password1").equals("")) && !data.getString("password2").equals("")) {

                /* Lediglich das Kennwort 1 wird verifiziert, da das Kennwort 2 diesem ja entsprechen muss */
                if (!LoginFragment.isValidPassword(data.getString("password1"))) {
                    valid = false;
                    _password1.setError("Das Kennwort muss aus mindestens 6 Zeichen bestehen");
                } else {

                    /* Es wird überprüft ob beide Kennwörter übereinstimmen */
                    if (!data.getString("password1").equals(data.getString("password2"))) {
                        valid = false;
                        _password2.setError("Kennwörter stimmen nicht überein");
                    }else{

                        /* Kennwort erfüllt alle Voraussetzungen. Kennwort 1 wird MD5 gehashed. Anschließend werden Kennwort 1 und 2 entfernt und durch
                         * dieses ersetzt  */
                        String md5Password = HashString.md5(data.getString("password1"));
                        data.put("password", md5Password);
                        data.remove("password1");
                        data.remove("password2");
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace(); //sollte nicht eintreten
        }

        /* Wenn valid == true werden die Daten an den Server gesandt */
        return valid;
    }

    /* Daten wurden verifiziert und werden an den Server gesandt */
    private void submitData(final JSONObject data){

        String URI = base_url+"/users";

        /* Ein POST-Request mit den Verifizierten Formulardaten wird an den Server gesandt */
        StringRequest postRequest = new StringRequest(Request.Method.POST, URI,
                new Response.Listener<String>() {

                    /* Bei einer erfolgreichen Response wird eine positive Snackbar mit entsprechender Meldung angezeigt
                     * Anschleßend wird wieder auf Seite 1 (Login) navigiert */
                    @Override
                    public void onResponse(String response) {
                        Snackbar snackbar = Snackbar.make(v, "Account wurde erfolgreich erstellt", Snackbar.LENGTH_LONG)
                                .setAction("Action", null);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.positive));
                        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        snackbar.show();

                        ((LoginActivity)getActivity()).setCurrentPage(1);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

                int statusCode = 0;

                if(error.networkResponse != null) {
                    statusCode = error.networkResponse.statusCode;
                }

                /* Bei einem Fehler wird je nach Statuscode weiter verfahren */
                switch (statusCode) {

                    /* E-Mail Adresse ist bereits vergeben und wird dem Benutzer als Error im Feld "E-Mail" angezeigt */
                    case 409:
                        _mail.setError("Diese E-Mail Adresse ist bereits vergeben");
                        break;

                    /* Serverseitiger Fehler. Negative Snackbar mit entsprechendem Hinweis wird angezeigt */
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
                try {
                    params.put("birth_year", data.getString("birthYear"));
                    params.put("first_name", data.getString("firstName"));
                    params.put("last_name", data.getString("lastName"));
                    params.put("gender", data.getString("gender"));
                    params.put("email", data.getString("mail"));
                    params.put("pass", data.getString("password"));
                    params.put("interests", "");
                    params.put("more", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }

        };

        /* Request wird der Volley Queue hinzugefügt und abgearbeitet */
        Volley.newRequestQueue(getActivity()).add(postRequest);

    }

}
