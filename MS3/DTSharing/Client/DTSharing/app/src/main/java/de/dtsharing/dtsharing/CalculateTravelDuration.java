package de.dtsharing.dtsharing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalculateTravelDuration {



    public String getHoursMinutes(String departureTime, String arrivalTime){
        int days = 0, hours = 0, min = 0;
        try {
            /*http://stackoverflow.com/a/31725197
            * Berechnung der Zeitdifferenz zweier Uhrzeiten*/
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.GERMANY);
            Date dt = simpleDateFormat.parse(departureTime);
            Date at = simpleDateFormat.parse(arrivalTime);

            long difference = at.getTime() - dt.getTime();

            if(difference < 0){ //Wenn die Ankunftszeit kleiner ist als die Abfahrtszeit, weil Abfahrt vor und Ankunft nach 0 Uhr
                Date dateMax = simpleDateFormat.parse("24:00");
                Date dateMin = simpleDateFormat.parse("00:00");
                difference = (dateMax.getTime() - dt.getTime()) + (at.getTime() - dateMin.getTime());
            }

            days    = (int) (difference / (1000 * 60 * 60 * 24));
            hours   = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
            min     = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return hours+":"+min;
    }

}
