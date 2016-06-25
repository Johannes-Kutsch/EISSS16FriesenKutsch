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

            /* Die Anzahl an Tagen zwischen zwei Daten wird ermittelt. Ein Tag wird in Millisekunden angegeben  */
            days    = (int) (difference / (1000 * 60 * 60 * 24));

            /* Um die Stunden zu ermitteln werden von der Differenz alle Tage abgezogen und geschaut wie häufig eine Stunde
             * in das Ergebnis passt */
            hours   = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));

            /* Um die Minuten zu ermitteln werden von der Differenz alle ermittelten Tage sowie Stunden abgezogen und geschaut
             * wie häufig eine Minute in das Ergebnis passt. */
            min     = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        /* Stunden und Minuten kleiner als 10 werden mit einer vorangestellten 0 ergänzt */
        String hoursString = hours < 10 ? "0"+hours : Integer.toString(hours),
                minString = min < 10 ? "0"+min : Integer.toString(min);

        return hoursString+":"+minString;
    }

}
