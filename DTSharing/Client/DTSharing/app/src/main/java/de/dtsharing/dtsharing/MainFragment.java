package de.dtsharing.dtsharing;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private Button bSubmit, bReset;
    private EditText etDate, etTime;
    private Spinner spTicket;
    AutoCompleteTextView etStart, etDestination;

    ArrayAdapter adapterAutoComplete;
    ArrayList<String> stations = new ArrayList<>();

    private String spTicket_selected = "kein Ticket";
    private String url, urlBase = "http://192.168.0.15:3000/";

    /*Aktuelles Datum + Uhrzeit erhalten*/
    final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);
    int mHour = c.get(Calendar.HOUR_OF_DAY);
    int mMinute = c.get(Calendar.MINUTE);

    public MainFragment() {

    }

    //<--       OnCreate Start         -->
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_main Layout*/
        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.fragment_main, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        //etStart = (EditText) v.findViewById(R.id.etStart);
        etStart = (AutoCompleteTextView) v.findViewById(R.id.etStart);
        etDestination = (AutoCompleteTextView) v.findViewById(R.id.etDestination);
        etDate = (EditText) v.findViewById(R.id.etDate);
        etTime = (EditText) v.findViewById(R.id.etTime);
        spTicket = (Spinner) v.findViewById(R.id.spTicket);
        bSubmit = (Button) v.findViewById(R.id.bSubmit);
        bReset = (Button) v.findViewById(R.id.bReset);


        /*Eintragen der aktuellen Uhrzeit + Datum (mMonth + 1 da die Monate bei 0 beginnen)*/
        etDate.setText(String.format(Locale.US, "%02d-%02d-%04d", mDay, (mMonth+1), mYear));
        etTime.setText(String.format(Locale.US, "%02d:%02d", mHour, mMinute));

        adapterAutoComplete = new ArrayAdapter(getActivity().getBaseContext(),android.R.layout.simple_list_item_1, stations);
        etStart.setAdapter(adapterAutoComplete);
        etStart.setThreshold(1);
        etDestination.setAdapter(adapterAutoComplete);
        etDestination.setThreshold(1);

        getStations();


        /*Ticket Dialog item selected listener*/
        spinnerListener();

        /*OnClickListener Time, Datum, Submit, Reset*/
        etDate.setOnClickListener(this);
        etTime.setOnClickListener(this);
        bSubmit.setOnClickListener(this);
        bReset.setOnClickListener(this);

        return v;
    }
    //<--           OnCreate End           -->

    //<--           getStations Start           -->
    public void getStations(){
        url = urlBase+"get/stations";

        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        /*stations.add(response);
                        adapterAutoComplete.addAll(stations);
                        adapterAutoComplete.notifyDataSetChanged();*/
                        try {
                            for(int i=0;i<response.length();i++){
                                JSONObject json_data = response.getJSONObject(i);
                                stations.add(json_data.getString("stop_name"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        System.out.println("###FÜLLE ARRAY AUF###");
                        System.out.println(stations);
                        /*stations.add(response);*/
                        adapterAutoComplete.addAll(stations);
                        adapterAutoComplete.notifyDataSetChanged();
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        Volley.newRequestQueue(getActivity().getApplicationContext()).add(jsonRequest);
    }
    //<--           getStations End         -->

    //<--           Ticket Dialog Start          -->
    public void spinnerListener() {
        /*Erzeuge einen Item Selected Listener*/
        spTicket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /*Ein Eintrag wurde ausgewählt*/
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /*Erhalte String des ausgewählten Items*/
                spTicket_selected = adapterView.getItemAtPosition(i).toString();
            }
            /*Wenn nichts ausgewählt wurde.. Hier unwichtig aber muss überschrieben werden*/
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }
    //<--           Ticket Dialog End           -->

    //<--           Datum Dialog Start          -->
    public void showDateDialog(){
        /*Erzeuge den DatePickerDialog*/
        DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    /*Datum wurde gewählt und bestätigt*/
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        /*Aktualisiere Datum Variablen, da der Date Picker bei diesen beginnt*/
                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;

                        /*Trage Wert in das EditText ein (String.format um die )*/
                        etDate.setText(String.format(Locale.US, "%02d-%02d-%04d", dayOfMonth, (monthOfYear+1), year));
                    }
                }, mYear, mMonth, mDay);

        /*Setze Titel und zeige den Dialog*/
        dpd.setTitle("Datum wählen");
        dpd.show();
    }
    //<--           Datum Dialog End            -->

    //<--           Time Dialog Start           -->
    public void showTimeDialog(){
        /*Erzeuge den TimePickerDialog*/
        TimePickerDialog tpd = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {

                    /*Uhrzeit wurde gewählt und bestätigt*/
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {

                        /*Aktualisiere Uhrzeit Variablen, da der Time Picker bei diesen beginnt*/
                        mHour = selectedHour;
                        mMinute = selectedMinute;

                        /*Übergebe Zeit an getTime und trage den return Wert in das EditText ein*/
                        etTime.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
                    }
                }, mHour, mMinute, true);/*true => 24h Format*/

        /*Setze Titel und zeige den Dialog*/
        tpd.setTitle("Uhrzeit wählen");
        tpd.show();
    }
    //<--           Time Dialog End         -->

    //<--           OnClick Listener Start          -->
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //Submit Button pressed
            case R.id.bSubmit:
                /*Daten vor abschicken in final Variablen speichern*/
                final String start = etStart.getText().toString(),
                            destination = etDestination.getText().toString(),
                            ticket = spTicket_selected,
                            time = etTime.getText().toString(),
                            date = etDate.getText().toString();

                /*Prüfe ob Start oder Ziel leer sind*/
                if(start.equals("") || destination.equals("")){
                    Snackbar.make(view, "Fehler!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else{
                    /*Erzeuge die Matching Activity und füge Daten hinzu*/
                    Intent matchingIntent = new Intent(getActivity(), MatchingActivity.class);
                    matchingIntent.putExtra("start", start);
                    matchingIntent.putExtra("destination", destination);
                    matchingIntent.putExtra("ticket", ticket);
                    matchingIntent.putExtra("date", date);
                    matchingIntent.putExtra("time", time);
                    /*Starte Matching Activity*/
                    startActivity(matchingIntent);
                }
                break;

            //Reset Button pressed
            case R.id.bReset:
                /*Eingabefelder zurücksetzen*/
                etStart.setText("");
                etDestination.setText("");
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);
                etDate.setText(String.format(Locale.US, "%02d-%02d-%04d", mDay, (mMonth + 1), mYear));
                etTime.setText(String.format(Locale.US, "%02d:%02d", mHour, mMinute));
                break;

            //etDate pressed
            case R.id.etDate:
                showDateDialog();
                break;

            //etTime pressed
            case R.id.etTime:
                showTimeDialog();
                break;
        }
    }
    //<--           OnClick Listener End            -->
}
