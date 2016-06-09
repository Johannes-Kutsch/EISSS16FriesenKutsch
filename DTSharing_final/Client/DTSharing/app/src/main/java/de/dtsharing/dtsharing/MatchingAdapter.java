package de.dtsharing.dtsharing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MatchingAdapter extends BaseAdapter{

    private ArrayList<MatchingEntry> matches;
    private Context context_1;

    public class ViewHolder {
        public TextView userName, departureTime, departureName, targetTime, targetName;
        public TextView matchingButton;
        public ImageView picture, star1, star2, star3, star4, star5;

    }

    public MatchingAdapter(Context context, ArrayList<MatchingEntry> matches) {
        this.context_1 = context;
        this.matches = matches;
    }

    @Override
    public int getCount() {
        return matches.size();
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
                    R.layout.matching_row, null);
            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.departureTime = (TextView) convertView.findViewById(R.id.tvDepartureTime);
            viewHolder.departureName = (TextView) convertView.findViewById(R.id.tvDepartureName);
            viewHolder.targetTime = (TextView) convertView.findViewById(R.id.tvTargetTime);
            viewHolder.targetName = (TextView) convertView.findViewById(R.id.tvTargetName);
            viewHolder.matchingButton = (TextView) convertView.findViewById(R.id.bMatching);
            viewHolder.picture = (ImageView) convertView.findViewById(R.id.ivAvatar);
            viewHolder.star1 = (ImageView) convertView.findViewById(R.id.ivStar1);
            viewHolder.star2 = (ImageView) convertView.findViewById(R.id.ivStar2);
            viewHolder.star3 = (ImageView) convertView.findViewById(R.id.ivStar3);
            viewHolder.star4 = (ImageView) convertView.findViewById(R.id.ivStar4);
            viewHolder.star5 = (ImageView) convertView.findViewById(R.id.ivStar5);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MatchingEntry matchesEntry = matches.get(position);

        viewHolder.userName.setText(matchesEntry.getUserName());
        viewHolder.departureTime.setText(matchesEntry.getDepartureTime());
        viewHolder.departureName.setText(matchesEntry.getDepartureName());
        viewHolder.targetTime.setText(matchesEntry.getTargetTime());
        viewHolder.targetName.setText(matchesEntry.getTargetName());

        setRating(matchesEntry.getRating(), viewHolder);

        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context_1.getResources(), decodeBase64(matchesEntry.getPicture()));
        roundDrawable.setCircular(true);
        viewHolder.picture.setImageDrawable(roundDrawable);

        return convertView;
    }


    private void setRating(double rating, ViewHolder viewHolder){

        int starFull = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_full_24dp", null, null);
        int starHalf = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_half_24dp", null, null);
        int starBorder = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_border_24dp", null, null);

        if(rating < 0.5){
            viewHolder.star1.setImageResource(starBorder);
            viewHolder.star2.setImageResource(starBorder);
            viewHolder.star3.setImageResource(starBorder);
            viewHolder.star4.setImageResource(starBorder);
            viewHolder.star5.setImageResource(starBorder);
        }else if(rating >= 0.5 && rating < 1){
            viewHolder.star1.setImageResource(starHalf);
            viewHolder.star2.setImageResource(starBorder);
            viewHolder.star3.setImageResource(starBorder);
            viewHolder.star4.setImageResource(starBorder);
            viewHolder.star5.setImageResource(starBorder);
        }
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
