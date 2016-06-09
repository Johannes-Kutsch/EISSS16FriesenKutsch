package de.dtsharing.dtsharing;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class Suchmaske extends Fragment {

    RelativeLayout v;
    private NonScrollListView lvHistory;
    private Button bSubmit;
    private EditText etDate, etTime;
    private AutoCompleteTextView etDeparture, etTarget;
    private Spinner spTicket;
    private ScrollView svContainer;

    private ArrayList<HistoryEntry> transit = new ArrayList<>();
    private HistoryAdapter mAdapter;

    private Boolean spTicket_selected = false;

    /*Aktuelles Datum + Uhrzeit erhalten*/
    final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);
    int mHour = c.get(Calendar.HOUR_OF_DAY);
    int mMinute = c.get(Calendar.MINUTE);

    public Suchmaske() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_suchmaske, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvHistory = (NonScrollListView) v.findViewById(R.id.lvHistory);
        svContainer = (ScrollView) v.findViewById(R.id.svContainer);
        etDeparture = (AutoCompleteTextView) v.findViewById(R.id.etDeparture);
        etTarget = (AutoCompleteTextView) v.findViewById(R.id.etTarget);
        etDate = (EditText) v.findViewById(R.id.etDate);
        etTime = (EditText) v.findViewById(R.id.etTime);
        spTicket = (Spinner) v.findViewById(R.id.spTicket);
        bSubmit = (Button) v.findViewById(R.id.bSubmit);

        /*Eintragen der aktuellen Uhrzeit + Datum (mMonth + 1 da die Monate bei 0 beginnen)*/
        etDate.setText(String.format(Locale.US, "%02d-%02d-%04d", mDay, (mMonth+1), mYear));
        etTime.setText(String.format(Locale.US, "%02d:%02d", mHour, mMinute));

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        v.post(new Runnable() {

            @Override
            public void run() {
                /*Erzeuge und verbinde Adapter mit der History ListView*/
                mAdapter = new HistoryAdapter(getActivity().getBaseContext(), transit);
                lvHistory.setAdapter(mAdapter);
            }

        });

        /*Fülle Array mit Beispieldaten*/
        prepareVerlaufData();

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog();
            }
        });
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeDialog();
            }
        });
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String departureName = etDeparture.getText().toString(),
                        targetName = etTarget.getText().toString(),
                        time = etTime.getText().toString(),
                        date = etDate.getText().toString();
                final Boolean hasTicket = spTicket_selected;

                /*Prüfe ob Start oder Ziel leer sind*/
                if(departureName.equals("") || targetName.equals("")){
                    Snackbar.make(view, "Fehler!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else{
                    /*Erzeuge die Matching Activity und füge Daten hinzu*/
                    Intent tripsIntent = new Intent(getActivity(), TripsActivity.class);
                    tripsIntent.putExtra("departureName", departureName);
                    tripsIntent.putExtra("targetName", targetName);
                    tripsIntent.putExtra("hasTicket", hasTicket);
                    tripsIntent.putExtra("date", date);
                    tripsIntent.putExtra("time", time);
                    /*Starte Matching Activity*/
                    startActivity(tripsIntent);
                }
            }
        });

        /*Ticket Dialog item selected listener*/
        spinnerListener();

        /*History item selected listener*/
        historyOnClickListener();

        return v;
    }

    //<--           prepareVerlaufData Start          -->
    private void prepareVerlaufData(){
        transit.add(new HistoryEntry("Gummersbach Bf", "Köln Hbf"));
        transit.add(new HistoryEntry("Köln Hbf", "Paderborn Hbf"));
        transit.add(new HistoryEntry("Paderborn Hbf", "Bad Driburg Bahnhof"));
        transit.add(new HistoryEntry("Paderborn Hbf", "Hamm Hbf"));
        transit.add(new HistoryEntry("Hamm Hbf", "Köln Hbf"));

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        v.post(new Runnable() {

            @Override
            public void run() {
                /*Benachrichtige Adapter über Änderungen*/
                mAdapter.notifyDataSetChanged();
            }

        });
    }
    //<--           prepareVerlaufData End            -->

    //<--           Ticket Dialog Start          -->
    public void spinnerListener() {
        /*Erzeuge einen Item Selected Listener*/
        spTicket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /*Ein Eintrag wurde ausgewählt*/
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*Ticket vorhanden?*/
                if(position == 0)
                    spTicket_selected = false;
                else
                    spTicket_selected = true;

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

    //<--           History onClickListener Start          -->
    public void historyOnClickListener() {
        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent,
                                    View view, int position, long id) {

                HistoryEntry historyEntry = transit.get(position);

                etDeparture.setText(historyEntry.getDeparture());
                etTarget.setText(historyEntry.getTarget());

                svContainer.smoothScrollTo(0,0);

                Snackbar.make(view, "Eintrag gewählt", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }
    //<--           History onClickListener End           -->

}
