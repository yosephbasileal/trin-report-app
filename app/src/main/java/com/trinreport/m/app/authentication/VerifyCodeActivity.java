package com.trinreport.m.app.authentication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.trinreport.m.app.R;

public class VerifyCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        // Create a fragment and add to view
        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = new VerifyCodeFragment();
        fm.beginTransaction().add(R.id.verify_code_activity, frag).commit();
    }
}
