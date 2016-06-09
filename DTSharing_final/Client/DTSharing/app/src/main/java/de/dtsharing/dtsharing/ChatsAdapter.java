package de.dtsharing.dtsharing;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ChatsAdapter extends BaseAdapter{

    private ArrayList<ChatsEntry> chats;
    private Context context_1;

    public class ViewHolder {
        public TextView name;
        public TextView date;
        public TextView departure;
        public TextView target;
        public TextView message;
        public ImageView picture;

    }

    public ChatsAdapter(Context context, ArrayList<ChatsEntry> chats) {
        this.context_1 = context;
        this.chats = chats;
    }

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context_1).inflate(
                    R.layout.chats_row, null);
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
        viewHolder.departure.setText(chatsEntry.getDeparture());
        viewHolder.target.setText(chatsEntry.getTarget());
        viewHolder.message.setText(chatsEntry.getMessage());

        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context_1.getResources(), decodeBase64(chatsEntry.getPicture()));
        roundDrawable.setCircular(true);
        viewHolder.picture.setImageDrawable(roundDrawable);

        return convertView;
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
