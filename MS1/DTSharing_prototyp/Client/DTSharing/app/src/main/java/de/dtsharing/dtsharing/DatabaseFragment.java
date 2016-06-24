package de.dtsharing.dtsharing;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class DatabaseFragment extends Fragment implements View.OnClickListener {

    ArrayList<Result> arrayOfResults = new ArrayList<Result>();
    ResultsAdapter adapter;
    private ListView myList;
    private RadioButton rbOffer, rbSearch;
    RelativeLayout v;
    private String url;
    String urlBase = "http://10.0.2.2:3000/";
    //String urlBase = "http://192.168.0.15:3000/";
    //Allgemeine urlBase: http://10.0.2.2:3000/


    public DatabaseFragment() {
        // Required empty public constructor
    }


    //<--               onCreateView Start          -->
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_database, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        myList = (ListView) v.findViewById(R.id.lvQuerys);
        rbOffer = (RadioButton) v.findViewById(R.id.rbOffer);
        rbSearch = (RadioButton) v.findViewById(R.id.rbSearch);

        /*Erzeuge onClickListener für die Buttons*/
        rbOffer.setOnClickListener(this);
        rbSearch.setOnClickListener(this);

        /*Erzeuge Adapter und verknüpfe ihn mit dem Array*/
        adapter = new ResultsAdapter(getActivity().getApplicationContext(), arrayOfResults);
        /*Verknüpfe ListView mit Adapter*/
        myList.setAdapter(adapter);

        /*Erzeuge onListItemClicked Listener*/
        listClickListener();

        /*Beim aufrufen der View schonmal die Daten des Typs "offers" auslesen und darstellen*/
        getAllData("offers");

        return v;
    }
    //<--               onCreateView End            -->

    //<--           getAllData Start            -->
    public void getAllData(String type){

        /*Resete Adapter (Frisch von vorn)*/
        adapter.clear();

        url = urlBase+type+"/get/all";

        /*Get Request an den Server um die Daten nach Typ abzurufen*/
        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        /*Erzeuge ArrayList mit dem Ergebnis*/
                        ArrayList<Result> newResults = Result.fromJson(response);
                        /*Füge ArrayList dem Adapter hinzu und benachrichtige ihn über diese Änderung*/
                        adapter.addAll(newResults);
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(getActivity().getApplicationContext()).add(jsonRequest);
    }
    //<--           getAllData End          -->

    //<--           onClick Start           -->
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            /*RadioButton Offers wurde gedrückt. Rufe Daten des Typs "offers" ab*/
            case R.id.rbOffer:
                getAllData("offers");
                break;
            /*RadioButton Offers wurde gedrückt. Rufe Daten des Typs "searches" ab*/
            case R.id.rbSearch:
                getAllData("searches");
                break;
        }
    }
    //<--           onClick End         -->

    //<--           ListClickListener Start         -->
    public void listClickListener(){
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                /*Wurde ein Item der Liste angeklickt, gib die ID des Items aus
                * (Platzhalter für spätere Implementierung)*/
                TextView myData = (TextView) v.findViewById(R.id.tvId);
                String myId = myData.getText().toString();
                Snackbar.make(v, myId, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    //<--           ListClickListener End           -->

}
