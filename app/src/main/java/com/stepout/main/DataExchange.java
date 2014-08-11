package com.stepout.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.facebook.model.GraphUser;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.PushService;
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
    public static final String CATEGORY_TABLE_NAME = "Categories";

    public static final String MESSAGE_COL_NAME = "message";
    public static final String CATEGORY_COL_NAME = "category";
    public static final String AUTHOR_HASH_COL_NAME = "authorHash";
    public static final String DATE_COL_NAME = "date";
    public static final String COORDINATES_COL_NAME = "coordinates";
    public static final String RESPONDENTS_COL_NAME = "respondents";
    public static final String USER_HASH_COL_NAME = "userHash";
    public static final String EVENT_HASH_COL_NAME = "eventHash";
    public static final String FACEBOOK_ID_COL_NAME = "fbId";
    public static final String FIRST_NAME_COL_NAME = "firstName";
    public static final String LAST_NAME_COL_NAME = "lastName";
    public static final String PHONE_COL_NAME = "phone";
    public static final String OBJECT_ID_COL_NAME = "objectId";
    public static final String NAME_COL_NAME = "name";
    public static final String IMAGE_COL_NAME = "image";


    public static final double EVENTS_VISIBILITY_RADIUS_IN_MILES = 50;

    public static final ArrayList<Event> uploadedEvents = new ArrayList<Event>();
    public static final ArrayList<Event> searchEventResult = new ArrayList<Event>();
    public static HashMap<String, Bitmap> categories = new HashMap<String, Bitmap>();

    public static Bus bus;
    public static Context context;

    public static final String STATUS_SUCCESS = "STATUS_SUCCESS";
    public static final String STATUS_FAIL = "STATUS_FAIL";
    public static final String STATUS_REMOVE_SUCCESS = "STATUS_REMOVE_SUCCESS";
    public static final String STATUS_REMOVE_FAIL = "STATUS_REMOVE_FAIL";
    public static final String STATUS_UPDATE_EVENT_SUCCESS = "STATUS_UPDATE_EVENT_SUCCESS";
    public static final String STATUS_UPDATE_EVENT_FAIL = "STATUS_UPDATE_EVENT_FAIL";
    public static final String STATUS_SEARCH_SUCCESS = "STATUS_SEARCH_SUCCESS";
    public static final String STATUS_SEARCH_FAIL = "STATUS_SEARCH_FAIL";
    public static final String EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY = "EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY";
    public static final String LOCATION_OF_NEW_EVENT_LAT_KEY = "LOCATION_OF_NEW_EVENT_LAT_KEY";
    public static final String LOCATION_OF_NEW_EVENT_LNG_KEY = "LOCATION_OF_NEW_EVENT_LNG_KEY";

    public void onCreate() {
        super.onCreate();
        Parse.initialize(getApplicationContext(), "w8w75nqgzFroCnZEqO6auY85PJnTRKILNXYZUeKa", "UNH39pBxBzLAD4ekMZQUp0VzGUACPTPTHBT5x8qg");



        bus = new Bus();
        context = getApplicationContext();

        // New shit begins right here

        PushService.setDefaultPushCallback(getApplicationContext(), MainActivity.class);
        //Activity mContext = (Activity)getApplicationContext();
        //ParseAnalytics.trackAppOpened(mContext.getIntent());

        // And ends here

        DataExchange.getCategories();
    }

    //Needs to be rewritten!
    public static void getCategories() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(CATEGORY_TABLE_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject po: objects) {
                        ParseFile imageFile = po.getParseFile(IMAGE_COL_NAME);
                        try {
                            byte[] imageBytes = imageFile.getData();
                            Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            categories.put(po.getString(NAME_COL_NAME), bmp);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
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
                if (e == null) {
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

        eventParse.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    ParseRelation relation = eventParse.getRelation(RESPONDENTS_COL_NAME);
                    ParseQuery query = relation.getQuery();
                    try {
                        List<ParseObject> respondentObjects = query.find();
                        ArrayList<User> respondents = castParseObjectToUserList(respondentObjects);

                        Event ev = new Event(
                                eventParse.getString(MESSAGE_COL_NAME),
                                eventParse.getParseGeoPoint(COORDINATES_COL_NAME),
                                eventParse.getString(CATEGORY_COL_NAME),
                                eventParse.getString(AUTHOR_HASH_COL_NAME),
                                eventParse.getDate(DATE_COL_NAME),
                                respondents
                        );

                        ev.setHash(eventParse.getObjectId());
                        bus.post(ev);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public static void respondToEvent(final String eventHash, final User user) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, user.getHash());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    final ParseObject userParseObj = objects.get(0);

                    ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
                    query.whereEqualTo(OBJECT_ID_COL_NAME, eventHash);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                final ParseObject obj = objects.get(0);

                                ParseRelation<ParseObject> relation = obj.getRelation(RESPONDENTS_COL_NAME);
                                relation.add(userParseObj);

                                obj.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            for (Event ev : uploadedEvents) {
                                                if (ev.getHash().equals(obj.getObjectId())) {
                                                    ev.getRespondents().add(user);
                                                }
                                            }

                                            bus.post(STATUS_SUCCESS);
                                        } else {
                                            bus.post(STATUS_FAIL);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    public static void getEventsByUser(String userHash) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(AUTHOR_HASH_COL_NAME, userHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    ArrayList<Event> events = new ArrayList<Event>();

                    for (int i = 0; i < objects.size(); i++) {
                        ParseObject po = objects.get(i);

                        ParseRelation relation = po.getRelation(RESPONDENTS_COL_NAME);
                        ParseQuery query = relation.getQuery();
                        try {
                            List<ParseObject> respondentObjects = query.find();
                            ArrayList<User> respondents = castParseObjectToUserList(respondentObjects);

                            Event ev = new Event(
                                    po.getString(MESSAGE_COL_NAME),
                                    po.getParseGeoPoint(COORDINATES_COL_NAME),
                                    po.getString(CATEGORY_COL_NAME),
                                    po.getString(AUTHOR_HASH_COL_NAME),
                                    po.getDate(DATE_COL_NAME),
                                    respondents
                            );

                            ev.setHash(po.getObjectId());
                            events.add(ev);

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    bus.post(events);
                }
            }
        });
    }

    public static void getEventByHash(String eventHash) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, eventHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        ParseObject po = objects.get(0);
                        ParseRelation relation = po.getRelation("respondent");
                        ParseQuery query = relation.getQuery();

                        try {
                            List<ParseObject> respondentObjects = query.find();
                            ArrayList<User> respondents = castParseObjectToUserList(respondentObjects);

                            Event ev = new Event(
                                    po.getString(MESSAGE_COL_NAME),
                                    po.getParseGeoPoint(COORDINATES_COL_NAME),
                                    po.getString(CATEGORY_COL_NAME),
                                    po.getString(AUTHOR_HASH_COL_NAME),
                                    po.getDate(DATE_COL_NAME),
                                    respondents
                            );

                            ev.setHash(po.getObjectId());

                            bus.post(ev);
                            return;

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }

                    bus.post(null);
                }
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

    public static void getEventsInRadius(double lan, double lng) {
        final ArrayList<Event> events = new ArrayList<Event>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereWithinMiles(COORDINATES_COL_NAME, new ParseGeoPoint(lan, lng), EVENTS_VISIBILITY_RADIUS_IN_MILES);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < parseObjects.size(); i++) {
                        ParseObject po = parseObjects.get(i);
                        ParseRelation relation = po.getRelation(RESPONDENTS_COL_NAME);
                        ParseQuery query = relation.getQuery();

                        try {
                            List<ParseObject> respondentObjects = query.find();
                            ArrayList<User> respondents = castParseObjectToUserList(respondentObjects);

                            Event ev = new Event(
                                    po.getString(MESSAGE_COL_NAME),
                                    po.getParseGeoPoint(COORDINATES_COL_NAME),
                                    po.getString(CATEGORY_COL_NAME),
                                    po.getString(AUTHOR_HASH_COL_NAME),
                                    po.getDate(DATE_COL_NAME),
                                    respondents
                            );

                            ev.setHash(po.getObjectId());
                            events.add(ev);

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }

                    bus.post(events);
                }
            }
        });
    }

    public static boolean isEventAssignedToUser(String eventHash, String userHash) {
        return false;
    }

    public static boolean unsubscribeFromEvent(String eventHash, String userHash) {
        return false;
    }

    public static void removeEvent(final String eventHash, String userHash) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, eventHash);
        query.whereEqualTo(AUTHOR_HASH_COL_NAME, userHash);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null && parseObjects.size() > 0) {
                    parseObjects.get(0).deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                                for (Event event: uploadedEvents) {
                                    if (event.getHash().equals(eventHash)) {
                                        uploadedEvents.remove(event);
                                        break;
                                    }
                                }

                                bus.post(STATUS_REMOVE_SUCCESS);
                            } else {
                                bus.post(STATUS_REMOVE_FAIL);
                            }
                        }
                    });
                } else {
                    bus.post(STATUS_REMOVE_FAIL);
                }
            }
        });
    }

    public static void updateEvent(final Event event, String userHash) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);

        query.getInBackground(event.getHash(), new GetCallback<ParseObject>() {
            public void done(ParseObject eventParse, ParseException e) {
                if (e == null) {
                    eventParse.put(MESSAGE_COL_NAME, event.getMessage());
                    eventParse.put(CATEGORY_COL_NAME, event.getCategory());
                    eventParse.put(AUTHOR_HASH_COL_NAME, event.getAuthorHash());
                    eventParse.put(DATE_COL_NAME, event.getDate());

                    eventParse.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                for (int i = 0; i < uploadedEvents.size(); i++) {
                                    Event ev = uploadedEvents.get(i);
                                    if (ev.getHash().equals(event.getHash())) {
                                        uploadedEvents.set(i, event);
                                        break;
                                    }
                                }
                                bus.post(STATUS_UPDATE_EVENT_SUCCESS);
                            } else {
                                bus.post(STATUS_UPDATE_EVENT_FAIL);
                            }
                        }
                    });
                } else {
                    bus.post(STATUS_UPDATE_EVENT_FAIL);
                }
            }
        });
    }

    public static boolean shareEvent(String eventHash, String type) {
        return false;
    }

    public static void searchEventsInRadius(String key, Double lan, Double lng) {
        final ArrayList<Event> events = new ArrayList<Event>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereWithinMiles(COORDINATES_COL_NAME, new ParseGeoPoint(lan, lng), EVENTS_VISIBILITY_RADIUS_IN_MILES);
        query.whereContains(MESSAGE_COL_NAME, key);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < parseObjects.size(); i++) {
                        ParseObject po = parseObjects.get(i);
                        ParseRelation relation = po.getRelation(RESPONDENTS_COL_NAME);
                        ParseQuery query = relation.getQuery();

                        try {
                            List<ParseObject> respondentObjects = query.find();
                            ArrayList<User> respondents = castParseObjectToUserList(respondentObjects);

                            Event ev = new Event(
                                    po.getString(MESSAGE_COL_NAME),
                                    po.getParseGeoPoint(COORDINATES_COL_NAME),
                                    po.getString(CATEGORY_COL_NAME),
                                    po.getString(AUTHOR_HASH_COL_NAME),
                                    po.getDate(DATE_COL_NAME),
                                    respondents
                            );

                            ev.setHash(po.getObjectId());
                            events.add(ev);

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }

                    searchEventResult.clear();
                    searchEventResult.addAll(events);
                    bus.post(STATUS_SEARCH_SUCCESS);
                }
            }
        });
    }

    private static ArrayList<User> castParseObjectToUserList(List<ParseObject> objects) {

        ArrayList<User> users = new ArrayList<User>();

        for (ParseObject obj: objects) {

            User user = new User(
                    obj.getString(FIRST_NAME_COL_NAME),
                    obj.getString(LAST_NAME_COL_NAME),
                    obj.getString(PHONE_COL_NAME),
                    obj.getString(FACEBOOK_ID_COL_NAME)
            );

            user.setHash(obj.getObjectId());

            users.add(user);
        }

        return users;
    }
}
