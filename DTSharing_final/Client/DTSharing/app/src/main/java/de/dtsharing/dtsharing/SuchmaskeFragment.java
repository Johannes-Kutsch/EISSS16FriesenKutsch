package de.dtsharing.dtsharing;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SuchmaskeFragment extends Fragment {

    private static final String LOG_TAG = SuchmaskeFragment.class.getSimpleName();

    FrameLayout v;
    Snackbar snackbar;
    private NonScrollListView lvHistory;
    private Button bSubmit;
    private EditText etDate, etTime, etTicket;
    private AutoCompleteTextView etDeparture, etTarget;
    private ScrollView svContainer;

    private ArrayList<HistoryEntry> transit = new ArrayList<>();
    private ArrayAdapter adapterAutoComplete;
    private HistoryAdapter mAdapter;

    private Boolean hasTicket;
    private CharSequence[] ticketDialogArray;
    private int currentTicketIndex = -1;
    public static final int REQUEST_CODE_TRIPS = 1;

    /*Aktuelles Datum + Uhrzeit erhalten*/
    final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);
    int mHour = c.get(Calendar.HOUR_OF_DAY);
    int mMinute = c.get(Calendar.MINUTE);

    public SuchmaskeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (FrameLayout) inflater.inflate(R.layout.fragment_suchmaske, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvHistory = (NonScrollListView) v.findViewById(R.id.lvHistory);
        svContainer = (ScrollView) v.findViewById(R.id.svContainer);
        etDeparture = (AutoCompleteTextView) v.findViewById(R.id.etDeparture);
        etTarget = (AutoCompleteTextView) v.findViewById(R.id.etTarget);
        etDate = (EditText) v.findViewById(R.id.etDate);
        etTime = (EditText) v.findViewById(R.id.etTime);
        etTicket = (EditText) v.findViewById(R.id.etTicket);
        bSubmit = (Button) v.findViewById(R.id.bSubmit);

        /*Eintragen der aktuellen Uhrzeit + Datum (mMonth + 1 da die Monate bei 0 beginnen)*/
        etDate.setText(String.format(Locale.US, "%02d.%02d.%04d", mDay, (mMonth+1), mYear));
        etTime.setText(String.format(Locale.US, "%02d:%02d", mHour, mMinute));

        ticketDialogArray = getResources().getStringArray(R.array.ticket_spinner);

        /*Erzeuge und verbinde Adapter mit der History ListView*/
        mAdapter = new HistoryAdapter(getContext(), transit);
        lvHistory.setAdapter(mAdapter);

        /*Erzeuge Adapter für Autocomplete*/
        adapterAutoComplete = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1);

        /*Koppel AutoCompleteTextViews mit dem Adapter*/
        etDeparture.setAdapter(adapterAutoComplete);
        etDeparture.setThreshold(1);
        etTarget.setAdapter(adapterAutoComplete);
        etTarget.setThreshold(1);

        /*Fülle Array mit Beispieldaten*/
        prepareVerlaufData();

        etTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
                builder.setTitle("Ich besitze...");
                builder.setSingleChoiceItems(ticketDialogArray, currentTicketIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        etTicket.setText(ticketDialogArray[position]);
                        etTicket.setError(null);
                        currentTicketIndex = position;
                        hasTicket = position != 0;
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });
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

                boolean valid = true;

                /*Prüfe ob Start oder Ziel leer sind*/

                if (departureName.equals("")) {
                    valid = false;
                    etDeparture.setError("Darf nicht leer sein!");
                }
                if (targetName.equals("")) {
                    valid = false;
                    etTarget.setError("Darf nicht leer sein!");
                }

                if(etTicket.getText().toString().equals("")){
                    valid = false;
                    etTicket.setError("Bitte ein Ticket wählen!");
                }

                if(valid){
                    /*Erzeuge die Matching Activity und füge Daten hinzu*/
                    Intent tripsIntent = new Intent(getContext(), TripsActivity.class);
                    tripsIntent.putExtra("departureName", departureName);
                    tripsIntent.putExtra("targetName", targetName);
                    tripsIntent.putExtra("hasTicket", hasTicket);
                    tripsIntent.putExtra("date", date);
                    tripsIntent.putExtra("time", time);
                    /*Starte Matching Activity*/
                    startActivityForResult(tripsIntent, REQUEST_CODE_TRIPS);
                }
            }
        });

        /*History item selected listener*/
        historyOnClickListener();

        getStationData();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_TRIPS && resultCode == Activity.RESULT_CANCELED) {
            String message = data.getStringExtra("message");
            snackbar = Snackbar.make(v, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            snackbar.show();
        }

    }

    //<--           prepareVerlaufData Start          -->
    private void prepareVerlaufData(){
        transit.add(new HistoryEntry("Köln, Hansaring", "Gummersbach Bf"));
        transit.add(new HistoryEntry("Köln Hbf", "Paderborn Hbf"));
        transit.add(new HistoryEntry("Paderborn Hbf", "Bad Driburg Bahnhof"));
        transit.add(new HistoryEntry("Paderborn Hbf", "Hamm Hbf"));
        transit.add(new HistoryEntry("Hamm Hbf", "Köln Hbf"));

        /*Benachrichtige Adapter über Änderungen*/
        mAdapter.notifyDataSetChanged();
    }
    //<--           prepareVerlaufData End            -->


    //<--           Datum Dialog Start          -->
    public void showDateDialog(){
        /*Erzeuge den DatePickerDialog*/

        DatePickerDialog dpd = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

                        /*Aktualisiere Datum Variablen, da der Date Picker bei diesen beginnt*/
                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;

                        /*Trage Wert in das EditText ein (String.format um die )*/
                        etDate.setText(String.format(Locale.US, "%02d.%02d.%04d", dayOfMonth, (monthOfYear+1), year));
                    }
                },mYear, mMonth, mDay);

        dpd.setTitle("Datum wählen");
        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");

    }
    //<--           Datum Dialog End            -->

    //<--           Time Dialog Start           -->
    public void showTimeDialog(){
        /*Erzeuge den TimePickerDialog*/
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {

                    /*Uhrzeit wurde gewählt und bestätigt*/
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int selectedHour, int selectedMinute, int selectedSecond) {
                        /*Aktualisiere Uhrzeit Variablen, da der Time Picker bei diesen beginnt*/
                        mHour = selectedHour;
                        mMinute = selectedMinute;

                        /*Übergebe Zeit an getTime und trage den return Wert in das EditText ein*/
                        etTime.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
                    }

                }, mHour, mMinute, true);/*true => 24h Format*/

        /*Setze Titel und zeige den Dialog*/
        tpd.setTitle("Uhrzeit wählen");
        tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
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

                etDeparture.setError(null);
                etTarget.setError(null);

                svContainer.smoothScrollTo(0,0);

                Snackbar.make(view, "Eintrag gewählt", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }
    //<--           History onClickListener End           -->

    private void getStationData(){

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db;
                db = getContext().openOrCreateDatabase("Stops", Context.MODE_PRIVATE, null);
                Cursor cursor = db.rawQuery("SELECT stop_name FROM vrs", null);

                if(cursor.getCount() == 0){
                    Log.d(LOG_TAG, "Keine Einträge gefunden");
                }
                while(cursor.moveToNext()){
                    String str = cursor.getString(0);
                    //str = str.replace(",", "")
                    adapterAutoComplete.add(str);
                }
                cursor.close();
                db.close();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterAutoComplete.notifyDataSetChanged();
                    }
                });

            }
        });
        myThread.start();

    }

}
