package de.dtsharing.dtsharing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Result {
    public String id, start, destination, ticket, date, time;

    public Result(JSONObject object){
        try {
            this.id = object.getString("_id");
            this.start = object.getString("start");
            this.destination = object.getString("destination");
            this.ticket = object.getString("ticket");
            this.date = object.getString("date");
            this.time = object.getString("time");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public Result(String start, String destination, String ticket, String date, String time) {
        this.start = start;
        this.destination = destination;
        this.ticket = ticket;
        this.date = date;
        this.time = time;

    }

    public static ArrayList<Result> fromJson(JSONArray jsonObjects) {
        ArrayList<Result> results = new ArrayList<Result>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                results.add(new Result(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }
}
