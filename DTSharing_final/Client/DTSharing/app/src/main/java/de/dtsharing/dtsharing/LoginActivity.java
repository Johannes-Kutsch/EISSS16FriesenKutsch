package de.dtsharing.dtsharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    Toolbar toolbar;
    Button _signin;
    TextView _forgotPassword, _signup, toolbar_title;
    EditText _mail, _password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar_title = (TextView) (toolbar != null ? toolbar.findViewById(R.id.toolbar_title) : null);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            /*Deaktiviere Titel da Custom Titel*/
            actionBar.setDisplayShowTitleEnabled(false);
        }

        _signup = (TextView) findViewById(R.id.tvSignup);
        _forgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        _signin = (Button) findViewById(R.id.bSignin);
        _mail = (EditText) findViewById(R.id.etMail);
        _password = (EditText) findViewById(R.id.etPassword);


        _signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signupIntent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(signupIntent);
            }
        });
        _forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forgotPasswordIntent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(forgotPasswordIntent);
            }
        });
        _signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideSoftKeyboard(LoginActivity.this, view);

                final String mail = _mail.getText().toString(),
                        password = _password.getText().toString();

                if(verifyInput(mail, password)){
                    submitData(mail, password);
                }
            }
        });
    }

    private boolean verifyInput(String mail, String password){
        boolean valid = true;

        if(!isValidEmail(mail)){
            valid = false;
            _mail.setError("Bitte eine korrekte E-Mail Adresse eingeben!");
        }

        if(!isValidPassword(password)){
            valid = false;
            _password.setError("Ungültiges Kennwort");
        }

        return valid;
    }

    private void submitData(String mail, String password){

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
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
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
                finish();
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
