package de.dtsharing.dtsharing;

public class TripsEntry {

    private String tripID, uniqueTripID, departureTime, departureName, departureDate, arrivalTime, targetName, travelDuration, routeName;
    private int departureSequence, targetSequence, numberMatches;

    public TripsEntry(){}

    public TripsEntry(String tripID, String uniqueTripID, int departureSequence, String departureTime, String departureDate, String departureName, int targetSequence, String arrivalTime, String targetName, String travelDuration, String routeName, int numberMatches) {
        this.tripID = tripID;
        this.uniqueTripID = uniqueTripID;
        this.departureSequence = departureSequence;
        this.departureTime = departureTime;
        this.departureDate = departureDate;
        this.departureName = departureName;
        this.targetSequence = targetSequence;
        this.arrivalTime = arrivalTime;
        this.targetName = targetName;
        this.travelDuration = travelDuration;
        this.routeName = routeName;
        this.numberMatches = numberMatches;
    }

    public String getTripID(){
        return tripID;
    }

    public String getUniqueTripID(){
        return uniqueTripID;
    }

    public int getDepartureSequence(){
        return departureSequence;
    }

    public String getDepartureTime(){
        return departureTime;
    }

    public String getDepartureDate(){
        return departureDate;
    }

    public String getDepartureName(){
        return departureName;
    }

    public int getTargetSequence(){
        return targetSequence;
    }

    public String getArrivalTime(){
        return arrivalTime;
    }

    public String getTargetName(){
        return targetName;
    }

    public String getTravelDuration(){
        return travelDuration;
    }

    public String getRouteName(){
        return routeName;
    }

    public int getNumberMatches(){
        return numberMatches;
    }
}
