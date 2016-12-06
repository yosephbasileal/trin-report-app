package com.trinreport.m.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.trinreport.m.app.addReport.AddReportStep1;

public class AddReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        Fragment fragStep1 = AddReportStep1.newInstance();

        FragmentManager fm = getFragmentManager();

        fm.beginTransaction().add(R.id.activity_add_report, fragStep1).commit();
    }
}
