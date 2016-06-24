package de.dtsharing.dtsharing;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.ArrayList;

public class ChatsAdapter extends BaseAdapter{

    private ArrayList<ChatsEntry> chats;
    private Context context_1;
    private AesCbcWithIntegrity.SecretKeys key;

    public class ViewHolder {
        public TextView name;
        public TextView date;
        public TextView departure;
        public TextView target;
        public TextView message;
        public ImageView picture;

    }

    /* chats enthält alle aktiven Chats des Benutzers */
    public ChatsAdapter(Context context, ArrayList<ChatsEntry> chats) {
        this.context_1 = context;
        this.chats = chats;
    }

    /* Gibt die größe der ArrayList aus */
    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /* in getView werden die Views erfasst, dem viewHolder zugewiesen und abschließend mit den Daten der ArrayListe trips
     * angereichert. Die Operationen eines Adapters gelten für jedes Item welches diesem über die ArrayListe hinzugefügt wurde */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context_1).inflate(
                    R.layout.chats_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.date = (TextView) convertView.findViewById(R.id.tvDate);
            viewHolder.departure = (TextView) convertView.findViewById(R.id.tvDeparture);
            viewHolder.target = (TextView) convertView.findViewById(R.id.tvTarget);
            viewHolder.message = (TextView) convertView.findViewById(R.id.tvMessage);
            viewHolder.picture = (ImageView) convertView.findViewById(R.id.ivAvatar);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatsEntry chatsEntry = chats.get(position);

        viewHolder.name.setText(chatsEntry.getName());
        viewHolder.date.setText(chatsEntry.getDate());
        viewHolder.departure.setText(chatsEntry.getDepartureStationName());
        viewHolder.target.setText(chatsEntry.getTargetStationName());


        /* Wenn eine letzte Nachricht existiert */
        if(!chatsEntry.getMessage().equals("null")) {
            String message = null;

            /* Suche in der Lokalen Datenbank den zur ChatID zugehörigen Key zum entschlüsseln der Nachricht */
            SQLiteDatabase db;
            db = context_1.openOrCreateDatabase("DTSharing", Context.MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("SELECT key FROM chats WHERE chat_id=?", new String[]{chatsEntry.getChatId()});
            if (cursor.moveToFirst()) {
                String keystring = cursor.getString(0);

                ToznyHelper toznyHelper = new ToznyHelper(keystring);
                toznyHelper.decryptString(chatsEntry.getMessage());

                viewHolder.message.setText(toznyHelper.getDecryptedString());

            } else {
                viewHolder.message.setText("");
            }
            cursor.close();
            db.close();
        } else {
            viewHolder.message.setText("Neuer Chat");
        }

        if(chatsEntry.getPicture().equals("null")) {
            int placeholder = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_account_circle_48dp", null, null);
            viewHolder.picture.setImageResource(placeholder);
        }else {
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context_1.getResources(), EncodeDecodeBase64.decodeBase64(chatsEntry.getPicture()));
            roundDrawable.setCircular(true);
            viewHolder.picture.setImageDrawable(roundDrawable);
        }

        return convertView;
    }
}
