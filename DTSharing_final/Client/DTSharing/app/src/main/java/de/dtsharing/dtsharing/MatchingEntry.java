package de.dtsharing.dtsharing;

public class MatchingEntry {

    private String userId, userName, departureTime, departureName, targetTime, targetName, picture;
    private double rating;
    private boolean hasTicket;

    public MatchingEntry(){}

    public MatchingEntry(String userName, double rating, String departureTime, String departureName, String targetTime, String targetName, String picture, boolean hasTicket, String userId) {
        this.userName = userName;
        this.rating = rating;
        this.departureTime = departureTime;
        this.departureName = departureName;
        this.targetTime = targetTime;
        this.targetName = targetName;
        this.picture = picture;
        this.hasTicket = hasTicket;
        this.userId = userId;

    }

    public String getUserName(){
        return userName;
    }

    public String getUserId(){
        return userId;
    }

    public double getAverageRating(){
        return rating;
    }

    public String getDepartureTime(){
        return departureTime;
    }

    public String getDepartureName(){
        return departureName;
    }

    public String getTargetTime(){
        return targetTime;
    }

    public String getTargetName(){
        return targetName;
    }

    public String getPicture(){
        return picture;
    }

    public boolean hasTicket(){
        return hasTicket;
    }
}
