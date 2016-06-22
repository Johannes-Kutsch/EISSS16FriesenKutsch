package de.dtsharing.dtsharing;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.Space;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MessagesAdapter extends BaseAdapter{

    private ArrayList<MessagesEntry> messages;
    private Context context_1;
    private String userID;

    public class ViewHolder {
        public TextView name, time, date, message;
        public LinearLayout main_content;
        public LinearLayout.LayoutParams mainContentParams;
        public CardView card;

    }

    public MessagesAdapter(Context context, ArrayList<MessagesEntry> messages, String userID) {
        this.context_1 = context;
        this.messages = messages;
        this.userID = userID;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context_1).inflate(
                    R.layout.messages_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.userName);
            viewHolder.time = (TextView) convertView.findViewById(R.id.time);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            viewHolder.message = (TextView) convertView.findViewById(R.id.message);
            viewHolder.card = (CardView) convertView.findViewById(R.id.card);
            viewHolder.main_content = (LinearLayout) convertView.findViewById(R.id.main_content);
            viewHolder.mainContentParams = (LinearLayout.LayoutParams) viewHolder.main_content.getLayoutParams();

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MessagesEntry messagesEntry = messages.get(position);

        if(messagesEntry.getAuthorID().equals(userID)) {
            viewHolder.mainContentParams.setMargins(200, 0, 0, 0);
            viewHolder.main_content.setLayoutParams(viewHolder.mainContentParams);
            viewHolder.card.setCardBackgroundColor(0xFFFBE9E7);

            viewHolder.date.setText(messagesEntry.getName());
            viewHolder.name.setText(messagesEntry.getDate());
        } else {
            viewHolder.mainContentParams.setMargins(0, 0, 200, 0);
            viewHolder.main_content.setLayoutParams(viewHolder.mainContentParams);
            viewHolder.card.setCardBackgroundColor(Color.WHITE);

            viewHolder.name.setText(messagesEntry.getName());
            viewHolder.date.setText(messagesEntry.getDate());
        }

        viewHolder.time.setText(messagesEntry.getTime());
        viewHolder.message.setText(messagesEntry.getMessage());

        return convertView;
    }
}
