package model;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.facebook.model.GraphUser;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuri on 25.07.2014.
 */
public class DataExchange {

    public static final String EVENT_TABLE_NAME = "Event";
    public static final String USER_TABLE_NAME = "User";
    public static final String RESPONSE_TABLE_NAME = "Response";

    public static final String MESSAGE_COL_NAME = "message";
    public static final String TAGS_COL_NAME = "tags";
    public static final String AUTHOR_HASH_COL_NAME = "authorHash";
    public static final String DATE_COL_NAME = "date";
    public static final String COORDINATES_COL_NAME = "coordinates";
    public static final String RESPONSES_COUNT_COL_NAME = "responsesCount";
    public static final String USER_HASH_COL_NAME = "userHash";
    public static final String EVENT_HASH_COL_NAME = "eventHash";
    public static final String FACEBOOK_ID_COL_NAME = "fbId";
    public static final String FIRST_NAME_COL_NAME = "firstName";
    public static final String LAST_NAME_COL_NAME = "lastName";
    public static final String PHOTO_LINK_COL_NAME = "photoLink";
    public static final String PHONE_COL_NAME = "phone";
    public static final String OBJECT_ID_COL_NAME = "objectId";

    public static final double EVENTS_VISIBILITY_RADIUS_IN_MILES = 400;

    public DataExchange(Context ctx) {
        Parse.initialize(ctx, "w8w75nqgzFroCnZEqO6auY85PJnTRKILNXYZUeKa", "UNH39pBxBzLAD4ekMZQUp0VzGUACPTPTHBT5x8qg");
    }

    public User loginFb(GraphUser fbUser, Context context) {
        if (!isRegistered(fbUser.getId())) {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String phone = telephonyManager.getLine1Number();
            User newUser = new User(fbUser.getFirstName(), fbUser.getLastName(), phone, fbUser.getId());
            return saveUserToParseCom(newUser);
        }
        return getUserByFbId(fbUser.getId());
    }

    public User saveUserToParseCom(User user) {

        String userHash = null;

        ParseObject userParse = new ParseObject(USER_TABLE_NAME);
        userParse.put(FACEBOOK_ID_COL_NAME, user.getFbId());
        userParse.put(FIRST_NAME_COL_NAME, user.getFirstName());
        userParse.put(LAST_NAME_COL_NAME, user.getLastName());
        userParse.put(PHONE_COL_NAME, user.getPhone()   );

        try {
            userParse.save();
            userHash = userParse.getObjectId();
        } catch(ParseException e) {
            Log.d("ERROR", e.getMessage());
        }

        if (userHash != null) {
            user.setHash(userHash);
            return user;
        }

        return null;
    }

    public User getUserByFbId(String fbId) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_TABLE_NAME);
        query.whereEqualTo(FACEBOOK_ID_COL_NAME, fbId);
        try {
            List<ParseObject> objects = query.find();

            if (objects.size() > 0) {

                ParseObject obj = objects.get(0);

                User user = new User(
                        obj.getString(FIRST_NAME_COL_NAME),
                        obj.getString(LAST_NAME_COL_NAME),
                        obj.getString(PHONE_COL_NAME),
                        obj.getString(FACEBOOK_ID_COL_NAME)
                );

                user.setHash(obj.getObjectId());

                return user;
            }

        } catch(ParseException e) {
            Log.d("ERROR", e.getMessage());
        }

        return null;
    }

    public User getUserByHash(String userHash) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, userHash);
        try {
            List<ParseObject> objects = query.find();

            if (objects.size() > 0) {

                ParseObject obj = objects.get(0);

                User user = new User(
                        obj.getString(FIRST_NAME_COL_NAME),
                        obj.getString(LAST_NAME_COL_NAME),
                        obj.getString(PHONE_COL_NAME),
                        obj.getString(FACEBOOK_ID_COL_NAME)
                );

                user.setHash(obj.getObjectId());

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

        ParseObject eventParse = new ParseObject(EVENT_TABLE_NAME);
        eventParse.put(MESSAGE_COL_NAME, event.getMessage());
        eventParse.put(TAGS_COL_NAME, event.getTags());
        eventParse.put(AUTHOR_HASH_COL_NAME, event.getAuthorHash());
        eventParse.put(DATE_COL_NAME, event.getDate());
        eventParse.put(COORDINATES_COL_NAME, event.getCoordinates());
        eventParse.put(RESPONSES_COUNT_COL_NAME, event.getResponsesCount());

        try {
            eventParse.save();
            eventHash = eventParse.getObjectId();
        } catch(ParseException e) {
            Log.d("ERROR", e.getMessage());
        }

        if (eventHash != null) {
            event.setHash(eventHash);
            return event;
        }

        return null;
    }

    public boolean respondToEvent(String eventHash, String userHash, String message) {

        ParseObject eventRespondParse = new ParseObject(RESPONSE_TABLE_NAME);
        eventRespondParse.put(MESSAGE_COL_NAME, message);
        eventRespondParse.put(USER_HASH_COL_NAME, userHash);
        eventRespondParse.put(EVENT_HASH_COL_NAME, eventHash);

        try {
            eventRespondParse.save();
        } catch(ParseException e) {
            Log.d("ERROR", e.getMessage());
        }

        if (eventRespondParse.getObjectId() != null) {
            return true;
        }

        return false;
    }

    public ArrayList<Event> getEventsByUser(String userHash) {

        ArrayList<Event> result = new ArrayList<Event>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(AUTHOR_HASH_COL_NAME, userHash);
        try {
            List<ParseObject> objects = query.find();

            for (int i = 0; i < objects.size(); i++) {
                ParseObject po = objects.get(i);

                ArrayList<String> tags = new ArrayList<String>();
                JSONArray jsonArray = po.getJSONArray(TAGS_COL_NAME);
                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int j=0; j<len; j++){
                        tags.add(jsonArray.get(j).toString());
                    }
                }

                Event ev = new Event(
                        po.getString(MESSAGE_COL_NAME),
                        po.getParseGeoPoint(COORDINATES_COL_NAME),
                        tags,
                        po.getString(AUTHOR_HASH_COL_NAME),
                        po.getDate(DATE_COL_NAME),
                        po.getInt(RESPONSES_COUNT_COL_NAME)
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

        ParseQuery<ParseObject> query = ParseQuery.getQuery(RESPONSE_TABLE_NAME);
        query.whereEqualTo(EVENT_HASH_COL_NAME, eventHash);
        try {
            List<ParseObject> objects = query.find();

            for (int i = 0; i < objects.size(); i++) {
                ParseObject po = objects.get(i);

                User user = getUserByHash(po.getString(USER_HASH_COL_NAME));
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

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereWithinMiles(COORDINATES_COL_NAME, new ParseGeoPoint(x, y), EVENTS_VISIBILITY_RADIUS_IN_MILES);
        try {
            List<ParseObject> objects = query.find();

            for (int i = 0; i < objects.size(); i++) {
                ParseObject po = objects.get(i);

                ArrayList<String> tags = new ArrayList<String>();
                JSONArray jsonArray = po.getJSONArray(TAGS_COL_NAME);
                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int j=0; j<len; j++){
                        tags.add(jsonArray.get(j).toString());
                    }
                }

                Event ev = new Event(
                        po.getString(MESSAGE_COL_NAME),
                        po.getParseGeoPoint(COORDINATES_COL_NAME),
                        tags,
                        po.getString(AUTHOR_HASH_COL_NAME),
                        po.getDate(DATE_COL_NAME),
                        po.getInt(RESPONSES_COUNT_COL_NAME)
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
