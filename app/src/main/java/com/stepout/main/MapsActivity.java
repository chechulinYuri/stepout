package com.stepout.main;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private SupportMapFragment mapFragment;
    private static GoogleMap map;
    private LocationClient locationClient;
    private LocationManager service;
    private boolean allowConnection = true;
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_maps);


        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        locationClient = new LocationClient(this, this, this);
        service = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            TurnOnGpsDialog dialog = new TurnOnGpsDialog();
            dialog.show(getSupportFragmentManager(), "TurnOnGpsDialog");
        }
        else {
            allowConnection = false;
            locationClient.connect();
        }
    }

    @Subscribe
    public void getEvents(ArrayList<Event> events) {
        DataExchange.uploadedEvents.addAll(events);
        //Log.d("asd", DataExchange.uploadedEvents.size() + "");
        for (int i = 0; i < DataExchange.uploadedEvents.size(); i++) {
            Event currentEvent = DataExchange.uploadedEvents.get(i);
            LatLng latLng = new LatLng(currentEvent.getCoordinates().getLatitude(), DataExchange.uploadedEvents.get(i).getCoordinates().getLongitude());
            String category = currentEvent.getCategory();
            String snippet = currentEvent.getMessage() + " Attenders: " + currentEvent.getResponsesCount();
            IconGenerator iconGenerator = new IconGenerator(this);
            iconGenerator.setStyle(IconGenerator.STYLE_RED);
            DataExchange.getCategories();
            Bitmap bmp = iconGenerator.makeIcon(DataExchange.categories.get(category));
            //Bitmap bmp = iconGenerator.makeIcon(/*category*/);
            map.addMarker(new MarkerOptions().position(latLng).title(category).snippet(snippet).icon(BitmapDescriptorFactory.fromBitmap(bmp)));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if ((!locationClient.isConnected()) && (service.isProviderEnabled(LocationManager.GPS_PROVIDER)) && (allowConnection)) {
            allowConnection = false;
            locationClient.connect();
        }
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        locationClient.disconnect();
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        locationClient.connect();
                        break;
                }

        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode =  GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog( resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        /*Location location = null;
        while (location == null) {
            location = locationClient.getLastLocation();
            //LocationManager asd = (LocationManager)getSystemService(LOCATION_SERVICE);
            //location = asd.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }*/
        Location location = locationClient.getLastLocation();
        locationClient.disconnect();
        //LocationManager asd = (LocationManager)getSystemService(LOCATION_SERVICE);
        //location = asd.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            DataExchange.getEventsInRadius(location.getLatitude(), location.getLongitude());
        }
        else {
            LatLng latLng = new LatLng(-31.90, 115.86);
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            DataExchange.getEventsInRadius(-31.90, 115.86);
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    /*
     * Google Play services can resolve some errors it detects.
     * If the error has a resolution, try sending an Intent to
     * start a Google Play services activity that can resolve
     * error.
     */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
            * Thrown if Google Play services canceled the original
            * PendingIntent
            */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    public static class TurnOnGpsDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.turn_on_gps_dialog)
                    .setPositiveButton(R.string.settings_text, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            LatLng sydney = new LatLng(-33.867, 151.206);
                            map.setMyLocationEnabled(true);
                            map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                            dismiss();
                        }
                    });
            return builder.create();
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
}