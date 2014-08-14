package com.stepout.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.stepout.main.models.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 13.08.2014.
 */
public class Util {
    private static boolean isProgressDialogShowing;
    private static ProgressDialog pd;

    public static void showLoadingDialog(Context context) {
        if (!isProgressDialogShowing) {
            isProgressDialogShowing = true;
            Log.d("asd", "pd show");
            pd = new ProgressDialog(context);
            pd.setTitle(context.getResources().getString(R.string.loading_process));
            pd.setCancelable(false);
            pd.show();
        }
    }

    public static void dismissLoadingDialog() {
        if (isProgressDialogShowing) {
            isProgressDialogShowing = false;
            Log.d("asd", "pd dismiss");
            pd.dismiss();
            pd = null;
        }
    }

    public static void addEventToCal(Context ctx, Event event) {
        Geocoder asd = new Geocoder(ctx);
        List<Address> qwe = new ArrayList<Address>();
        try {
            qwe = asd.getFromLocation (event.getCoordinates().getLatitude(), event.getCoordinates().getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address ad = qwe.get(0);

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", event.getDate());
        intent.putExtra("allDay", true);
        intent.putExtra("rrule", "FREQ=YEARLY");
        intent.putExtra("endTime", event.getDate());
        intent.putExtra("title", event.getMessage());
        intent.putExtra("eventLocation", ad.getAddressLine(0) + " " + ad.getAddressLine(1) + " " + ad.getAddressLine(2) + " " + ad.getAddressLine(3));

        ctx.startActivity(intent);
    }
}
