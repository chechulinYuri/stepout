package com.stepout.main;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

public class ViewEventAsAuthorActivity extends ActionBarActivity {

    private Event currentEvent;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_as_author);

        currentUser = UserKeeper.readUserFromSharedPref(this);

        boolean isEventUploaded = false;
        for (int i = 0; i < DataExchange.uploadedEvents.size(); i++) {
            if (DataExchange.uploadedEvents.get(i).getHash().equals(getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY))) {
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
        switch (item.getItemId()) {
            case R.id.action_edit:

                return true;

            case R.id.action_share:

                return true;

            case R.id.action_delete:

                return true;

            default:
                return super.onOptionsItemSelected(item);
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

    @Subscribe
    public void getEvent(Event event) {
        if (event != null) {
            currentEvent = event;
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

        date.setText(android.text.format.DateFormat.format("dd.MM.yy hh:mm", event.getDate()));
        message.setText(event.getMessage());
        author.setText(currentUser.getFirstName() + " " + currentUser.getLastName());


        Picasso.with(this).load("https://graph.facebook.com/" + currentUser.getFbId().toString() + "/picture?type=square").into(userPhoto);

    }
}
