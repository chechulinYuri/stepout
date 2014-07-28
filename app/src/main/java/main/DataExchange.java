package main;

import android.content.Context;

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

    private String userHash;

    public DataExchange(Context ctx) {
        Parse.initialize(ctx, "w8w75nqgzFroCnZEqO6auY85PJnTRKILNXYZUeKa", "UNH39pBxBzLAD4ekMZQUp0VzGUACPTPTHBT5x8qg");
    }

    public User loginFb() {
        return null;
    }

    public User saveUserToParseCom(User user) {

        userHash = null;

        ParseObject userParse = new ParseObject(MainActivity.USER_TABLE_NAME);
        userParse.put("fbId", user.fbId);
        userParse.put("firstName", user.firstName);
        userParse.put("lastName", user.lastName);
        userParse.put("photoLink", user.photoLink);
        userParse.put("phone", user.phone);

        try {
            userParse.save();
        } catch(ParseException e) {

        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery(MainActivity.USER_TABLE_NAME);
        try {
            List<ParseObject> objects = query.find();

            if (objects.size() > 0) {
                userHash = objects.get(0).getObjectId();
            }

        } catch(ParseException e) {

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

        }

        return null;
    }

    private boolean isRegistered(String fbId) {
        if (getUserFromParseCom(fbId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isLogin() {
        return false;
    }

    public boolean createEvent(Event event) {
        return false;
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
