package com.trinreport.m.app.authentication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.trinreport.m.app.R;

public class RequestTokenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_token);

        // Create a fragment and add to view
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = new RequestTokenFragment();
        fm.beginTransaction().add(R.id.request_token_activity, frag).commit();
    }
}
