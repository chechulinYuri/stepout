package com.stepout.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
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
import com.stepout.main.models.Event;
import com.stepout.main.models.User;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewEventAsRespondentActivity extends ActionBarActivity {

    private Event currentEvent;
    private User eventAuthor;
    private User currentUser;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_as_author);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = UserKeeper.readUserFromSharedPref(this);

        boolean isEventUploaded = false;
        for (int i = 0; i < DataExchange.uploadedEvents.size(); i++) {
            if (DataExchange.uploadedEvents.get(i).getHash().equals(getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY))) {
                currentEvent = DataExchange.uploadedEvents.get(i);
                DataExchange.getUserByHash(currentEvent.getAuthorHash());
                isEventUploaded = true;
                break;
            }
        }

        if (!isEventUploaded) {
            DataExchange.getEventByHash(getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY));
        }

        pd = new ProgressDialog(this);
        pd.setTitle(getResources().getString(R.string.loading_process));
        pd.setCancelable(false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_event_as_respondent, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_call:

                return true;

            case R.id.action_share:

                return true;

            case R.id.action_unresponse:
                pd.show();
                DataExchange.unresponseFromEvent(currentEvent.getHash(), currentUser.getHash());
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

    @Subscribe
    public void getUnresponseStatus(String status) {
        pd.hide();
        if (status.equals(DataExchange.STATUS_SUCCESS)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.you_unrespond), Toast.LENGTH_LONG).show();

            PushService.unsubscribe(getApplicationContext(), DataExchange.PREFIX_FOR_CHANNEL_NAME + currentEvent.getHash());
            ParsePush push = new ParsePush();
            ParseQuery pushQuery = ParseInstallation.getQuery();
            pushQuery.whereNotEqualTo("installationId", ParseInstallation.getCurrentInstallation().getInstallationId());
            pushQuery.whereEqualTo("channels", DataExchange.PREFIX_FOR_CHANNEL_NAME + currentEvent.getHash());
            push.setQuery(pushQuery);
            //push.setMessage(getString(R.string.user_do_not_attend_event));
            try {
                JSONObject data = new JSONObject("{\"action\": \"com.stepout.main.CustomReceiver.SHOW_EVENT\", \"message\": \"" + getString(R.string.user_do_not_attend_event, currentEvent.getRespondentsHash().size() - 1) + "\", \"" + DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY + "\": \"" + currentEvent.getHash() + "\", \"author\": \"" + currentEvent.getAuthorHash() + "\"}");
                push.setData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            push.sendInBackground();
            Intent intentDeletion = new Intent(this, MapsActivity.class);
            startActivity(intentDeletion);
        } else if (status.equals(DataExchange.STATUS_FAIL)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
        }
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
