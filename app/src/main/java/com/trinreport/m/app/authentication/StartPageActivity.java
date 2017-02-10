package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.trinreport.m.app.MainActivity;
import com.trinreport.m.app.R;

/**
 * This is the very initial page an authenticated user sees after they download the app
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

        // check if user is authenticated (uses a boolean stored in shared prefs)
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean authenticated = mSharedPref.getBoolean("authenticated", false);
        if (authenticated) {
            // if user is authenticated, doesn't render this page, goes to main activity
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            return;
        }

        // inflate layout
        setContentView(R.layout.activity_start_page);

        // add listener for get started button
        mGetStartedButton = (Button) findViewById(R.id.get_started_button);
        mGetStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start activity for requesting auth token
                startRequestTokenActivity();
            }
        });
    }

    private void startRequestTokenActivity() {
        Intent i = new Intent(this, RequestTokenActivity.class);
        startActivity(i);
    }
}