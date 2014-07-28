package main;

import android.app.Activity;
import android.os.Bundle;

import com.example.admin.stepout.R;

public class MainActivity extends Activity {

    public static final String EVENT_TABLE_NAME = "Event";
    public static final String USER_TABLE_NAME = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);

        DataExchange dataExchange = new DataExchange(this);

        // TEST USER SAVE
        /*User testUser = new User("Yuri", "Chechulin", "+79531233212", "http://cs618726.vk.me/v618726028/d03e/_EawHr8ITXc.jpg", "42323");
        User usr = dataExchange.saveUserToParseCom(testUser);

        if (usr != null) {
            Toast toast = Toast.makeText(this, usr.hash, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "User not saved", Toast.LENGTH_LONG);
            toast.show();
        }*/


        // TEST USER GET
        /*User usr = dataExchange.getUserFromParseCom("42323");
        if (usr != null) {
            Toast toast = Toast.makeText(this, usr.hash, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "User not found", Toast.LENGTH_LONG);
            toast.show();
        }*/

        // TEST EVENT SAVE
        /*Event testEvent = new Event("GO ASD", new ParseGeoPoint(40.0, -30.0), Arrays.asList("asd", "qwe"), "dhdhdfhdf", System.currentTimeMillis(), 3);
        Event evt = dataExchange.saveEventToParseCom(testEvent);

        if (evt != null) {
            Toast toast = Toast.makeText(this, evt.message + " " + evt.hash, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "Event not found", Toast.LENGTH_LONG);
            toast.show();
        }*/

        //TODO

        // TEST GET EVENTS BY USER HASH
        /*ArrayList<Event> events = dataExchange.getEventsByUser("asdadqwe123");
        String res = "";
        for (int i = 0; i < events.size(); i++) {
            res += events.get(i).message + " ";
        }
        Toast toast = Toast.makeText(this, res, Toast.LENGTH_LONG);
        toast.show();*/

    }
}
