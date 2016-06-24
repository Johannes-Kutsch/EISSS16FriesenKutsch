package de.dtsharing.dtsharing;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class SuchmaskeFragment extends Fragment {

    private static final String LOG_TAG = SuchmaskeFragment.class.getSimpleName();

    FrameLayout v;
    Snackbar snackbar;
    private NonScrollListView lvHistory;
    private Button bSubmit;
    private TextView noHistoryContainer, radiusSearch, swapStations;
    private EditText etDate, etTime, etTicket;
    private AutoCompleteTextView etDeparture, etTarget;
    private ScrollView svContainer;

    private ArrayList<HistoryEntry> transit = new ArrayList<>();
    private String[] stopsInRadius;
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
        noHistoryContainer = (TextView) v.findViewById(R.id.noHistoryContainer);
        radiusSearch = (TextView) v.findViewById(R.id.radiusSearch);
        swapStations = (TextView) v.findViewById(R.id.swapStations);
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

        /* Stop_names werden aus der Lokalen Datenbank abgerufen */
        getStationData();

        /* Verlauf wird aus der Loaklen Datenbank abgerufen */
        getHistoryData();

        /* onItemClickListener für Start und Ziel AutoCompleteItems damit die Software Tastatur versteckt wird */
        etDeparture.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                etDeparture.setText(adapterView.getItemAtPosition(position).toString());
                hideSoftKeyboard(getActivity(), view);
            }
        });
        etTarget.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                etTarget.setText(adapterView.getItemAtPosition(position).toString());
                hideSoftKeyboard(getActivity(), view);
            }
        });

        /* onClick swapStations. Start und Zielhaltestelle werden getauscht */
        swapStations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Zwischensichern eines Feldes und anschließendes tauschen*/
                String tmpHolder = etDeparture.getText().toString();
                etDeparture.setText(etTarget.getText().toString());
                etTarget.setText(tmpHolder);
            }
        });

        /* onClick für radiusSearch */
        radiusSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Aktuelle Position wird ermittelt */
                GPSTracker mGPS = new GPSTracker(v.getContext());
                if(mGPS.canGetLocation()){

                    /* die RadiusSearch wird mit aktuellen Positionsdaten erzeugt. Diese ermittelt alle stops in einem Umkreis von 2 km
                     * und gibt diese als String[] Array zurück. Dieses Array dient als Datenquelle für den AlertDialog */
                    stopsInRadius = new RadiusSearch(v.getContext()).startRadiusSearch(mGPS.getLatitude(), mGPS.getLongitude());

                    /* Die Nutzung von GPS wird nach abgeschlossener Ermittlung gestoppt um den Akku zu schonen */
                    mGPS.stopUsingGPS();

                    /* AlertDialog mit allen Haltestellen in einem Umkreis von 2 km wird erzeugt und angezeigt */
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
                    builder.setTitle("Haltestellen in einem Umkreis von 2 km");
                    builder.setIcon(R.drawable.ic_my_location_24dp);
                    builder.setItems(stopsInRadius, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            etDeparture.setText(stopsInRadius[i]);
                        }
                    });
                    builder.show();

                } else {

                    /* Kann keine Position ermittelt werden wird eine negative Snackbar ausgegeben */
                    Snackbar snackbar = Snackbar.make(v, "Ermittlung der aktuellen Position nicht möglich\nÜberprüfe deine Einstellungen", Snackbar.LENGTH_LONG)
                            .setAction("Action", null);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.colorPrimaryDark));
                    TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(v.getContext(), R.color.white));
                    snackbar.show();
                }
            }
        });

        /* onClick für die Auswahl des Tickets */
        etTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Es wird ein AlertDialog mit den Auswahlmöglichkeiten erzeugt */
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog_Alert);
                builder.setTitle("Ich besitze...");

                /* onClick für die einzelnen Auswahlmöglichkeiten */
                builder.setSingleChoiceItems(ticketDialogArray, currentTicketIndex, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {

                        /* Der Text der Ticketauswahl wird auf die aktuelle Auswahl gesetzt
                         * Der Index wird aktualisiert, sollte der Benutzer erneut den Dialog öffnen ist die aktuelle Auswahl gewählt
                         * Ein Boolean für hasTicket wird basierend auf der Auswahl gebildet und der Dialog wird geschlossen */
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

        /* onClick Listener für Date- und Time- Dialoge. Diese werden somit initiiert und auch auf Geräten die mit Kitkat laufen dem Material Design nachempfunden  */
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

        /* onClick für den Submit Button
         * Reisedaten werden validiert und bei erfolgreicher Validierung wird die TripsActivity erzeugt und die Daten an diese gegeben  */
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String departureName = etDeparture.getText().toString().trim(),
                        targetName = etTarget.getText().toString().trim(),
                        time = etTime.getText().toString(),
                        date = etDate.getText().toString();

                boolean valid = true;

                /*Prüfe ob Start, Ziel oder Ticket leer sind*/

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

                /* Wenn die Validierung Erfolgreich war wird die TripsActivity gestartet und die Reisedaten werden als Extra angefügt */
                if(valid){
                    HistoryService.startActionAddToHistory(v.getContext(), departureName, targetName);
                    /*Erzeuge die Matching Activity und füge Daten hinzu*/
                    Intent tripsIntent = new Intent(getContext(), TripsActivity.class);
                    tripsIntent.putExtra("departureName", departureName);
                    tripsIntent.putExtra("targetName", targetName);
                    tripsIntent.putExtra("hasTicket", hasTicket);
                    tripsIntent.putExtra("date", date);
                    tripsIntent.putExtra("time", time);
                    /* Starte Trips Activity
                    *  Werden keine Trips gefunden oder anderweitige Fehler treten auf wird in onActivityResult darauf reagiert*/
                    startActivityForResult(tripsIntent, REQUEST_CODE_TRIPS);
                }
            }
        });

        /* History item selected listener */
        historyOnClickListener();

        return v;
    }

    /* Die TripsActivity wird mit dem Zusatz ...ForResult gestartet. Dies bedeutet, dass die Suchmaske bei wiederkehr ein Feedbach anhand eines StatusCodes erwartet */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Die Suche nach Trips wurde mit einem Fehler beendet und wirft den Benutzer auf die Suchmaske zurück
         * Anschließend wird eine negative Snackbar mit entsprechender Fehlermeldung vom Server erzeugt */
        if(requestCode == REQUEST_CODE_TRIPS && resultCode == Activity.RESULT_CANCELED) {
            String message = data.getStringExtra("message");
            snackbar = Snackbar.make(v, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            snackbar.show();

        /* Kehrt der Benutzer manuell, ohne Fehler, zur Suchmaske zurück wird der Verlauf erneut aktualisiert, da dieser durch die Tripssuche verändert wurde */
        }else if (requestCode == REQUEST_CODE_TRIPS && resultCode == Activity.RESULT_OK) {
            getHistoryData();
        }

    }

    //<--           prepareVerlaufData Start          -->
    private void getHistoryData(){
        transit.clear();

        /* Es wird ein Worker Thread gestartet, welcher den Verlauf aus der Lokalen Datenbank ausliest und der ArrayList<HistoryEntry> 5 nach Rating Sortierten
         * Ergebnisse hinzufügt  */
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                /* SQLite Datenbank wird geöffnet und ein entsprechender Query wird eingeleitet */
                SQLiteDatabase db;
                db = v.getContext().openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);
                Cursor cursor = db.query("history", new String[] {"departure_station_name", "target_station_name"}, null, null, null, null, "rating DESC", "5");

                /* Entspricht das Ergebnis mehr als 0 Einträgen werden diese der ArrayList hinzugefügt */
                if(cursor.getCount() > 0){
                    while (cursor.moveToNext()){
                        transit.add(new HistoryEntry(cursor.getString(0), cursor.getString(1)));
                    }
                }

                /* Abschließend werden cursor und db geschlossen um Datenlecks zu vermeiden */
                cursor.close();
                db.close();

                /* Im UI Thread wird der Adapter benachrichtigt und der noHistoryContainer wird angezeigt oder versteckt */
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*Benachrichtige Adapter über Änderungen*/
                        if(!transit.isEmpty()) {
                            noHistoryContainer.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                        }else{
                            noHistoryContainer.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        myThread.start();
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

                        /*Trage Wert in das EditText ein*/
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

                /* der Pointer wird auf ein Item der position in der ArrayList gesetzt */
                HistoryEntry historyEntry = transit.get(position);

                /* Start und Zielname werden in die Eingabefelder übernommen */
                etDeparture.setText(historyEntry.getDeparture());
                etTarget.setText(historyEntry.getTarget());

                /* Errormeldungen werden entfernt, sofern vorhanden */
                etDeparture.setError(null);
                etTarget.setError(null);

                /* Es wird smooth nach oben gescrollt */
                svContainer.smoothScrollTo(0,0);

                /* Eine Snackbar wird dem Benutzer angezeigt damit dieser Feedbach bekommt */
                Snackbar.make(view, "Eintrag gewählt", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });
    }
    //<--           History onClickListener End           -->

    private void getStationData(){

        /* Ein Worker Thread wird gestartet welcher die Station_names aus der Lokalen Datenbank auslesen und dem Adapter hinzufügen soll */
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

                /* Im UI Thread wird der adapter über Veränderungen benachrichtigt */
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

    /* Methode um die Software Tastatur zu verstecken */
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

}
