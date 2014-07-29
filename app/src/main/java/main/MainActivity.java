package main;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.example.admin.stepout.R;

import java.util.ArrayList;

import model.DataExchange;
import model.Event;

public class MainActivity extends FragmentActivity {

    public static final String EVENT_TABLE_NAME = "Event";
    public static final String USER_TABLE_NAME = "User";
    public static final String RESPONSE_TABLE_NAME = "Response";
    public static final double EVENTS_VISIBILITY_RADIUS_IN_MILES = 400;

    private  LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.login_screen_layout);
        if (savedInstanceState == null) {
            loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, loginFragment);
        }
        else {
            loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }

        DataExchange dataExchange = new DataExchange(this);
    }
}
