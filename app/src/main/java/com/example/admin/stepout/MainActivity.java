package com.example.admin.stepout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.TagLostException;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.stepout.CreateEventActivity;
import com.example.admin.stepout.R;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.UserSettingsFragment;

import java.util.ArrayList;

import model.DataExchange;
import model.User;
import model.Event;

public class MainActivity extends Activity {

    LoginButton fbLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);

        DataExchange dataExchange = new DataExchange(this);

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
                    public void call(Session session, SessionState state, Exception exception) {
                        if (session.isOpened()) {

                            // make request to the /me API
                            Request.newMeRequest(session, new Request.GraphUserCallback() {

                                // callback after Graph API response with user object
                                @Override
                                public void onCompleted(GraphUser user, Response response) {
                                    if (user != null) {
                                        DataExchange dataExchange = new DataExchange(getApplicationContext());
                                        TelephonyManager telephonyManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                                        String phone = telephonyManager.getLine1Number();
                                        User newUser = new User(user.getFirstName(), user.getLastName(), phone, user.getId());
                                        dataExchange.saveUserToParseCom(newUser);
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
