package com.stepout.main;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.squareup.otto.Subscribe;
import com.stepout.main.models.Event;
import com.stepout.main.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class EditEventActivity extends FragmentActivity {

    private static Integer day;
    private static Integer month;
    private static Integer year;
    private static Integer hour;
    private static Integer minutes;
    private static String message;
    private static String category;
    private static TextView pickTimeView;
    private static TextView pickDateView;
    private static Button saveButton;
    private static AbsSpinner categorySpinner;
    private static LatLng eventLocation;
    private User currentUser;
    private static Event currentEvent;
    private boolean isSavingProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        currentUser = UserKeeper.readUserFromSharedPref(this);
        eventLocation = new LatLng(getIntent().getDoubleExtra(DataExchange.LOCATION_OF_NEW_EVENT_LAT_KEY, 0), getIntent().getDoubleExtra(DataExchange.LOCATION_OF_NEW_EVENT_LNG_KEY, 0));

        implementSpinner();

        final String currentEventHash = getIntent().getStringExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY);

        for (int i = 0; i < DataExchange.uploadedEvents.size(); i++) {
            if (DataExchange.uploadedEvents.get(i).getHash().equals(currentEventHash)) {
                currentEvent = DataExchange.uploadedEvents.get(i);

                showEvent(currentEvent);
                break;
            }
        }

        final EditText messageEditText = (EditText) findViewById(R.id.message_edit_text);
        pickTimeView = (TextView) findViewById(R.id.choose_time_view);
        pickDateView = (TextView) findViewById(R.id.choose_date_view);
        saveButton = (Button) findViewById(R.id.save_event_button);

        pickTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        pickDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSavingProcess) {
                    message = messageEditText.getText().toString();

                    if (day != null && month != null && year != null && minutes != null && hour != null && message != null && message.length() > 0 && category != null) {
                        isSavingProcess = true;
                        Util.showLoadingDialog(EditEventActivity.this);
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, day, hour, minutes, 0);
                        Event event = new Event(
                                message,
                                new ParseGeoPoint(
                                        currentEvent.getCoordinates().getLatitude(),
                                        currentEvent.getCoordinates().getLongitude()),
                                category,
                                currentUser.getHash(),
                                cal.getTime(),
                                currentEvent.getRespondentsHash()
                        );

                        event.setHash(currentEvent.getHash());

                        DataExchange.updateEvent(event, currentUser.getHash());
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.create_event_complete_all_fields_error, Toast.LENGTH_LONG).show();
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

    private void showEvent(Event event) {
        final EditText message = (EditText) findViewById(R.id.message_edit_text);
        final TextView date = (TextView) findViewById(R.id.choose_date_view);
        final TextView time = (TextView) findViewById(R.id.choose_time_view);

        date.setText(android.text.format.DateFormat.format("dd.MM.yy", event.getDate()));
        time.setText(android.text.format.DateFormat.format("hh:mm", event.getDate()));
        message.setText(event.getMessage());

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
        int spinnerPosition = adapter.getPosition(event.getCategory());
        categorySpinner.setSelection(spinnerPosition);
        category = event.getCategory();

        final Calendar c = Calendar.getInstance();
        c.setTime(currentEvent.getDate());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

    }

    @Subscribe
    public void updateEventStatus(String status) {
        if (status.equals(DataExchange.STATUS_UPDATE_EVENT_SUCCESS)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.event_saved), Toast.LENGTH_LONG).show();

            ParsePush push = new ParsePush();
            ParseQuery pushQuery = ParseInstallation.getQuery();
            pushQuery.whereNotEqualTo("installationId", ParseInstallation.getCurrentInstallation().getInstallationId());
            pushQuery.whereEqualTo("channels", DataExchange.PREFIX_FOR_CHANNEL_NAME + currentEvent.getHash());
            push.setQuery(pushQuery);
            try {
                JSONObject data = new JSONObject("{\"action\": \"com.stepout.main.CustomReceiver.SHOW_EVENT\", \"message\": \"" + getString(R.string.author_updated_event, currentEvent.getRespondentsHash().size()) + "\", \"" + DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY + "\": \"" + currentEvent.getHash() + "\", \"author\": \"" + currentEvent.getAuthorHash() + "\"}");
                push.setData(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            push.sendInBackground();
        } else if(status.equals(DataExchange.STATUS_UPDATE_EVENT_FAIL)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(this, ViewEventAsAuthorActivity.class);
        intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, currentEvent.getHash());
        startActivity(intent);

        Util.dismissLoadingDialog();
        isSavingProcess = false;
    }

    public void implementSpinner() {
        final String[] categories = DataExchange.categories.keySet().toArray(new String[DataExchange.categories.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner = (AbsSpinner) findViewById(R.id.category_spinner);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = categories[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            c.setTime(currentEvent.getDate());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int h, int m) {
            hour = h;
            minutes = m;
            pickTimeView.setText(hour + ":" + minutes);
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            c.setTime(currentEvent.getDate());
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dp = new DatePickerDialog(getActivity(), this, year, month, day);
            dp.getDatePicker().setMinDate(c.getTimeInMillis());

            return dp;
        }

        public void onDateSet(DatePicker view, int y, int m, int d) {
            year = y;
            month = m;
            day = d;
            pickDateView.setText(day + "." + (month + 1) + "." + year);
        }
    }
}
