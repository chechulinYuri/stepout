package com.stepout.main;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yuri on 25.07.2014.
 */
public class DataExchange extends Application {

    public static final String EVENT_TABLE_NAME = "Event";
    public static final String USER_TABLE_NAME = "User";
    public static final String RESPONSE_TABLE_NAME = "Response";

    public static final String MESSAGE_COL_NAME = "message";
    public static final String CATEGORY_COL_NAME = "category";
    public static final String AUTHOR_HASH_COL_NAME = "authorHash";
    public static final String DATE_COL_NAME = "date";
    public static final String COORDINATES_COL_NAME = "coordinates";
    public static final String RESPONSES_COUNT_COL_NAME = "responsesCount";
    public static final String USER_HASH_COL_NAME = "userHash";
    public static final String EVENT_HASH_COL_NAME = "eventHash";
    public static final String FACEBOOK_ID_COL_NAME = "fbId";
    public static final String FIRST_NAME_COL_NAME = "firstName";
    public static final String LAST_NAME_COL_NAME = "lastName";
    public static final String PHONE_COL_NAME = "phone";
    public static final String OBJECT_ID_COL_NAME = "objectId";

    public static final double EVENTS_VISIBILITY_RADIUS_IN_MILES = 50;

    public static final ArrayList<Event> uploadedEvents = new ArrayList<Event>();
    public static HashMap<String, String> categories = new HashMap<String, String>();

    public static Bus bus;
    public static Context context;

    public void onCreate() {
        super.onCreate();
        Parse.initialize(getApplicationContext(), "w8w75nqgzFroCnZEqO6auY85PJnTRKILNXYZUeKa", "UNH39pBxBzLAD4ekMZQUp0VzGUACPTPTHBT5x8qg");

        bus = new Bus();
        context = getApplicationContext();
    }

    //Needs to be rewritten!
    public static void getCategories() {
        categories.put("Games", "ic_games");
        categories.put("Communication", "ic_communication");
        categories.put("Sport", "ic_sport");
    }

    public static User loginFb(GraphUser fbUser, Context context) {
        if (!isRegistered(fbUser.getId())) {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String phone = telephonyManager.getLine1Number();
            User newUser = new User(fbUser.getFirstName(), fbUser.getLastName(), phone, fbUser.getId());
            return saveUserToParseCom(newUser);
        }
        return getUserByFbId(fbUser.getId());
    }

    public static User saveUserToParseCom(User user) {

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

    public static User getUserByFbId(String fbId) {

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

    public static void getUserByHash(String userHash) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, userHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                ParseObject obj = objects.get(0);
                User user = new User(
                        obj.getString(FIRST_NAME_COL_NAME),
                        obj.getString(LAST_NAME_COL_NAME),
                        obj.getString(PHONE_COL_NAME),
                        obj.getString(FACEBOOK_ID_COL_NAME)
                );

                user.setHash(obj.getObjectId());

                bus.post(user);
            }
        });
    }

    public static boolean isRegistered(String fbId) {
        if (getUserByFbId(fbId) == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isLogedin() {
        return false;
    }

    public static void saveEventToParseCom(final Event event) {
        final ParseObject eventParse = new ParseObject(EVENT_TABLE_NAME);
        eventParse.put(MESSAGE_COL_NAME, event.getMessage());
        eventParse.put(CATEGORY_COL_NAME, event.getCategory());
        eventParse.put(AUTHOR_HASH_COL_NAME, event.getAuthorHash());
        eventParse.put(DATE_COL_NAME, event.getDate());
        eventParse.put(COORDINATES_COL_NAME, event.getCoordinates());
        eventParse.put(RESPONSES_COUNT_COL_NAME, event.getResponsesCount());

        eventParse.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                String eventHash = eventParse.getObjectId();

                if (eventHash != null) {
                    event.setHash(eventHash);
                }

                bus.post(event);
            }
        });
    }

    public static void respondToEvent(String eventHash, String userHash) {

        final ParseObject eventRespondParse = new ParseObject(RESPONSE_TABLE_NAME);
        eventRespondParse.put(USER_HASH_COL_NAME, userHash);
        eventRespondParse.put(EVENT_HASH_COL_NAME, eventHash);

        eventRespondParse.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                String eventRespondHash = eventRespondParse.getObjectId();

                bus.post(eventRespondHash);
            }
        });
    }

    public static void getEventsByUser(String userHash) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(AUTHOR_HASH_COL_NAME, userHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                ArrayList<Event> events = new ArrayList<Event>();

                for (int i = 0; i < objects.size(); i++) {
                    ParseObject po = objects.get(i);
                    Event ev = new Event(
                            po.getString(MESSAGE_COL_NAME),
                            po.getParseGeoPoint(COORDINATES_COL_NAME),
                            po.getString(CATEGORY_COL_NAME),
                            po.getString(AUTHOR_HASH_COL_NAME),
                            po.getDate(DATE_COL_NAME),
                            po.getInt(RESPONSES_COUNT_COL_NAME)
                    );

                    ev.setHash(po.getObjectId());
                    events.add(ev);
                }
                bus.post(events);
            }
        });
    }

    public static void getEventByHash(String eventHash) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, eventHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (objects.size() > 0) {
                    ParseObject po = objects.get(0);
                    Event ev = new Event(
                            po.getString(MESSAGE_COL_NAME),
                            po.getParseGeoPoint(COORDINATES_COL_NAME),
                            po.getString(CATEGORY_COL_NAME),
                            po.getString(AUTHOR_HASH_COL_NAME),
                            po.getDate(DATE_COL_NAME),
                            po.getInt(RESPONSES_COUNT_COL_NAME)
                    );

                    ev.setHash(po.getObjectId());
                    
                    bus.post(ev);
                    return;
                }

                bus.post(null);
            }
        });
    }

    /*public static ArrayList<User> getUsersByEvent(String eventHash) {
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
    }*/

    public static void getEventsInRadius(double x, double y) {
        final ArrayList<Event> result = new ArrayList<Event>();
        final User user = UserKeeper.readUserFromSharedPref(context);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereWithinMiles(COORDINATES_COL_NAME, new ParseGeoPoint(x, y), EVENTS_VISIBILITY_RADIUS_IN_MILES);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                for (int i = 0; i < parseObjects.size(); i++) {
                    ParseObject po = parseObjects.get(i);
                    Event ev = new Event(
                            po.getString(MESSAGE_COL_NAME),
                            po.getParseGeoPoint(COORDINATES_COL_NAME),
                            po.getString(CATEGORY_COL_NAME),
                            po.getString(AUTHOR_HASH_COL_NAME),
                            po.getDate(DATE_COL_NAME),
                            po.getInt(RESPONSES_COUNT_COL_NAME)
                    );
                    ev.setHash(po.getObjectId());

                    if (user != null) {
                        if (user.getHash() == ev.getHash()) {
                            ev.setIsMeAuthor(true);
                        }
                    }

                    result.add(ev);
                }

                bus.post(result);
            }
        });
    }

    public static boolean isEventAssignedToUser(String eventHash, String userHash) {
        return false;
    }

    public static boolean unsubscribeFromEvent(String eventHash, String userHash) {
        return false;
    }

    public static boolean removeEvent(String eventHash, String userHash) {
        return false;
    }

    public static boolean updateEvent(Event event, String userHash) {
        return false;
    }

    public static boolean shareEvent(String eventHash, String type) {
        return false;
    }
}
