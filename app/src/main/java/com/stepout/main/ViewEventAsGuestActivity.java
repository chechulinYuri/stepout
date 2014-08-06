package com.stepout.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

public class ViewEventAsGuestActivity extends FragmentActivity {

    private Event currentEvent;
    private User currentUser;
    private boolean isSavingProcess;
    private Button respondButton;
    private User eventAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_as_guest);

        currentUser = UserKeeper.readUserFromSharedPref(this);

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

        boolean isEventUploaded = false;
        for (int i = 0; i < DataExchange.uploadedEvents.size(); i++) {
            if (DataExchange.uploadedEvents.get(i).getHash().equals(getIntent().getStringExtra(MainActivity.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY))) {
                currentEvent = DataExchange.uploadedEvents.get(i);
                DataExchange.getUserByHash(currentEvent.getAuthorHash());
                isEventUploaded = true;
                Log.d("asd", "Get event from uploaded");
                break;
            }
        }

        if (!isEventUploaded) {
            Log.d("asd", "Get event from parse.com");
            DataExchange.getEventByHash(getIntent().getStringExtra(MainActivity.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY));
        }
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

            Intent intent = new Intent(this, ViewEventAsRespondentActivity.class);
            intent.putExtra(MainActivity.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY, currentEvent.getHash());
            startActivity(intent);
        }
    }

    @Subscribe
    public void getEvent(Event event) {
        currentEvent = event;
        DataExchange.getUserByHash(currentEvent.getAuthorHash());
    }

    @Subscribe
    public void getAuthor(User user) {
        eventAuthor = user;
        showEvent(currentEvent);

        findViewById(R.id.content_wrapper).setVisibility(View.VISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    private void showEvent(Event event) {
        final TextView message = (TextView) findViewById(R.id.message_view_text);
        final TextView author = (TextView) findViewById(R.id.view_author);
        final TextView date = (TextView) findViewById(R.id.view_date);
        final ImageView userPhoto = (ImageView) findViewById(R.id.user_photo);

        date.setText(android.text.format.DateFormat.format("dd.MM.yy hh:mm", event.getDate()));
        message.setText(event.getMessage());
        author.setText(eventAuthor.getFirstName() + " " + eventAuthor.getLastName());

        Picasso.with(this).load("https://graph.facebook.com/" + eventAuthor.getFbId().toString() + "/picture?type=square").into(userPhoto);
    }
}
