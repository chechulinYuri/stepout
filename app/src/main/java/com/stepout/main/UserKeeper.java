package com.stepout.main;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.stepout.main.models.User;

/**
 * Created by Admin on 31.07.2014.
 */
public class UserKeeper {

    public static final String USER_DATA = "UserPrefsFile";
    public static final String USER_TO_JSON = "UserToJson";

    public static void writeUserToSharedPref(GraphUser user, Context context) {
        Gson gson = new Gson();
        String json = gson.toJson(DataExchange.loginFb(user, context));
        SharedPreferences logedInUser = context.getSharedPreferences(USER_DATA, 0);
        SharedPreferences.Editor editor = logedInUser.edit();
        editor.putString(USER_TO_JSON, json);
        editor.commit();
    }

    public static User readUserFromSharedPref(Context context) {
        SharedPreferences logedInUser = context.getSharedPreferences(USER_DATA, 0);
        String readJson = logedInUser.getString(USER_TO_JSON, null);
        Gson gson = new Gson();
        return gson.fromJson(readJson, User.class);
    }
}
