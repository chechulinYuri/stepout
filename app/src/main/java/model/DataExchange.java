package model;

import android.content.Context;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import main.MainActivity;

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

    public User getUserByFbId(String fbId) {

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

    public User getUserByHash(String userHash) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(MainActivity.USER_TABLE_NAME);
        query.whereEqualTo("objectId", userHash);
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
        if (getUserByFbId(fbId) == null) {
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

        ArrayList<Event> result = new ArrayList<Event>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(MainActivity.EVENT_TABLE_NAME);
        query.whereEqualTo("authorHash", userHash);
        try {
            List<ParseObject> objects = query.find();

            for (int i = 0; i < objects.size(); i++) {
                ParseObject po = objects.get(i);

                ArrayList<String> tags = new ArrayList<String>();
                JSONArray jsonArray = po.getJSONArray("tags");
                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int j=0; j<len; j++){
                        tags.add(jsonArray.get(j).toString());
                    }
                }

                Event ev = new Event(
                        po.getString("message"),
                        po.getParseGeoPoint("coordinates"),
                        tags,
                        po.getString("authorHash"),
                        po.getLong("date"),
                        po.getInt("responsesCount")
                    );

                result.add(ev);
            }
        } catch(ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;

    }

    public ArrayList<User> getUsersByEvent(String eventHash) {
        ArrayList<User> result = new ArrayList<User>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(MainActivity.RESPONSE_TABLE_NAME);
        query.whereEqualTo("eventHash", eventHash);
        try {
            List<ParseObject> objects = query.find();

            for (int i = 0; i < objects.size(); i++) {
                ParseObject po = objects.get(i);

                User user = getUserByHash(po.getString("userHash"));
                if (user != null) {
                    result.add(user);
                }
            }
        } catch(ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<Event> getEventsInRadius(float x, float y) {
        ArrayList<Event> result = new ArrayList<Event>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(MainActivity.EVENT_TABLE_NAME);
        query.whereWithinMiles("coordinates", new ParseGeoPoint(x, y), MainActivity.EVENTS_VISIBILITY_RADIUS_IN_MILES);
        try {
            List<ParseObject> objects = query.find();

            for (int i = 0; i < objects.size(); i++) {
                ParseObject po = objects.get(i);

                ArrayList<String> tags = new ArrayList<String>();
                JSONArray jsonArray = po.getJSONArray("tags");
                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int j=0; j<len; j++){
                        tags.add(jsonArray.get(j).toString());
                    }
                }

                Event ev = new Event(
                        po.getString("message"),
                        po.getParseGeoPoint("coordinates"),
                        tags,
                        po.getString("authorHash"),
                        po.getLong("date"),
                        po.getInt("responsesCount")
                );

                result.add(ev);
            }
        } catch(ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
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
