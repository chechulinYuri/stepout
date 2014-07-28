package main;

import com.parse.ParseGeoPoint;

import java.util.List;

/**
 * Created by Yuri on 25.07.2014.
 */
public class Event extends Entity {
    ParseGeoPoint coordinates;
    public String message;
    public List<String> tags;
    public String authorHash;
    public Long date;
    public int responsesCount;

    public Event(String message, ParseGeoPoint coordinates, List<String> tags, String authorHash, Long date, int responsesCount) {
        this.message = message;
        this.coordinates = coordinates;
        this.tags = tags;
        this.authorHash = authorHash;
        this.date = date;
        this.responsesCount = responsesCount;
    }
}
