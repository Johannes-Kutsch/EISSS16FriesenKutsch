package de.dtsharing.dtsharing;

public class HistoryEntry {

    private String departure, target;

    public HistoryEntry(){}

    public HistoryEntry(String departure, String target) {
        this.departure = departure;
        this.target = target;
    }

    public String getDeparture(){
        return departure;
    }

    public void setDeparture(String departure){
        this.departure = departure;
    }

    public String getTarget(){
        return target;
    }

    public void setTarget(String target){
        this.target = target;
    }

}
