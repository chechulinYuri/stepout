package com.stepout.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.PushService;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.stepout.main.models.Event;
import com.stepout.main.models.User;

public class ViewEventAsAuthorActivity extends ActionBarActivity {

    private Event currentEvent;
    private User currentUser;
    private boolean isRemovingProcess;
    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_event_as_author);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
            Log.d("asd", "event loading from parse com " + currentEventHash);
            DataExchange.getEventByHash(currentEventHash);
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
                    if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                            FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
                        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                                .setLink("https://developers.facebook.com/android")
                                .setName(getString(R.string.app_name))
                                .setCaption(getString(R.string.fb_share_caption))
                                .setPicture("http://files.parsetfss.com/ba2c63d0-4860-42a0-9547-7d01e94d4446/tfss-371c4d8e-35e1-4257-a8f5-0fbb6a0670f9-Card-Games.png")
                                .build();
                        uiHelper.trackPendingDialogCall(shareDialog.present());

                    } else {
                        publishFeedDialog();
                    }
                    return true;

                case R.id.action_delete:
                    Util.showLoadingDialog(this);
                    isRemovingProcess = true;
                    DataExchange.removeEvent(currentEvent.getHash(), currentUser.getHash());
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        DataExchange.bus.register(this);
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        DataExchange.bus.unregister(this);
        super.onPause();
        uiHelper.onPause();
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
        if (event.getHash() != null) {
            currentEvent = event;
            showEvent(currentEvent);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.event_not_found), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        findViewById(R.id.content_wrapper).setVisibility(View.VISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    @Subscribe
    public void removeEventStatus(String status) {

        Util.dismissLoadingDialog();

        if (status.equals(DataExchange.STATUS_REMOVE_SUCCESS)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.remove_success), Toast.LENGTH_LONG).show();

            PushService.unsubscribe(getApplicationContext(), DataExchange.PREFIX_FOR_CHANNEL_NAME + currentEvent.getHash());
            ParsePush push = new ParsePush();
            ParseQuery pushQuery = ParseInstallation.getQuery();
            pushQuery.whereNotEqualTo("installationId", ParseInstallation.getCurrentInstallation().getInstallationId());
            pushQuery.whereEqualTo("channels", DataExchange.PREFIX_FOR_CHANNEL_NAME + currentEvent.getHash());
            push.setQuery(pushQuery);
            push.setMessage(getString(R.string.author_deleted_event, android.text.format.DateFormat.format("dd.MM.yy hh:mm", currentEvent.getDate())));
            push.sendInBackground();

            Intent intent = new Intent(this, MapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (status.equals(DataExchange.STATUS_REMOVE_FAIL)) {
            isRemovingProcess = false;
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
        author.setText(currentUser.getFirstName() + " " + currentUser.getLastName());


        Picasso.with(this).load("https://graph.facebook.com/" + currentUser.getFbId().toString() + "/picture?type=square").into(userPhoto);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }

            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }
        });
    }

    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", getString(R.string.app_name));
        params.putString("caption", getString(R.string.fb_share_caption));
        params.putString("link", "https://developers.facebook.com/android");
        params.putString("picture", "http://files.parsetfss.com/ba2c63d0-4860-42a0-9547-7d01e94d4446/tfss-371c4d8e-35e1-4257-a8f5-0fbb6a0670f9-Card-Games.png");

        WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(), params)).setOnCompleteListener(new WebDialog.OnCompleteListener() {
            @Override
            public void onComplete(Bundle values, FacebookException error) {
                if (error == null) {
                    final String postId = values.getString("post_id");
                    if (postId != null) {
                        Toast.makeText(ViewEventAsAuthorActivity.this, "Posted story, id: "+postId, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ViewEventAsAuthorActivity.this.getApplicationContext(), "Publish cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
                else  if (error instanceof FacebookOperationCanceledException) {
                    Toast.makeText(ViewEventAsAuthorActivity.this.getApplicationContext(), "Publish cancelled", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ViewEventAsAuthorActivity.this.getApplicationContext(), "Error posting story", Toast.LENGTH_SHORT).show();
                }
            }
        }).build();
        feedDialog.show();
    }
}
