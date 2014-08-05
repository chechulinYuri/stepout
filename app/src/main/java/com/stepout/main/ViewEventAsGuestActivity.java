package com.stepout.main;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewEventAsGuestActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        DataExchange.getEventsByUser(getIntent().getStringExtra(MainActivity.USER_HASH_FOR_VIEW_EVENT_ACTIVITY));
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
    public void getEvents(ArrayList<Event> events) {
        if (events.size() > 0) {
            showEvent(events.get(0));
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
