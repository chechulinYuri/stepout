package com.stepout.main;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewEventAsGuestActivity extends FragmentActivity {

    private Event currentEvent;
    private User currentUser;
    private boolean isSavingProcess;
    private Button respondButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        currentUser = UserKeeper.readUserFromSharedPref(this);

        DataExchange.getEventsByUser(getIntent().getStringExtra(MainActivity.USER_HASH_FOR_VIEW_EVENT_ACTIVITY));

        respondButton = (Button) findViewById(R.id.respond_event_button);

        respondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isSavingProcess) {
                    if (currentEvent != null && currentUser != null) {
                        isSavingProcess = true;
                        updateSaveButton();
                        DataExchange.respondToEvent(currentEvent.getHash(), currentUser.getHash());
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        DataExchange.bus.register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        DataExchange.bus.unregister(this);
        super.onPause();
    }

    void updateSaveButton() {
        if (isSavingProcess) {
            respondButton.setText(getResources().getString(R.string.responding_process));
            respondButton.setBackgroundColor(getResources().getColor(R.color.flat_emerald));
        } else {
            respondButton.setText(getResources().getString(R.string.respond_button));
            respondButton.setBackgroundColor(getResources().getColor(R.color.flat_nephritis));
        }
    }

    @Subscribe
    public void getRespond(String respondHash) {
        isSavingProcess = false;
        updateSaveButton();
        if (respondHash == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.you_respond), Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void getEvents(ArrayList<Event> events) {
        if (events.size() > 0) {
            currentEvent = events.get(0);
            showEvent(currentEvent);
        }

        findViewById(R.id.content_wrapper).setVisibility(View.VISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    private void showEvent(Event event) {
        final TextView message = (TextView) findViewById(R.id.message_view_text);
        final TextView author = (TextView) findViewById(R.id.view_author);
        final TextView date = (TextView) findViewById(R.id.view_date);
        final ImageView userPhoto = (ImageView) findViewById(R.id.user_photo);

        User authorObj = DataExchange.getUserByHash(event.getAuthorHash());

        if (authorObj != null) {
            date.setText(android.text.format.DateFormat.format("dd.MM.yy hh:mm", event.getDate()));
            message.setText(event.getMessage());
            author.setText(authorObj.getFirstName() + " " + authorObj.getLastName());


            Picasso.with(this).load("https://graph.facebook.com/" + authorObj.getFbId().toString() + "/picture?type=square").into(userPhoto);

        }
    }
}
