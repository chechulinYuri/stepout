package main;

import android.content.Context;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuri on 25.07.2014.
 */
public class DataExchange {

    public DataExchange(Context ctx) {
        Parse.initialize(ctx, "w8w75nqgzFroCnZEqO6auY85PJnTRKILNXYZUeKa", "UNH39pBxBzLAD4ekMZQUp0VzGUACPTPTHBT5x8qg");
    }

    public User loginFb() {
        return null;
    }

    public User saveUserToParseCom(User user) {

        String userHash = null;

        ParseObject userParse = new ParseObject(MainActivity.USER_TABLE_NAME);
        userParse.put("fbId", user.fbId);
        userParse.put("firstName", user.firstName);
        userParse.put("lastName", user.lastName);
        userParse.put("photoLink", user.photoLink);
        userParse.put("phone", user.phone);

        try {
            userParse.save();
            userHash = userParse.getObjectId();
        } catch(ParseException e) {
            Log.d("ERROR", e.getMessage());
        }

        if (userHash != null) {
            user.hash = userHash;
            return user;
        }

        return null;
    }

    public User getUserFromParseCom(String fbId) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(MainActivity.USER_TABLE_NAME);
        query.whereEqualTo("fbId", fbId);
        try {
            List<ParseObject> objects = query.find();

            if (objects.size() > 0) {

                ParseObject obj = objects.get(0);

                User user = new User(
                        obj.getString("firstName"),
                        obj.getString("lastName"),
                        obj.getString("phone"),
                        obj.getString("photoLink"),
                        obj.getString("fbId")
                );

                user.hash = obj.getObjectId();

                return user;
            }

        } catch(ParseException e) {
            Log.d("ERROR", e.getMessage());
        }

        return null;
    }

    public boolean isRegistered(String fbId) {
        if (getUserFromParseCom(fbId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isLogedin() {
        return false;
    }

    public Event saveEventToParseCom(Event event) {

        String eventHash = null;

        ParseObject eventParse = new ParseObject(MainActivity.EVENT_TABLE_NAME);
        eventParse.put("message", event.message);
        eventParse.put("tags", event.tags);
        eventParse.put("authorHash", event.authorHash);
        eventParse.put("date", event.date);
        eventParse.put("coordinates", event.coordinates);
        eventParse.put("responsesCount", event.responsesCount);

        try {
            eventParse.save();
            eventHash = eventParse.getObjectId();
        } catch(ParseException e) {
            Log.d("ERROR", e.getMessage());
        }

        if (eventHash != null) {
            event.hash = eventHash;
            return event;
        }

        return null;
    }

    public boolean respondToEvent(String eventHash, String message, String userHash) {
        return false;
    }

    public ArrayList<Event> getEventsByUser(String userHash) {
        return null;
    }

    public ArrayList<User> getUsersByEvent(String eventHash) {
        return null;
    }

    public ArrayList<Event> getEventsInRadius(float x, float y) {
        return null;
    }

    public boolean isEventAssignedToUser(String eventHash, String userHash) {
        return false;
    }

    public boolean unsubscribeFromEvent(String eventHash, String userHash) {
        return false;
    }

    public boolean removeEvent(String eventHash, String userHash) {
        return false;
    }

    public boolean updateEvent(Event event, String userHash) {
        return false;
    }

    public boolean shareEvent(String eventHash, String type) {
        return false;
    }
}
