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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.PushService;
import com.squareup.otto.Subscribe;

import java.util.Calendar;

public class CreateEventActivity extends FragmentActivity {

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
    private static LatLng eventLocation;
    private User currentUser;
    private boolean isSavingProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        currentUser = UserKeeper.readUserFromSharedPref(this);
        eventLocation = new LatLng(getIntent().getDoubleExtra(DataExchange.LOCATION_OF_NEW_EVENT_LAT_KEY, 0), getIntent().getDoubleExtra(DataExchange.LOCATION_OF_NEW_EVENT_LNG_KEY, 0));

        final EditText messageEditText = (EditText) findViewById(R.id.message_edit_text);
        pickTimeView = (TextView) findViewById(R.id.choose_time_view);
        pickDateView = (TextView) findViewById(R.id.choose_date_view);
        saveButton = (Button) findViewById(R.id.save_event_button);

        implementSpinner();

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
                        updateSaveButton();
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, day, hour, minutes, 0);
                        Event event = new Event(message, new ParseGeoPoint(eventLocation.latitude, eventLocation.longitude), category, currentUser.getHash(), cal.getTime(), null);
                        DataExchange.saveEventToParseCom(event);
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

    void updateSaveButton() {
        if (isSavingProcess) {
            saveButton.setText(getResources().getString(R.string.saving_process));
            saveButton.setBackgroundColor(getResources().getColor(R.color.flat_turquoise));
        } else {
            saveButton.setText(getResources().getString(R.string.save_button));
            saveButton.setBackgroundColor(getResources().getColor(R.color.flat_green_sea));
        }
    }

    @Subscribe
    public void savedEvent(Event event) {
        if (event.getHash() == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.event_saved), Toast.LENGTH_LONG).show();

            DataExchange.uploadedEvents.add(event);

            Intent intent = new Intent(this, ViewEventAsAuthorActivity.class);
            intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, event.getHash());
            PushService.subscribe(getApplicationContext(), event.getHash(), MainActivity.class);
            startActivity(intent);
        }
    }

    public void implementSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.event_categories));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.category_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categories = getResources().getStringArray(R.array.event_categories);
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
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int y, int m, int d) {
            year = y;
            month = m;
            day = d;
            pickDateView.setText(day + "." + (month + 1) + "." + year);
        }
    }
}
