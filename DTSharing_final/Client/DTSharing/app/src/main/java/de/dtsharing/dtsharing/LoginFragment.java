package de.dtsharing.dtsharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

    public ProgressDialog progressDialog;

    Button _signin;
    TextView _forgotPassword, _signup, toolbar_title;
    EditText _mail, _password;

    MyFirebaseInstanceIDService myFirebaseInstanceIDService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_login, container, false);

        _signup = (TextView) v.findViewById(R.id.tvSignup);
        _forgotPassword = (TextView) v.findViewById(R.id.tvForgotPassword);
        _signin = (Button) v.findViewById(R.id.bSignin);
        _mail = (EditText) v.findViewById(R.id.etMail);
        _password = (EditText) v.findViewById(R.id.etPassword);

        myFirebaseInstanceIDService = new MyFirebaseInstanceIDService();
        myFirebaseInstanceIDService.onTokenRefresh();

        _signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivity)getActivity()).setCurrentPage(2);
            }
        });

        _forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivity)getActivity()).setCurrentPage(0);
            }
        });

        _signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideSoftKeyboard(getActivity(), view);

                final String mail = _mail.getText().toString(),
                        password = _password.getText().toString();

                if(verifyInput(mail, password)){
                    submitData(mail, HashString.md5(password));
                }
            }
        });

        return v;

    }

    private boolean verifyInput(String mail, String password){
        boolean valid = true;

        if(!isValidEmail(mail)){
            valid = false;
            _mail.setError("Keine gültige E-Mail Adresse");
        }

        if(!isValidPassword(password)){
            valid = false;
            _password.setError("Ungültiges Kennwort");
        }

        return valid;
    }

    private void submitData(final String mail, final String password){

        String base_url = getResources().getString(R.string.base_url);
        final String url = base_url+"/sessions";

        progressDialog = new ProgressDialog(v.getContext(),
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_circle, null));
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Es wird ein Preis für deine Daten ermittelt...");
        progressDialog.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
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

                            Intent mainIntent = new Intent(v.getContext(), MainActivity.class);
                            mainIntent.putExtra("cameFromLogin", true);
                            mainIntent.putExtra("user_id", user_id);
                            mainIntent.putExtra("picture", picture);
                            mainIntent.putExtra("firstName", firstName);
                            mainIntent.putExtra("lastName", lastName);
                            mainIntent.putExtra("more", more);
                            mainIntent.putExtra("interests", interests);
                            startActivity(mainIntent);
                            getActivity().finish();

                            progressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

                int  statusCode = error.networkResponse.statusCode;

                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();

                switch (statusCode) {
                    case 403:
                        _password.setError("Die E-Mail und Kennwort Kombination ist nicht im System vorhanden");
                        break;
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
                params.put("token", myFirebaseInstanceIDService.getToken());

                Log.d("LoginFragment", "FCM TOKEN: "+params.toString());

                return params;
            }

        };

        Volley.newRequestQueue(getActivity()).add(postRequest);
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public final static boolean isValidPassword(CharSequence target) {
        return !TextUtils.isEmpty(target) && target.length() >= 6;
    }

}
