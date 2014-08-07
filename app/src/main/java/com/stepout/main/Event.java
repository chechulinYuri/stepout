package com.stepout.main;

import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Yuri on 25.07.2014.
 */
public class Event extends Entity {
    private ParseGeoPoint coordinates;
    private String message;
    private String category;
    private String authorHash;
    private Date date;
    private ArrayList<User> respondents;

    private String markerId;

    private boolean isMeAuthor;
    private boolean isMeRespondent;

    public Event(String message, ParseGeoPoint coordinates, String category, String authorHash, Date date, ArrayList<User> respondents) {
        this.message = message;
        this.coordinates = coordinates;
        this.category = category;
        this.authorHash = authorHash;
        this.date = date;
        this.respondents = respondents;
    }

    public String getMessage() {
        return message;
    }

    public String getCategory() {
        return category;
    }

    public String getAuthorHash() {
        return authorHash;
    }

    public Date getDate() {
        return date;
    }

    public ParseGeoPoint getCoordinates() {
        return coordinates;
    }

    public void setIsMeAuthor(boolean isMeAuthor) { this.isMeAuthor = isMeAuthor; }

    public void setIsMeRespondent(boolean isMeRespondent) { this.isMeRespondent = isMeRespondent; }

    public ArrayList<User> getRespondents() { return respondents; }

    public boolean isMeRespondent() { return isMeRespondent; }

    public boolean isMeAuthor() { return isMeAuthor; }

    public void setMarkerId(String markerId) { this.markerId = markerId; }

    public String getMarkerId() { return markerId; }
}
