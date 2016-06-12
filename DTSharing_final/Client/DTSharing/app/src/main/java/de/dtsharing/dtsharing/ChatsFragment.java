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
public class ChatsFragment extends Fragment {

    RelativeLayout v;

    private ListView lvChats;

    private ArrayList<ChatsEntry> chats = new ArrayList<>();
    private ChatsAdapter mAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*Lade fragment_database Layout*/
        v = (RelativeLayout) inflater.inflate(R.layout.fragment_chats, container, false);

        /*Erfassen der Views mit denen interagiert werden soll*/
        lvChats = (ListView) v.findViewById(R.id.lvChats);


        /*Abkapseln des Adapters vom UI-Thread -> Kein Freeze bei längeren Operationen*/
        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                /*Erzeuge und verbinde Adapter mit der ChatsFragment ListView*/
                mAdapter = new ChatsAdapter(getContext(), chats);
                lvChats.setAdapter(mAdapter);
            }
        });

        /*Fülle Array mit Beispieldaten*/
        prepareVerlaufData();


        return v;
    }

    //<--           prepareVerlaufData Start          -->
    private void prepareVerlaufData(){
        chats.clear();
        String bild = getString(R.string.unknownPerson);
        chats.add(new ChatsEntry("Peter W.", "16. Mai", "Gummersbach Bf", "Köln Hbf", "Was geht ab du Schnitzel?", bild));
        chats.add(new ChatsEntry("Alina S.", "14. Mai", "Köln Hbf", "Paderborn Hbf", "Ok wo stehst du jetzt? :)", bild));
        chats.add(new ChatsEntry("Johannes K.", "10. Mai", "Gummersbach Bf", "Köln Hbf", "Icksdeh", bild));
        chats.add(new ChatsEntry("Tee T.", "5. Mai", "Paderborn Hbf", "Bad Driburg Bahnhof", "Okay das reicht...", bild));

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

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
