package de.dtsharing.dtsharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    RelativeLayout v;

    Button _signin;
    TextView _forgotPassword, _signup, toolbar_title;
    EditText _mail, _password;

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
                    submitData(mail, password);
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

    private void submitData(String mail, String password){

        final ProgressDialog progressDialog = new ProgressDialog(v.getContext(),
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setIndeterminateDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.progress_circle, null));
        //progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Es wird ein Preis für deine Daten ermitteln...");
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Intent mainIntent = new Intent(v.getContext(), MainActivity.class);
                startActivity(mainIntent);
                getActivity().finish();
            }
        }, 5000);

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
