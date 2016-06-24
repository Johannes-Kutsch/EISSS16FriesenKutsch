package de.dtsharing.dtsharing;

import android.content.ContentValues;

public class ChatsEntry {

    ContentValues chat = new ContentValues();

    public ChatsEntry(){}

    public ChatsEntry(ContentValues chat) {
        this.chat.put("chatId", chat.getAsString("chatId"));
        this.chat.put("name", chat.getAsString("firstName")+" "+chat.getAsString("lastName"));
        this.chat.put("date", chat.getAsString("date"));
        this.chat.put("departureStationName", chat.getAsString("departureStationName"));
        this.chat.put("targetStationName", chat.getAsString("targetStationName"));
        this.chat.put("lastMessage", chat.getAsString("lastMessage"));
        this.chat.put("picture", chat.getAsString("picture"));
    }

    public String getChatId(){
        return chat.getAsString("chatId");
    }

    public String getName(){
        return chat.getAsString("name");
    }

    public String getDate(){
        return chat.getAsString("date");
    }

    public String getDepartureStationName(){
        return chat.getAsString("departureStationName");
    }

    public String getTargetStationName(){
        return chat.getAsString("targetStationName");
    }

    public String getMessage(){
        return chat.getAsString("lastMessage");
    }

    public String getPicture(){
        return chat.getAsString("picture");
    }

    public void setLastMessageAndDate(String newMessage, String newDate){
        chat.put("lastMessage", newMessage);
        chat.put("date", newDate);
    }
}
