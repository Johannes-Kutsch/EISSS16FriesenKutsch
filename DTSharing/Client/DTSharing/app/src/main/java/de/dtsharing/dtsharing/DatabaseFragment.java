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
    private String url, urlBase = "http://192.168.0.15:3000/";
    //Allgemeine urlBase: http://10.0.2.2:3000/


    public DatabaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = (RelativeLayout) inflater.inflate(R.layout.fragment_database, container, false);

        myList = (ListView) v.findViewById(R.id.lvQuerys);
        rbOffer = (RadioButton) v.findViewById(R.id.rbOffer);
        rbSearch = (RadioButton) v.findViewById(R.id.rbSearch);

        rbOffer.setOnClickListener(this);
        rbSearch.setOnClickListener(this);

        adapter = new ResultsAdapter(getActivity().getApplicationContext(), arrayOfResults);
        myList.setAdapter(adapter);

        listClickListener();

        getAllData("offers");

        return v;
    }

    public void getAllData(String type){

        adapter.clear();

        url = urlBase+type+"/get/all";

        JsonArrayRequest jsonRequest = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // the response is already constructed as a JSONObject!
                        ArrayList<Result> newResults = Result.fromJson(response);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rbOffer:
                getAllData("offers");
                break;
            case R.id.rbSearch:
                getAllData("searches");
                break;
        }
    }

    public void listClickListener(){
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                TextView myData = (TextView) v.findViewById(R.id.tvId);
                String myId = myData.getText().toString();
                Snackbar.make(v, myId, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
