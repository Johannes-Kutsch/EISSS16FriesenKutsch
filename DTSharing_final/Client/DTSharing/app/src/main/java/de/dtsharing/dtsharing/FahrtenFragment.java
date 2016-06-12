package de.dtsharing.dtsharing;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Handler;


/**
 * A simple {@link Fragment} subclass.
 */
public class FahrtenFragment extends Fragment {

    RelativeLayout v;

    private ListView lvFahrten;

    private ArrayList<FahrtenEntry> fahrten = new ArrayList<>();
    private FahrtenAdapter mAdapter;

    public FahrtenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_fahrten, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvFahrten = (ListView) v.findViewById(R.id.lvFahrten);


        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                /*Erzeuge und verbinde Adapter mit der FahrtenFragment ListView*/
                mAdapter = new FahrtenAdapter(getContext(), fahrten);
                lvFahrten.setAdapter(mAdapter);
            }
        });

        /*Fülle Array mit Beispieldaten*/
        prepareFahrtenData();


        return v;
    }

    //<--           prepareVerlaufData Start          -->
    private void prepareFahrtenData(){
        fahrten.clear();
        fahrten.add(new FahrtenEntry("13:23", "Gummersbach Bf", "14:36", "Köln Hbf", "1:13", "RB11549", "2"));
        fahrten.add(new FahrtenEntry("13:53", "Gummersbach Bf", "15:06", "Köln Hbf", "1:13", "RB11549", "0"));

        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        new android.os.Handler().post(new Runnable() {

            @Override
            public void run() {
                /*Benachrichtige Adapter über Änderungen*/
                mAdapter.notifyDataSetChanged();
            }

        });
    }
    //<--           prepareVerlaufData End            -->
}
