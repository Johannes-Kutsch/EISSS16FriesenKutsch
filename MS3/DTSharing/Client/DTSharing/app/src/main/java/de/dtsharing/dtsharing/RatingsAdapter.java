package de.dtsharing.dtsharing;

import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RatingsAdapter extends BaseAdapter{

    private ArrayList<RatingsEntry> ratings;
    private Context context_1;

    public class ViewHolder {
        public TextView name, date, message;
        public ImageView picture, star1, star2, star3, star4, star5;

    }

    public RatingsAdapter(Context context, ArrayList<RatingsEntry> ratings) {
        this.context_1 = context;
        this.ratings = ratings;
    }

    /* Gibt die größe der ArrayList aus */
    @Override
    public int getCount() {
        return ratings.size();
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
            convertView = LayoutInflater.from(context_1).inflate(R.layout.ratings_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.date = (TextView) convertView.findViewById(R.id.tvRatingDate);
            viewHolder.message = (TextView) convertView.findViewById(R.id.tvRatingDescription);
            viewHolder.star1 = (ImageView) convertView.findViewById(R.id.ivStar1);
            viewHolder.star2 = (ImageView) convertView.findViewById(R.id.ivStar2);
            viewHolder.star3 = (ImageView) convertView.findViewById(R.id.ivStar3);
            viewHolder.star4 = (ImageView) convertView.findViewById(R.id.ivStar4);
            viewHolder.star5 = (ImageView) convertView.findViewById(R.id.ivStar5);
            viewHolder.picture = (ImageView) convertView.findViewById(R.id.ivAvatar);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        RatingsEntry ratingsEntry = ratings.get(position);

        viewHolder.name.setText(ratingsEntry.getName());
        viewHolder.date.setText(ratingsEntry.getDate());
        viewHolder.message.setText(ratingsEntry.getMessage());

        setRating(ratingsEntry.getRating(), viewHolder);

        if(ratingsEntry.getPicture().equals("null")){
            int placeholder = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_account_circle_48dp", null, null);
            viewHolder.picture.setImageResource(placeholder);
        }else {
            RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context_1.getResources(), EncodeDecodeBase64.decodeBase64(ratingsEntry.getPicture()));
            roundDrawable.setCircular(true);
            viewHolder.picture.setImageDrawable(roundDrawable);
        }

        return convertView;
    }

    private void setRating(final int rating, ViewHolder viewHolder) {

        /*Default = starBorder => Somit muss dieser Stern nicht zugewiesen werden*/
        final int starFull = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_full_24dp", null, null);

        System.out.println("Rating: "+rating);

        switch (rating){
            case 5:
                viewHolder.star5.setImageResource(starFull);
            case 4:
                viewHolder.star4.setImageResource(starFull);
            case 3:
                viewHolder.star3.setImageResource(starFull);
            case 2:
                viewHolder.star2.setImageResource(starFull);
            case 1:
                viewHolder.star1.setImageResource(starFull);
        }
    }
}
