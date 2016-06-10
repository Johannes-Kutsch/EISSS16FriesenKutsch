package de.dtsharing.dtsharing;

public class RatingsEntry {

    private String name, date, message, picture;
    private int rating;

    public RatingsEntry(){}

    public RatingsEntry(String name, String date, String message, String picture, int rating) {
        this.name = name;
        this.date = date;
        this.message = message;
        this.picture = picture;
        this.rating = rating;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    public String getMessage(){
        return message;
    }

    public String getPicture(){
        return picture;
    }

    public int getRating(){
        return rating;
    }
}
