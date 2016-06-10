package de.dtsharing.dtsharing;

public class FahrtenEntry {

    private String departureTime, departureName, targetTime, targetName, transitDuration, lineName;
    private String badgeCount;

    public FahrtenEntry(){}

    public FahrtenEntry(String departureTime, String departureName, String targetTime, String targetName, String transitDuration, String lineName, String badgeCount) {
        this.departureTime = departureTime;
        this.departureName = departureName;
        this.targetTime = targetTime;
        this.targetName = targetName;
        this.transitDuration = transitDuration;
        this.lineName = lineName;
        this.badgeCount = badgeCount;
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

    public String getTransitDuration(){
        return transitDuration;
    }

    public String getLineName(){
        return lineName;
    }

    public String getBadgeCount(){
        return badgeCount;
    }
}
