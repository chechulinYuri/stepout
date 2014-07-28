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
        setContentView(R.layout.activity_my);

        DataExchange dataExchange = new DataExchange(this);

        // TEST USER SAVE
        /*User testUser = new User("Yuri", "Chechulin", "+79531233212", "http://cs618726.vk.me/v618726028/d03e/_EawHr8ITXc.jpg", "1298");
        User usr = dataExchange.saveToParseCom(testUser);

        if (usr != null) {
            Log.d("qwe", usr.hash);
        } else {
            Log.d("qwe", "usr null");
        }*/


        // TEST USER GET
        /*User usr = dataExchange.getFromParseCom("12982");
        if (usr != null) {
            Toast toast = Toast.makeText(this, usr.firstName, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "User not found", Toast.LENGTH_LONG);
            toast.show();
        }*/

    }
}
