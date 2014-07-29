package main;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.example.admin.stepout.R;

import java.util.ArrayList;

import model.DataExchange;
import model.Event;

public class MainActivity extends FragmentActivity {

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
