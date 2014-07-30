package com.example.admin.stepout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.gson.Gson;

import model.DataExchange;
import model.User;

public class MainActivity extends Activity {

    private LoginButton fbLoginButton;
    public static final String USER_DATA = "UserPrefsFile";
    public static final String USER_TO_JSON = "UserToJson";
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);

        //Restore data from SharedPreferences
        final SharedPreferences logedInUser = getSharedPreferences(USER_DATA, 0);
        String readJson = logedInUser.getString(USER_TO_JSON, null);
        Gson gson = new Gson();
        currentUser = gson.fromJson(readJson, User.class);
        if (currentUser != null) {
            Log.d("ASD", currentUser.getFirstName());
        }

        findViewById(R.id.test_create_event_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                startActivity(intent);
            }
        });

        fbLoginButton = (LoginButton)findViewById(R.id.fb_login);
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Session.openActiveSession(MainActivity.this, true, new Session.StatusCallback() {

                    // callback when session changes state
                    @Override
                    public void call(Session session, SessionState state, final Exception exception) {
                        if (session.isOpened()) {

                            // make request to the /me API
                            Request.newMeRequest(session, new Request.GraphUserCallback() {

                                // callback after Graph API response with user object
                                @Override
                                public void onCompleted(GraphUser user, Response response) {
                                    if (user != null) {
                                        Gson gson = new Gson();
                                        String json = gson.toJson(DataExchange.loginFb(user, getApplicationContext()));
                                        SharedPreferences logedInUser = getSharedPreferences(USER_DATA, 0);
                                        SharedPreferences.Editor editor = logedInUser.edit();
                                        editor.putString(USER_TO_JSON, json);
                                        editor.commit();
                                    }
                                }
                            }).executeAsync();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
}
