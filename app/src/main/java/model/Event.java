package model;

import com.parse.ParseGeoPoint;

import java.util.Date;
import java.util.List;

/**
 * Created by Yuri on 25.07.2014.
 */
public class Event extends Entity {
    private ParseGeoPoint coordinates;
    private String message;
    private List<String> tags;
    private String authorHash;
    private Date date;
    private int responsesCount;

    public Event(String message, ParseGeoPoint coordinates, List<String> tags, String authorHash, Date date, int responsesCount) {
        this.message = message;
        this.coordinates = coordinates;
        this.tags = tags;
        this.authorHash = authorHash;
        this.date = date;
        this.responsesCount = responsesCount;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getTags() {
        return tags;
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
