package main;

import android.app.Activity;
import android.os.Bundle;

import com.example.admin.stepout.R;

import model.DataExchange;

public class MainActivity extends Activity {

    public static final String EVENT_TABLE_NAME = "Event";
    public static final String USER_TABLE_NAME = "User";
    public static final String RESPONSE_TABLE_NAME = "Response";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);

        DataExchange dataExchange = new DataExchange(this);
    }
}
