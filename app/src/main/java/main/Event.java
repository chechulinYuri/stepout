package main;

import java.util.Map;

/**
 * Created by Yuri on 25.07.2014.
 */
public class Event extends Entity {
    public Map<String, Float> coordinates;
    public String message;
    public int category;
    public String authorHash;
    public String date;
    public int responsesCount;
}
