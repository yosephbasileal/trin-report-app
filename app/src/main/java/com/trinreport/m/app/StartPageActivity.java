package com.trinreport.m.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        // Create a fragment and add to view
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = new StartPageFragment();
        fm.beginTransaction().add(R.id.start_page_activity, frag).commit();
    }
}
