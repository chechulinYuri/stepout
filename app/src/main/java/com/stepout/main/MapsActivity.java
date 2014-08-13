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
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.otto.Subscribe;
import com.stepout.main.models.Event;
import com.stepout.main.models.User;

import java.util.ArrayList;
import java.util.HashMap;


public class MapsActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private SupportMapFragment mapFragment;
    private static GoogleMap map;
    private LocationClient locationClient;
    private LocationManager service;
    private boolean allowConnection = true;
    private User currentUser;
    private static final LatLng nsk = new LatLng(54.940803, 83.074371);
    private boolean isUserPickLocationForNewEvent;
    private boolean isCategoriesLoaded;
    private boolean isEventsRefreshing;
    private LatLng locationOfNewEvent;
    private Button createEventButton;
    private Button chooseEventLocationButton;
    private Button cancelChoosingLocationButton;
    private boolean isSearching;
    private static LatLng currentLocation;
    private SubMenu filterSubMenu;
    private Menu menu;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static String LOG_TAG = "asd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        currentUser = UserKeeper.readUserFromSharedPref(this);

        createEventButton = (Button) findViewById(R.id.create_event_button);
        chooseEventLocationButton = (Button) findViewById(R.id.choose_location_for_new_event);
        cancelChoosingLocationButton = (Button) findViewById(R.id.cancel_choose_location_for_new_event);

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        map = mapFragment.getMap();
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);

        map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                for (Event e: DataExchange.uploadedEvents) {
                    if (e.getMarkerId().equals(marker.getId())) {

                        if (e.getAuthorHash().equals(currentUser.getHash())) {
                            Intent intent = new Intent(getApplicationContext(), ViewEventAsAuthorActivity.class);
                            intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, e.getHash());
                            startActivity(intent);
                            return;
                        }

                        for (String usrHash: e.getRespondentsHash()) {
                            if (usrHash.equals(currentUser.getHash())) {
                                Intent intent = new Intent(getApplicationContext(), ViewEventAsRespondentActivity.class);
                                intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, e.getHash());
                                startActivity(intent);
                                return;
                            }
                        }

                        Intent intent = new Intent(getApplicationContext(), ViewEventAsGuestActivity.class);
                        intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, e.getHash());
                        startActivity(intent);
                        return;
                    }
                }
            }
        });

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

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUserPickLocationForNewEvent = true;
                Toast.makeText(MapsActivity.this, R.string.choose_event_location_dialog, Toast.LENGTH_LONG).show();
                map.clear();

                createEventButton.setVisibility(View.GONE);
                cancelChoosingLocationButton.setVisibility(View.VISIBLE);
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isUserPickLocationForNewEvent) {
                    map.clear();
                    map.addMarker(new MarkerOptions().position(latLng));
                    locationOfNewEvent = latLng;
                    chooseEventLocationButton.setVisibility(View.VISIBLE);
                }
            }
        });

        chooseEventLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationOfNewEvent != null) {
                    map.clear();
                    drawMarkers(DataExchange.uploadedEvents);
                    chooseEventLocationButton.setVisibility(View.GONE);
                    cancelChoosingLocationButton.setVisibility(View.GONE);
                    isUserPickLocationForNewEvent = false;

                    Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
                    intent.putExtra(DataExchange.LOCATION_OF_NEW_EVENT_LAT_KEY, locationOfNewEvent.latitude);
                    intent.putExtra(DataExchange.LOCATION_OF_NEW_EVENT_LNG_KEY, locationOfNewEvent.longitude);
                    startActivity(intent);
                }
            }
        });

        cancelChoosingLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                drawMarkers(DataExchange.uploadedEvents);
                chooseEventLocationButton.setVisibility(View.GONE);
                cancelChoosingLocationButton.setVisibility(View.GONE);
                createEventButton.setVisibility(View.VISIBLE);
                isUserPickLocationForNewEvent = false;
            }
        });

        if (DataExchange.categories.size() == 0) {
            DataExchange.getCategories();
            Util.showLoadingDialog(this);
        } else {
            isCategoriesLoaded = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_view, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                DataExchange.searchEventsInRadius(s, map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        this.menu = menu;

        if (isCategoriesLoaded) {
            addFilterMenuOption();
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!isEventsRefreshing) {
                    isEventsRefreshing = true;
                    Util.showLoadingDialog(this);

                    DataExchange.uploadedEvents.clear();
                    DataExchange.getEventsInRadius(currentLocation.latitude, currentLocation.longitude);
                }
                return true;
        }

        // Handle item selection
        if (item.isCheckable()) {
            if (item.isChecked()) {
                item.setChecked(false);
            } else {
                item.setChecked(true);
            }

            initFilterQuery();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initFilterQuery() {
        Util.showLoadingDialog(this);

        ArrayList<String> categories = new ArrayList<String>();
        categories.addAll(DataExchange.categories.keySet());

        HashMap<String, Boolean> categoriesFilter = new HashMap<String, Boolean>();
        boolean onlyRespondEvent = false;

        for (int i = 0; i < filterSubMenu.size(); i++) {
            MenuItem item = filterSubMenu.getItem(i);

            if (categories.contains(item.getTitle().toString())) {
                categoriesFilter.put(item.getTitle().toString(), item.isChecked());
            } else if (item.getTitle().toString().equals(getResources().getString(R.string.filter_only_respond_event)) && item.isChecked()) {
                onlyRespondEvent = true;
            }
        }

        DataExchange.filterEventsInRadius(categoriesFilter, onlyRespondEvent, currentLocation.latitude, currentLocation.longitude, currentUser.getHash());
    }

    @Subscribe
    public void getEvents(ArrayList<Event> events) {
        DataExchange.uploadedEvents.addAll(events);
        Log.d(LOG_TAG, "mapactivity get events");
        isEventsRefreshing = false;
        if (isCategoriesLoaded) {
            Util.dismissLoadingDialog();
            Toast.makeText(this, getResources().getString(R.string.map_updated), Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "and category here");
            drawMarkers(DataExchange.uploadedEvents);
        }
    }

    private void addFilterMenuOption() {
        filterSubMenu = menu.findItem(R.id.action_filter).getSubMenu();
        filterSubMenu.clear();

        for (String category: DataExchange.categories.keySet()) {
            filterSubMenu.add(category).setCheckable(true);
        }

        filterSubMenu.add(getResources().getString(R.string.filter_only_respond_event)).setCheckable(true);
    }

    @Subscribe
    public void getCategories(HashMap<String, Bitmap> categories) {
        Log.d(LOG_TAG, "mapactivity get categories");
        isCategoriesLoaded = true;

        addFilterMenuOption();

        Util.dismissLoadingDialog();
        if (DataExchange.uploadedEvents.size() > 0) {
            Log.d(LOG_TAG, "and events here");
            drawMarkers(DataExchange.uploadedEvents);
        }
    }

    @Subscribe
    public void searchOrFilterCallback(String status) {
        Util.dismissLoadingDialog();

        if (status.equals(DataExchange.STATUS_SEARCH_SUCCESS)) {
            drawMarkers(DataExchange.searchEventResult);

            if (DataExchange.searchEventResult.size() == 0) {
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.no_search_event_dialog), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.events_updated), Toast.LENGTH_LONG).show();
            }
        } else if (status.equals(DataExchange.STATUS_SEARCH_FAIL)) {
            Toast.makeText(MapsActivity.this, getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
            drawMarkers(DataExchange.uploadedEvents);
        } else if (status.equals(DataExchange.STATUS_FILTER_FAIL)) {
            Toast.makeText(MapsActivity.this, getResources().getString(R.string.some_error), Toast.LENGTH_LONG).show();
            drawMarkers(DataExchange.uploadedEvents);
        } else if (status.equals(DataExchange.STATUS_FILTER_SUCCESS)) {
            drawMarkers(DataExchange.filterEventResult);

            if (DataExchange.filterEventResult.size() == 0) {
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.no_search_event_dialog), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MapsActivity.this, getResources().getString(R.string.events_updated), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void drawMarkers(ArrayList<Event> events) {
        map.clear();
        for (int i = 0; i < events.size(); i++) {
            Event currentEvent = events.get(i);
            LatLng latLng = new LatLng(currentEvent.getCoordinates().getLatitude(), events.get(i).getCoordinates().getLongitude());
            String category = currentEvent.getCategory();
            String snippet = currentEvent.getMessage() + " " + getString(R.string.attenders_text, currentEvent.getRespondentsHash().size());
            IconGenerator iconGenerator = new IconGenerator(this);
            if (currentUser.getHash().compareTo(currentEvent.getAuthorHash()) == 0) {
                iconGenerator.setStyle(IconGenerator.STYLE_GREEN);
            }
            else if (currentEvent.getRespondentsHash().indexOf(currentUser.getHash()) != -1) {
                iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
            }
            else {
                iconGenerator.setStyle(IconGenerator.STYLE_BLUE);
            }
            ImageView imageView = new ImageView(this);
            Bitmap categoryIcon = DataExchange.categories.get(category);
            Bitmap bmp;
            if (categoryIcon != null) {
                iconGenerator.setContentView(imageView);
                imageView.setImageBitmap(categoryIcon);
                imageView.setPadding(10, 10, 10, 10);
                bmp = iconGenerator.makeIcon();
            }
            else {
                bmp = iconGenerator.makeIcon(category);
            }
            currentEvent.setMarkerId(map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(category)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                ).getId());
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
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Util.showLoadingDialog(this);
            DataExchange.getEventsInRadius(currentLocation.latitude, currentLocation.longitude);
        }
        else {
            currentLocation = nsk;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
            Util.showLoadingDialog(this);
            DataExchange.getEventsInRadius(currentLocation.latitude, currentLocation.longitude);
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
                            map.setMyLocationEnabled(true);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));
                            Util.showLoadingDialog(getActivity());
                            DataExchange.getEventsInRadius(currentLocation.latitude, currentLocation.longitude);
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }

    @Override
    protected void onResume() {
        DataExchange.bus.register(this);
        createEventButton.setVisibility(View.VISIBLE);
        cancelChoosingLocationButton.setVisibility(View.GONE);
        chooseEventLocationButton.setVisibility(View.GONE);
        drawMarkers(DataExchange.uploadedEvents);
        super.onResume();
    }

    @Override
    protected void onPause() {
        DataExchange.bus.unregister(this);
        super.onPause();
    }

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
}