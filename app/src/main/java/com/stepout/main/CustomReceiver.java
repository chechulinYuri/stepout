package com.stepout.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
            String currentEventHash = json.getString(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY);
            Log.d("ASD", currentEventHash);
            Intent newIntent = new Intent(context, ViewEventAsGuestActivity.class);
            intent.putExtra(DataExchange.EVENT_HASH_FOR_VIEW_EVENT_ACTIVITY_KEY, currentEventHash);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
