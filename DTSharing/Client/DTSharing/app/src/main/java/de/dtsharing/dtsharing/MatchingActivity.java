package de.dtsharing.dtsharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MatchingActivity extends AppCompatActivity {

    ArrayList<Result> arrayOfResults = new ArrayList<Result>();
    ResultsAdapter adapter;
    private ListView myList;
    Button bSubmit;

    private String start, destination, ticket, date, time, getType, postType, url;
    String urlBase = "http://10.0.2.2:3000/";
    //String urlBase = "http://192.168.0.15:3000/";
                    //Allgemeine urlBase: http://10.0.2.2:3000/
    //<--       OnCreate Start         -->
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*Zurück Button in der Titelleiste*/
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        /*Erfasse Views mit denen interagiert werden soll*/
        myList = (ListView) findViewById(R.id.lvQuerys);
        bSubmit = (Button) findViewById(R.id.bSubmit);

        /*Erzeuge Adapter mit Referenz auf das Array. Koppel ListView und Adapter*/
        adapter = new ResultsAdapter(this, arrayOfResults);
        myList.setAdapter(adapter);

        /*Sichere die Empfangenen Daten in Variablen*/
        Intent empfangenerIntent = getIntent();
        if (empfangenerIntent != null) {
            start = empfangenerIntent.getStringExtra("start");
            destination = empfangenerIntent.getStringExtra("destination");
            ticket = empfangenerIntent.getStringExtra("ticket");
            date = empfangenerIntent.getStringExtra("date");
            time = empfangenerIntent.getStringExtra("time");
        }
        /*Anpassung der Begrifflichkeiten*/
        if(ticket.equals("ein Semesterticket"))
            ticket = "Semesterticket";
        else if(ticket.equals("ein Jobticket"))
            ticket = "Jobticket";

        /*Ermittlung des post- und getType. Gegenteilig, da Anbietende Suchende sehen müssen und umgekehrt*/
        if(ticket.equals("kein Ticket")) {
            postType = "searches";
            getType = "offers";
            actionBar.setTitle("Mitfahrgelegenheiten");
        } else {
            postType = "offers";
            getType = "searches";
            actionBar.setTitle("Suchende");
        }

        /*Submit OnClick Listener*/
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            switch (view.getId()){
                case R.id.bSubmit:
                    submitData("post");
                    Snackbar.make(view, "Abgeschickt", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
            }
            }
        });

        /*ListItem OnClick Listener*/
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                TextView myData = (TextView) v.findViewById(R.id.tvId);
                String myId = myData.getText().toString();
                Snackbar.make(v, myId, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /*submitData mit dem queryType get um Matches anzuzeigen*/
        submitData("get");
    }
    //<--           OnCreate End            -->

    //<--           OnOptionsItemSelected Start         -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*Zurück Button geklickt*/
            case android.R.id.home:
                /*Schließe Aktivität ab und kehre zurück*/
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //<--           OnOptionsItemSelected End         -->

    //POST-GET ANFRAGE START
    public void submitData(final String queryType){
        if(queryType.equals("post"))
            url = urlBase+postType+"/post/entry";
        else if(queryType.equals("get"))
            url = urlBase+getType+"/get/matches";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if(queryType.equals("post")) {
                                JSONObject jsonObjResponse = new JSONObject(response);
                                System.out.println(jsonObjResponse.getString("_id"));
                            } else if(queryType.equals("get")) {
                                JSONArray jsonArrayResponse = new JSONArray(response);
                                ArrayList<Result> newResults = Result.fromJson(jsonArrayResponse);
                                adapter.addAll(newResults);
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            })
            {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("start", start);
                params.put("destination", destination);
                params.put("ticket", ticket);
                params.put("date", date);
                params.put("time", time);
                return params;
            }
            };

            Volley.newRequestQueue(this).add(postRequest);
    }

    //POST-GET ANFRAGE ENDE
}
