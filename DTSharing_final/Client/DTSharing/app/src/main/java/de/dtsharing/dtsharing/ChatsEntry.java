package de.dtsharing.dtsharing;

public class ChatsEntry {

    private String name, date, departure, target, message, picture;

    public ChatsEntry(){}

    public ChatsEntry(String name, String date, String departure, String target, String message, String picture) {
        this.name = name;
        this.date = date;
        this.departure = departure;
        this.target = target;
        this.message = message;
        this.picture = picture;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    public String getDeparture(){
        return departure;
    }

    public String getTarget(){
        return target;
    }

    public String getMessage(){
        return message;
    }

    public String getPicture(){
        return picture;
    }
}
