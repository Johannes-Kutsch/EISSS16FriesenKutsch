package de.dtsharing.dtsharing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button _submit;
    TextView _forgotPassword, _signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*Setze default Fragment*/
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new LoginFragment());
        ft.commit();

        _signup = (TextView) findViewById(R.id.bSignup);
        _forgotPassword = (TextView) findViewById(R.id.bForgot);

        _signup.setOnClickListener(this);
        _forgotPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (view.getId()){
            case R.id.bForgot:
                fragment = new ForgotPasswordFragment();
                title = "Kennwort vergessen";
                break;
            case  R.id.bSignin:
                fragment = new SignupFragment();
                title = "Registrieren";
                break;
        }

        if(fragment != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
        }

        /*Ändere den Titel*/
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}
