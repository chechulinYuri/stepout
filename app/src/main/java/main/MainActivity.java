package main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.example.admin.stepout.R;

import model.DataExchange;

public class MainActivity extends FragmentActivity {

    public static final String EVENT_TABLE_NAME = "Event";
    public static final String USER_TABLE_NAME = "User";
    public static final String RESPONSE_TABLE_NAME = "Response";

    private  LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen_layout);
       /* if (savedInstanceState == null) {
            loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(loginFragment, android.R.id.content).commit();
        }
        else {
            loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(android.R.id.content)
        }*/ //NEEDS TO BE FIXED

        DataExchange dataExchange = new DataExchange(this);
    }
}
