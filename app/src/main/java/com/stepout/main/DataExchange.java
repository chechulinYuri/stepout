package com.stepout.main;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.facebook.model.GraphUser;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.squareup.otto.Bus;
import com.stepout.main.models.Event;
import com.stepout.main.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String RESPONDENTS_HASH_COL_NAME = "respondentsHash";
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
    public static final ArrayList<Event> filterEventResult = new ArrayList<Event>();
    public static HashMap<String, Bitmap> categories = new HashMap<String, Bitmap>();

    public static final String LOG_TAG = "asd";

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
    public static final String STATUS_FILTER_SUCCESS = "STATUS_FILTER_SUCCESS";
    public static final String STATUS_FILTER_FAIL = "STATUS_FILTER_FAIL";
    public static final String EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY = "EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY";
    public static final String LOCATION_OF_NEW_EVENT_LAT_KEY = "LOCATION_OF_NEW_EVENT_LAT_KEY";
    public static final String LOCATION_OF_NEW_EVENT_LNG_KEY = "LOCATION_OF_NEW_EVENT_LNG_KEY";

    public static final String PREFIX_FOR_CHANNEL_NAME = "channel_";

    public void onCreate() {
        super.onCreate();
        Parse.initialize(getApplicationContext(), "w8w75nqgzFroCnZEqO6auY85PJnTRKILNXYZUeKa", "UNH39pBxBzLAD4ekMZQUp0VzGUACPTPTHBT5x8qg");

        bus = new Bus();
        context = getApplicationContext();
        PushService.setDefaultPushCallback(getApplicationContext(), MainActivity.class);
    }

    public static void getCategories() {
        categories = new HashMap<String, Bitmap>();
        Log.d(LOG_TAG, "start category loading");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(CATEGORY_TABLE_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    Log.d(LOG_TAG, "category loaded");
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
                } else {
                    Log.d(LOG_TAG, e.getMessage());
                }

                bus.post(categories);
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

        if (user.getPhone() != null) {
            userParse.put(PHONE_COL_NAME, user.getPhone());
        }

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

    public static void saveEventToParseCom(final Event event) {
        final ParseObject eventParse = new ParseObject(EVENT_TABLE_NAME);
        eventParse.put(MESSAGE_COL_NAME, event.getMessage());
        eventParse.put(CATEGORY_COL_NAME, event.getCategory());
        eventParse.put(AUTHOR_HASH_COL_NAME, event.getAuthorHash());
        eventParse.put(DATE_COL_NAME, event.getDate());
        eventParse.put(COORDINATES_COL_NAME, event.getCoordinates());
        eventParse.put(RESPONDENTS_HASH_COL_NAME, event.getRespondentsHash());

        eventParse.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Event ev = new Event(
                            eventParse.getString(MESSAGE_COL_NAME),
                            eventParse.getParseGeoPoint(COORDINATES_COL_NAME),
                            eventParse.getString(CATEGORY_COL_NAME),
                            eventParse.getString(AUTHOR_HASH_COL_NAME),
                            eventParse.getDate(DATE_COL_NAME),
                            eventParse.<String>getList(RESPONDENTS_HASH_COL_NAME)
                    );

                    ev.setHash(eventParse.getObjectId());
                    bus.post(ev);
                }
            }
        });
    }

    public static void respondToEvent(final String eventHash, final String userHash) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, eventHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    final ParseObject eventParseObj = objects.get(0);
                    eventParseObj.getList(RESPONDENTS_HASH_COL_NAME).add(userHash);
                    eventParseObj.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                for (Event ev : uploadedEvents) {
                                    if (ev.getHash().equals(eventParseObj.getObjectId())) {
                                        ev.getRespondentsHash().add(userHash);
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

    public static void  unresponseFromEvent(String eventHash, final String userHash) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, eventHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    final ParseObject eventParseObj = objects.get(0);
                    eventParseObj.getList(RESPONDENTS_HASH_COL_NAME).remove(userHash);
                    eventParseObj.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                for (Event ev : uploadedEvents) {
                                    if (ev.getHash().equals(eventParseObj.getObjectId())) {
                                        ev.getRespondentsHash().remove(userHash);
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

    public static void getEventByHash(String eventHash) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        query.whereEqualTo(OBJECT_ID_COL_NAME, eventHash);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        ParseObject po = objects.get(0);

                        Event ev = new Event(
                                po.getString(MESSAGE_COL_NAME),
                                po.getParseGeoPoint(COORDINATES_COL_NAME),
                                po.getString(CATEGORY_COL_NAME),
                                po.getString(AUTHOR_HASH_COL_NAME),
                                po.getDate(DATE_COL_NAME),
                                po.<String>getList(RESPONDENTS_HASH_COL_NAME)
                        );

                        ev.setHash(po.getObjectId());

                        bus.post(ev);
                    } else {
                        Log.d(LOG_TAG, "getEventByHash event not found");
                        bus.post(new Event());
                    }
                } else {
                    Log.d(LOG_TAG, e.getMessage());
                    bus.post(new Event());
                }
            }
        });
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
                        Event ev = new Event(
                                po.getString(MESSAGE_COL_NAME),
                                po.getParseGeoPoint(COORDINATES_COL_NAME),
                                po.getString(CATEGORY_COL_NAME),
                                po.getString(AUTHOR_HASH_COL_NAME),
                                po.getDate(DATE_COL_NAME),
                                po.<String>getList(RESPONDENTS_HASH_COL_NAME)
                        );

                        ev.setHash(po.getObjectId());
                        events.add(ev);
                    }

                    bus.post(events);
                }
            }
        });
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

                        Event ev = new Event(
                                po.getString(MESSAGE_COL_NAME),
                                po.getParseGeoPoint(COORDINATES_COL_NAME),
                                po.getString(CATEGORY_COL_NAME),
                                po.getString(AUTHOR_HASH_COL_NAME),
                                po.getDate(DATE_COL_NAME),
                                po.<String>getList(RESPONDENTS_HASH_COL_NAME)
                        );

                        ev.setHash(po.getObjectId());
                        events.add(ev);

                    }

                    searchEventResult.clear();
                    searchEventResult.addAll(events);
                    bus.post(STATUS_SEARCH_SUCCESS);
                } else {
                    bus.post(STATUS_SEARCH_FAIL);
                }
            }
        });
    }

    public static void filterEventsInRadius(HashMap<String, Boolean> categoriesFlags, boolean onlyRespondEvent, Double lan, Double lng, String userHash) {

        final ArrayList<Event> events = new ArrayList<Event>();

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        for(Map.Entry<String, Boolean> entry : categoriesFlags.entrySet()) {
            if (entry.getValue()) {
                ParseQuery<ParseObject> subQuery = ParseQuery.getQuery(EVENT_TABLE_NAME);
                subQuery.whereEqualTo(CATEGORY_COL_NAME, entry.getKey());
                queries.add(subQuery);
            }
        }

        ParseQuery<ParseObject> query;

        if (queries.size() > 0) {
            query = ParseQuery.or(queries);
        } else {
            query = ParseQuery.getQuery(EVENT_TABLE_NAME);
        }

        query.whereWithinMiles(COORDINATES_COL_NAME, new ParseGeoPoint(lan, lng), EVENTS_VISIBILITY_RADIUS_IN_MILES);
        if (onlyRespondEvent) {
            query.whereEqualTo(RESPONDENTS_HASH_COL_NAME, userHash);
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < parseObjects.size(); i++) {
                        ParseObject po = parseObjects.get(i);

                        Event ev = new Event(
                                po.getString(MESSAGE_COL_NAME),
                                po.getParseGeoPoint(COORDINATES_COL_NAME),
                                po.getString(CATEGORY_COL_NAME),
                                po.getString(AUTHOR_HASH_COL_NAME),
                                po.getDate(DATE_COL_NAME),
                                po.<String>getList(RESPONDENTS_HASH_COL_NAME)
                        );

                        ev.setHash(po.getObjectId());
                        events.add(ev);

                    }

                    filterEventResult.clear();
                    filterEventResult.addAll(events);
                    bus.post(STATUS_FILTER_SUCCESS);
                } else {
                    bus.post(STATUS_FILTER_FAIL);
                }
            }
        });

    }
}
