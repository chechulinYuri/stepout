package com.stepout.main;

import com.parse.ParseGeoPoint;

import java.util.Date;

/**
 * Created by Yuri on 25.07.2014.
 */
public class Event extends Entity {
    private ParseGeoPoint coordinates;
    private String message;
    private String category;
    private String authorHash;
    private Date date;
    private int responsesCount;

    public Event(String message, ParseGeoPoint coordinates, String category, String authorHash, Date date, int responsesCount) {
        this.message = message;
        this.coordinates = coordinates;
        this.category = category;
        this.authorHash = authorHash;
        this.date = date;
        this.responsesCount = responsesCount;
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

    public int getResponsesCount() {
        return responsesCount;
    }

    public ParseGeoPoint getCoordinates() {
        return coordinates;
    }
}
