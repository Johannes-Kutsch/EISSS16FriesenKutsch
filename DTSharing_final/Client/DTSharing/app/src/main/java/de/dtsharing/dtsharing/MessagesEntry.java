package de.dtsharing.dtsharing;

public class MessagesEntry {

    private String authorID, name, time, date, message;
    private int sequence;

    public MessagesEntry(){}

    public MessagesEntry(String authorID, String name, String time, String date, String message, int sequence) {
        this.authorID = authorID;
        this.name = name;
        this.time = time;
        this.date = date;
        this.message = message;
        this.sequence = sequence;
    }

    public String getAuthorID(){
        return authorID;
    }

    public String getName(){
        return name;
    }

    public String getTime(){
        return time;
    }

    public String getDate(){
        return date;
    }

    public String getMessage(){
        return message;
    }

    public int getSequence() {
        return sequence;
    }

}
