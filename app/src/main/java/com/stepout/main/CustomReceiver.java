package com.stepout.main;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Admin on 11.08.2014.
 */
public class CustomReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String alert = json.getString("message");
            String currentEventHash = json.getString(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY);
            String author = json.getString("author");
            Log.d("ASD", currentEventHash);
            Log.d("AUTHOR", author);


            Intent newIntent = new Intent(context, ViewEventAsAuthorActivity.class);
            newIntent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, currentEventHash);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(true)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(alert))
                    .setContentText(alert)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            

            builder.setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
