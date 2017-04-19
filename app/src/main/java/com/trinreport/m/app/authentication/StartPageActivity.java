package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.trinreport.m.app.MainActivity;
import com.trinreport.m.app.R;

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

        // inflate layout
        setContentView(R.layout.activity_start_page);

        // get references
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mGetStartedButton = (Button) findViewById(R.id.get_started_button);

        // add event listener for get started button
        mGetStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if user has already authenticated using their trinity email
                boolean authenticated = mSharedPref.getBoolean("authenticated", false);
                Log.d(TAG, "User authenticated: " + authenticated);
                if (authenticated) {
                    // redirect to authentication start page
                    startMainActivity();
                }
                // start activity for requesting auth token
                startRequestTokenActivity();
            }
        });
    }

    /**
     * Wrapper for starting RequestTokenActivity
     */
    private void startRequestTokenActivity() {
        Intent i = new Intent(this, RequestTokenActivity.class);
        startActivity(i);
    }

    /**
     * Wrapper for starting MainActivity
     */
    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        // clear application history stack
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}