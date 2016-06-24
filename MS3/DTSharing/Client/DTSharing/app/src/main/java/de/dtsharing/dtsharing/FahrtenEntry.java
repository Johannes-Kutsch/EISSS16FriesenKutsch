package de.dtsharing.dtsharing;

import android.content.ContentValues;

public class FahrtenEntry {

    ContentValues trip = new ContentValues();
    String travelDuration;

    public FahrtenEntry(){}

    public FahrtenEntry(ContentValues trip) {
        this.trip.put("tripId", trip.getAsString("tripId"));
        this.trip.put("routeName", trip.getAsString("routeName"));
        this.trip.put("departureName", trip.getAsString("departureName"));
        this.trip.put("targetName", trip.getAsString("targetName"));
        this.trip.put("departureTime", trip.getAsString("departureTime"));
        this.trip.put("arrivalTime", trip.getAsString("arrivalTime"));
        this.trip.put("date", trip.getAsString("date"));
        this.trip.put("numberPartners", trip.getAsString("numberPartners"));
        this.travelDuration = new CalculateTravelDuration().getHoursMinutes(trip.getAsString("departureTime"), trip.getAsString("arrivalTime"));
    }


    public String getTripId(){
        return trip.getAsString("tripId");
    }

    public String getRouteName(){
        return trip.getAsString("routeName");
    }

    public String getDepartureName(){
        return trip.getAsString("departureName");
    }

    public String getTargetName(){
        return trip.getAsString("targetName");
    }

    public String getDepartureTime(){
        return trip.getAsString("departureTime");
    }

    public String getArrivalTime(){
        return trip.getAsString("arrivalTime");
    }

    public String getTravelDuration(){
        return travelDuration;
    }

    public String getDate(){
        return trip.getAsString("date");
    }

    public String getNumberPartners(){
        return trip.getAsString("numberPartners");
    }
}
