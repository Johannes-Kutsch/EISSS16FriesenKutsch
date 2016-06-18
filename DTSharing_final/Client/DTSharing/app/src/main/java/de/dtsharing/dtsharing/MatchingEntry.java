package de.dtsharing.dtsharing;

import android.content.ContentValues;

public class MatchingEntry {

    ContentValues ownerTripDetails, ownerDetails;

    public MatchingEntry(){}

    public MatchingEntry(ContentValues ownerTripDetails, ContentValues ownerDetails) {
        this.ownerTripDetails = ownerTripDetails;
        this.ownerDetails = ownerDetails;
    }

    public String getUserName(){
        return ownerDetails.getAsString("firstName")+" "+ownerDetails.getAsString("lastName");
    }

    public String getOwnerUserId(){
        return ownerDetails.getAsString("ownerUserId");
    }

    public double getAverageRating(){
        return ownerDetails.getAsDouble("averageRating");
    }

    public String getDepartureTime(){
        return ownerTripDetails.getAsString("departureTime");
    }

    public String getDepartureName(){
        return ownerTripDetails.getAsString("departureName");
    }

    public String getTargetTime(){
        return ownerTripDetails.getAsString("arrivalTime");
    }

    public String getTargetName(){
        return ownerTripDetails.getAsString("targetName");
    }

    public String getPicture(){
        return ownerDetails.getAsString("picture");
    }

    public String getDtTripId(){
        return ownerTripDetails.getAsString("dtTripId");
    }
}
