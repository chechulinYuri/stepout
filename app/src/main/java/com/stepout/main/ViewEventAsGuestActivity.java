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

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SendCallback;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

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
                        DataExchange.respondToEvent(currentEvent.getHash(), currentUser);
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        boolean isEventUploaded = false;
        for (int i = 0; i < DataExchange.uploadedEvents.size(); i++) {
            if (DataExchange.uploadedEvents.get(i).getHash().equals(getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY))) {
                currentEvent = DataExchange.uploadedEvents.get(i);
                DataExchange.getUserByHash(currentEvent.getAuthorHash());
                isEventUploaded = true;
                Log.d("asd", "Get event from uploaded");
                break;
            }
        }

        if (!isEventUploaded) {
            Log.d("asd", "Get event from parse.com");
            DataExchange.getEventByHash(getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY));
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
            respondButton.setBackgroundColor(getResources().getColor(R.color.flat_turquoise));
        } else {
            respondButton.setText(getResources().getString(R.string.respond_button));
            respondButton.setBackgroundColor(getResources().getColor(R.color.flat_green_sea));
        }
    }

    @Subscribe
    public void getRespondStatus(String status) {
        isSavingProcess = false;
        updateSaveButton();
        if (status == DataExchange.STATUS_FAIL) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
        } else if (status == DataExchange.STATUS_SUCCESS) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.you_respond), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, ViewEventAsRespondentActivity.class);
            intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, currentEvent.getHash());
            ParsePush push = new ParsePush();
            push.setChannel(currentEvent.getHash());
            push.setMessage(getString(R.string.user_joined_event, currentEvent.getRespondents().size() + 1));
            //NEW SHIT
            try {
                JSONObject data = new JSONObject("{\"action\": \"com.stepout.main.CustomReceiver.SHOW_EVENT\", \"alert\": \"" + getString(R.string.user_joined_event, currentEvent.getRespondents().size() + 1) + "\", \"" + DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY + "\": \"" + currentEvent.getHash() + "\"}");
                push.setData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //ENDS HERE
            push.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    PushService.subscribe(getApplicationContext(), currentEvent.getHash(), MainActivity.class);
                }
            });
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
