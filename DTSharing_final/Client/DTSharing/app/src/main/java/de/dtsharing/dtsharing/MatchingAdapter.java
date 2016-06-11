package de.dtsharing.dtsharing;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
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
        public Button matchingButton;
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
    public View getView(int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(context_1).inflate(R.layout.matching_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.departureTime = (TextView) convertView.findViewById(R.id.tvDepartureTime);
            viewHolder.departureName = (TextView) convertView.findViewById(R.id.tvDepartureName);
            viewHolder.targetTime = (TextView) convertView.findViewById(R.id.tvTargetTime);
            viewHolder.targetName = (TextView) convertView.findViewById(R.id.tvTargetName);
            viewHolder.matchingButton = (Button) convertView.findViewById(R.id.bMatching);
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

        final MatchingEntry matchesEntry = matches.get(position);

        viewHolder.userName.setText(matchesEntry.getUserName());
        viewHolder.departureTime.setText(matchesEntry.getDepartureTime());
        viewHolder.departureName.setText(matchesEntry.getDepartureName());
        viewHolder.targetTime.setText(matchesEntry.getTargetTime());
        viewHolder.targetName.setText(matchesEntry.getTargetName());

        setRating(matchesEntry.getAverageRating(), viewHolder);

        RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context_1.getResources(), EncodeDecodeBase64.decodeBase64(matchesEntry.getPicture()));
        roundDrawable.setCircular(true);
        viewHolder.picture.setImageDrawable(roundDrawable);

        viewHolder.matchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                String message, positiveButton;
                if(matchesEntry.hasTicket()) {
                    message = "Möchtest du wirklich " + matchesEntry.getUserName() + " mitnehmen?";
                    positiveButton = "Mitnehmen";
                }else {
                    message = "Möchtest du wirklich bei " + matchesEntry.getUserName() + " mitfahren?";
                    positiveButton ="Mitfahren";
                }

                AlertDialog.Builder builder =
                        new AlertDialog.Builder(parent.getContext(), R.style.AppTheme_Dialog_Alert);

                //builder.setTitle("Dialog");
                builder.setMessage(message);
                builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Snackbar.make(parent, "OK", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }

                });

                builder.setNegativeButton("Abbruch", null);
                builder.show();
            }
        });

        return convertView;
    }

    private void setRating(final double rating, ViewHolder viewHolder){

        /*Default = starBorder => Somit muss dieser Stern nicht zugewiesen werden*/
        int starFull = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_full_24dp", null, null);
        int starHalf = context_1.getResources().getIdentifier("de.dtsharing.dtsharing:drawable/ic_star_half_24dp", null, null);

        int wholeRating = (int) rating;
        double fractionalRating = rating - wholeRating;

        switch (wholeRating){
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


        if(fractionalRating > 0.25 && fractionalRating < 0.75){
            switch (wholeRating+1){
                case 1:
                    viewHolder.star1.setImageResource(starHalf);
                    break;
                case 2:
                    viewHolder.star2.setImageResource(starHalf);
                    break;
                case 3:
                    viewHolder.star3.setImageResource(starHalf);
                    break;
                case 4:
                    viewHolder.star4.setImageResource(starHalf);
                    break;
                case 5:
                    viewHolder.star5.setImageResource(starHalf);
                    break;
            }
        }else if(fractionalRating >= 0.75){
            switch (wholeRating+1){
                case 1:
                    viewHolder.star1.setImageResource(starFull);
                    break;
                case 2:
                    viewHolder.star2.setImageResource(starFull);
                    break;
                case 3:
                    viewHolder.star3.setImageResource(starFull);
                    break;
                case 4:
                    viewHolder.star4.setImageResource(starFull);
                    break;
                case 5:
                    viewHolder.star5.setImageResource(starFull);
                    break;
            }
        }
    }
}
