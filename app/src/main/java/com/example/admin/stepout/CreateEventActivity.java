package com.example.admin.stepout;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.ParseGeoPoint;

import java.util.Arrays;
import java.util.Calendar;

import model.DataExchange;
import model.Event;

public class CreateEventActivity extends FragmentActivity {

    private static Integer day;
    private static Integer month;
    private static Integer year;
    private static Integer hour;
    private static Integer minutes;
    private static String message;
    private static TextView pickTimeView;
    private static TextView pickDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        final EditText messageEditText = (EditText) findViewById(R.id.message_edit_text);
        pickTimeView = (TextView) findViewById(R.id.choose_time_view);
        pickDateView = (TextView) findViewById(R.id.choose_date_view);

        Spinner spinner = (Spinner) findViewById(R.id.category_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

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

        findViewById(R.id.save_event_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = messageEditText.getText().toString();

                if (day != null && month != null && year != null && minutes != null && hour != null && message != null && message.length() > 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, day, hour, minutes, 0);
                    Event event = new Event(message, new ParseGeoPoint(29.1, 30.4), Arrays.asList(new String[]{"One", "Two", "Three"}), "sdawe123eqwd", cal.getTime(), 0);
                    Event storedEvent = DataExchange.saveEventToParseCom(event);
                    if (storedEvent.getHash() != null) {
                        Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "NOT OK", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), R.string.create_event_complete_all_fields_error, Toast.LENGTH_LONG).show();
                }
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
            pickDateView.setText(day + "." + month + "." + year);
        }
    }
}
