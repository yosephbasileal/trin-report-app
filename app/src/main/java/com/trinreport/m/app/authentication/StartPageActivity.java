package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.trinreport.m.app.ApplicationContext;
import com.trinreport.m.app.MainActivity;
import com.trinreport.m.app.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the very initial page an authenticated user sees
 * after they download the app
 */
public class StartPageActivity extends AppCompatActivity {

    // constants
    private static final String TAG = "StartPageActivity";

    // layout references
    Button mGetStartedButton;

    // other references
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // save context in singleton class
        //ApplicationContext.getInstance().init(getApplicationContext());

        // inflate layout
        setContentView(R.layout.activity_start_page);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // add listener for get started button
        mGetStartedButton = (Button) findViewById(R.id.get_started_button);
        mGetStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if user has already authenticated using their trinity email
                boolean authenticated = mSharedPref.getBoolean("authenticated", false);
                if (!authenticated) {
                    // redirect to authentication start page
                    startMainActivity();
                }

                // start activity for requesting auth token
                startRequestTokenActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startRequestTokenActivity() {
        Intent i = new Intent(this, RequestTokenActivity.class);
        startActivity(i);
    }

    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}