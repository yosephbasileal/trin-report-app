package com.trinreport.m.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.trinreport.m.app.authentication.StartPageActivity;
import com.trinreport.m.app.emergency.EmergencyTabFragment;
import com.trinreport.m.app.followup.FollowupTabFragment;
import com.trinreport.m.app.report.ReportTabFragment;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    FragmentManager fm;
    Fragment frag_emergency, frag_report, frag_history, frag_settings;
    BottomBar bottomBar;
    SharedPreferences mSharedPref;

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if user has already authenticated using their trinity email
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean authenticated = mSharedPref.getBoolean("authenticated", false);
        if (!authenticated) {
            // redirect to authentication start page
            startStartPageActivity();
            return;
        }

        // if authenticated, render main page activity
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);

        // fragments for the four main tabs on the main page activity
        fm = getFragmentManager();
        frag_emergency = EmergencyTabFragment.newInstance();
        frag_report = ReportTabFragment.newInstance();
        frag_history = FollowupTabFragment.newInstance();
        frag_settings = SettingsTabFragment.newInstance();

        // initially show emergency button tab
        if(!frag_emergency.isAdded()) {
            Log.d(TAG, "Adding emergency fragment");
            fm.beginTransaction().add(R.id.main_activity_container, frag_emergency).commit();
        }

        // bottom bar to switch between four tabs
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        // add listeners to switch between tabs
        if (bottomBar != null) {
            bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
                @Override
                public void onTabSelected(@IdRes int tabId) {
                    if (tabId == R.id.tab_emergency) {
                        fm.beginTransaction().replace(R.id.main_activity_container,
                                frag_emergency).commit();
                    } else if (tabId == R.id.tab_report) {
                        fm.beginTransaction().replace(R.id.main_activity_container,
                                frag_report).commit();
                    } else if (tabId == R.id.tab_history) {
                        fm.beginTransaction().replace(R.id.main_activity_container,
                                frag_history).commit();
                        setSupportActionBar(mToolbar);
                    } else if (tabId == R.id.tab_settings) {
                        fm.beginTransaction().replace(R.id.main_activity_container,
                                frag_settings).commit();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // save context in singleton class
        ApplicationContext.getInstance().init(this);
    }

    private void startStartPageActivity() {
        Intent i = new Intent(this, StartPageActivity.class);
        startActivity(i);
    }
}
