package com.stepout.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
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
import com.stepout.main.models.Event;
import com.stepout.main.models.User;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewEventAsGuestActivity extends ActionBarActivity {

    private Event currentEvent;
    private User currentUser;
    private boolean isSavingProcess;
    private Button respondButton;
    private User eventAuthor;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_as_guest);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = UserKeeper.readUserFromSharedPref(this);

        respondButton = (Button) findViewById(R.id.respond_event_button);

        respondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isSavingProcess) {
                    if (currentEvent != null && currentUser != null) {
                        isSavingProcess = true;
                        DataExchange.respondToEvent(currentEvent.getHash(), currentUser.getHash());
                        pd.show();
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

        pd = new ProgressDialog(this);
        pd.setTitle(getResources().getString(R.string.loading_process));
        pd.setCancelable(false);
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

    // 2.0 and above
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    // Before 2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe
    public void getRespondStatus(String status) {
        isSavingProcess = false;
        pd.hide();
        if (status.equals(DataExchange.STATUS_FAIL)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
        } else if (status.equals(DataExchange.STATUS_SUCCESS)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.you_respond), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, ViewEventAsRespondentActivity.class);
            intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, currentEvent.getHash());

            ParsePush push = new ParsePush();
            push.setChannel(DataExchange.PREFIX_FOR_CHANNEL_NAME + currentEvent.getHash());
            try {
                JSONObject data = new JSONObject("{\"action\": \"com.stepout.main.CustomReceiver.SHOW_EVENT\", \"message\": \"" + getString(R.string.user_joined_event, currentEvent.getRespondentsHash().size()) + "\", \"" + DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY + "\": \"" + currentEvent.getHash() + "\", \"author\": \"" + currentEvent.getAuthorHash() + "\"}");
                push.setData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            push.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    PushService.subscribe(getApplicationContext(), DataExchange.PREFIX_FOR_CHANNEL_NAME + currentEvent.getHash(), MainActivity.class);
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
