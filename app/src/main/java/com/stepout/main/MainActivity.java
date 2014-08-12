package com.stepout.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.stepout.main.models.User;

public class MainActivity extends Activity {

    private LoginButton fbLoginButton;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);

        //Restore data from SharedPreferences
        currentUser = UserKeeper.readUserFromSharedPref(this);

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
                                        UserKeeper.writeUserToSharedPref(user, MainActivity.this);
                                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                        startActivity(intent);
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
    protected void onResume() {

        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        }

        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
}
