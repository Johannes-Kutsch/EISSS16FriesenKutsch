package de.dtsharing.dtsharing;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ForgotPasswordFragment extends Fragment {

    RelativeLayout v;

    Snackbar snackbar;

    EditText _mail;
    Button _submit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_forgot_password, container, false);

        _mail = (EditText) v.findViewById(R.id.etMail);
        _submit = (Button) v.findViewById(R.id.bSubmit);

        _submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String mail = _mail.getText().toString().trim();
                if (!LoginFragment.isValidEmail(mail)){
                    _mail.setError("Keine gültige E-Mail Adresse");
                }else{
                    submitData(mail);
                }
            }
        });

        return v;
    }

    private void submitData(String mail){
        LoginFragment.hideSoftKeyboard(getActivity(), v);
        snackbar = Snackbar.make(v, "Eine E-Mail mit weiteren Instruktionen zum zurücksetzen deines Kennwortes wird in kürze bei dir eintreffen", Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.positive));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        snackbar.show();
    }

}
