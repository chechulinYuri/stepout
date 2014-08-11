package com.stepout.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.PushService;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

public class ViewEventAsAuthorActivity extends ActionBarActivity {

    private Event currentEvent;
    private User currentUser;
    private boolean isRemovingProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_as_author);

        currentUser = UserKeeper.readUserFromSharedPref(this);
        String currentEventHash = getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY);

        boolean isEventUploaded = false;
        for (int i = 0; i < DataExchange.uploadedEvents.size(); i++) {
            if (DataExchange.uploadedEvents.get(i).getHash().equals(currentEventHash)) {
                currentEvent = DataExchange.uploadedEvents.get(i);

                showEvent(currentEvent);
                findViewById(R.id.content_wrapper).setVisibility(View.VISIBLE);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                isEventUploaded = true;
                break;
            }
        }

        if (!isEventUploaded) {
            DataExchange.getEventByHash(getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_event_as_author, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isRemovingProcess) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    Intent intent = new Intent(this, EditEventActivity.class);
                    intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, currentEvent.getHash());
                    startActivity(intent);
                    return true;

                case R.id.action_share:

                    return true;

                case R.id.action_delete:
                    isRemovingProcess = true;
                    DataExchange.removeEvent(currentEvent.getHash(), currentUser.getHash());
                    PushService.unsubscribe(getApplicationContext(), currentEvent.getHash());
                    ParsePush push = new ParsePush();
                    ParseQuery pushQuery = ParseInstallation.getQuery();
                    pushQuery.whereNotEqualTo("installationId", ParseInstallation.getCurrentInstallation().getInstallationId());
                    pushQuery.whereEqualTo("channels", currentEvent.getHash());
                    push.setQuery(pushQuery);
                    push.setMessage(getString(R.string.author_deleted_event, android.text.format.DateFormat.format("dd.MM.yy hh:mm", currentEvent.getDate())));
                    push.sendInBackground();
                    Intent intentDeletion = new Intent(this, MapsActivity.class);
                    startActivity(intentDeletion);
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
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

    @Subscribe
    public void getEvent(Event event) {
        if (event != null) {
            currentEvent = event;
            showEvent(currentEvent);
        }

        findViewById(R.id.content_wrapper).setVisibility(View.VISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    @Subscribe
    public void removeEventStatus(String status) {
        if (status == DataExchange.STATUS_REMOVE_SUCCESS) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.remove_success), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
        }

        isRemovingProcess = false;

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void showEvent(Event event) {
        final TextView message = (TextView) findViewById(R.id.message_view_text);
        final TextView author = (TextView) findViewById(R.id.view_author);
        final TextView date = (TextView) findViewById(R.id.view_date);
        final ImageView userPhoto = (ImageView) findViewById(R.id.user_photo);

        date.setText(android.text.format.DateFormat.format("dd.MM.yy hh:mm", event.getDate()));
        message.setText(event.getMessage());
        author.setText(currentUser.getFirstName() + " " + currentUser.getLastName());


        Picasso.with(this).load("https://graph.facebook.com/" + currentUser.getFbId().toString() + "/picture?type=square").into(userPhoto);

    }
}
