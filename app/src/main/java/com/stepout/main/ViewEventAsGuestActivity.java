package com.stepout.main;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.stepout.main.R;

import java.text.DateFormat;
import java.util.ArrayList;

public class ViewEventAsGuestActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        ArrayList<Event> events = DataExchange.getEventsByUser("xbG6xtaFUm");

        if (events.size() > 0) {
            showEvent(events.get(0));
        }
    }

    private void showEvent(Event event) {
        final TextView message = (TextView) findViewById(R.id.message_view_text);
        final TextView author = (TextView) findViewById(R.id.view_author);
        final TextView date = (TextView) findViewById(R.id.view_date);

        User authorObj = DataExchange.getUserByHash(event.getAuthorHash());

        if (authorObj != null) {
            date.setText(android.text.format.DateFormat.format("dd.MM.yy hh:mm", event.getDate()));
            message.setText(event.getMessage());
            author.setText(authorObj.getFirstName() + " " + authorObj.getLastName());
        }
    }
}
