package de.dtsharing.dtsharing;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    private Button bSubmit, bReset;
    private TextView tvCurrentRadius;
    private EditText etStart, etDestination;
    private SeekBar sbRadius;
    private Spinner spTicket;

    private String spTicket_selected = "Keins";

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.fragment_main, container, false);

        tvCurrentRadius = (TextView) v.findViewById(R.id.tvCurrentRadius);
        sbRadius = (SeekBar) v.findViewById(R.id.sbRadius);
        etStart = (EditText) v.findViewById(R.id.etStart);
        etDestination = (EditText) v.findViewById(R.id.etDestination);
        spTicket = (Spinner) v.findViewById(R.id.spTicket);
        bSubmit = (Button) v.findViewById(R.id.bSubmit);
        bReset = (Button) v.findViewById(R.id.bReset);

        seekBarListener();
        spinnerListener();

        bSubmit.setOnClickListener(this);
        bReset.setOnClickListener(this);

        return v;
    }

    public void seekBarListener() {
        tvCurrentRadius.setText(String.format(getResources().getString(R.string.tvCurrentRadius), sbRadius.getProgress()));

        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                String currentValue = String.format(getResources().getString(R.string.tvCurrentRadius), progress);
                tvCurrentRadius.setText(currentValue);
            }
        });
    }

    public void spinnerListener() {
        spTicket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spTicket_selected = adapterView.getItemAtPosition(i).toString();
                switch (spTicket_selected){
                    case "Keins":
                        bSubmit.setText("Fahrt suchen");
                        break;
                    case "Semesterticket":
                    case "Jobticket":
                        bSubmit.setText("Fahrt anbieten");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bSubmit:
                final String radius = ""+sbRadius.getProgress(),
                            start = etStart.getText().toString(),
                            destination = etDestination.getText().toString(),
                            ticket = spTicket_selected;

                submitData(radius, start, destination, ticket);

                Snackbar.make(view, "Abgeschickt", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                break;

            case R.id.bReset:
                etStart.setText("");
                etDestination.setText("");
                break;
        }
    }

    public void submitData(final String radius, final String start, final String destination, final String ticket){
        String url = "http://10.0.2.2:3000/newEntry";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String success = jsonResponse.getString("success"),
                                    data = jsonResponse.getString("data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("radius", radius);
                params.put("start", start);
                params.put("destination", destination);
                params.put("ticket", ticket);
                return params;
            }
        };

        Volley.newRequestQueue(getActivity().getApplicationContext()).add(postRequest);
    }
}
