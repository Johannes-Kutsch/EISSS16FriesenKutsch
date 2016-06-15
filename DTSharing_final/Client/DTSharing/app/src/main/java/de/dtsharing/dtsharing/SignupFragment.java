package de.dtsharing.dtsharing;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (ScrollView) inflater.inflate(R.layout.fragment_signup, container, false);

        _mail = (EditText) v.findViewById(R.id.etMail);
        _password1 = (EditText) v.findViewById(R.id.etPassword);
        _password2 = (EditText) v.findViewById(R.id.etPasswordRepeat);
        _firstName = (EditText) v.findViewById(R.id.etFirstName);
        _lastName = (EditText) v.findViewById(R.id.etLastName);
        _birthYear = (AutoCompleteTextView) v.findViewById(R.id.etBirthYear);

        _gender = (RadioGroup) v.findViewById(R.id.rgGender);

        _signup = (Button) v.findViewById(R.id.bSignup);

        if(birthYears.isEmpty()){
            addBirthYears();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, birthYears);
        _birthYear.setThreshold(1);
        _birthYear.setAdapter(adapter);

        _birthYear.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View convertView, int position, long l) {
                _birthYear.setText(parent.getItemAtPosition(position).toString());
                LoginFragment.hideSoftKeyboard(getActivity(), convertView);
            }
        });

        _signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final JSONObject data = new JSONObject();

                try {
                    data.put("mail", _mail.getText().toString());
                    data.put("password1", _password1.getText().toString());
                    data.put("password2", _password2.getText().toString());
                    data.put("firstName", _firstName.getText().toString());
                    data.put("lastName", _lastName.getText().toString());
                    data.put("birthYear", _birthYear.getText().toString());
                    data.put("gender", _gender.getCheckedRadioButtonId() == R.id.rbMale ? "Männlich" : "Weiblich");
                } catch (JSONException e) {
                    e.printStackTrace(); //sollte nicht eintreten
                }
                if(data.length() > 0)
                    if(verifyInput(data)){
                        submitData(data);
                    }
            }
        });

        return v;
    }

    private void addBirthYears(){
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = currentYear; i >= currentYear - 100; i--){
            birthYears.add(Integer.toString(i));
        }
    }

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

            if((!data.getString("password1").equals("")) && !data.getString("password2").equals("")) {
                if (!LoginFragment.isValidPassword(data.getString("password1"))) {
                    valid = false;
                    _password1.setError("Das Kennwort muss aus mindestens 6 Zeichen bestehen");
                } else {
                    if (!data.getString("password1").equals(data.getString("password2"))) {
                        valid = false;
                        _password2.setError("Kennwörter stimmen nicht überein");
                    }else{
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

        return valid;
    }

    private void submitData(final JSONObject data){

        String url = "http://10.0.2.2:3000/users";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObjResponse = new JSONObject(response);
                            System.out.println(response);
                            System.out.println(jsonObjResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

                int  statusCode = error.networkResponse.statusCode;
                NetworkResponse response = error.networkResponse;

                switch (statusCode) {
                    case 409:
                        System.out.println("errorcode 400!!");
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
                    params.put("birthYear", data.getString("birthYear"));
                    params.put("firstName", data.getString("firstName"));
                    params.put("name", data.getString("lastName"));
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

        Volley.newRequestQueue(getActivity()).add(postRequest);

    }

}
