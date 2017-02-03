package com.trinreport.m.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Emergency extends AppCompatActivity {

    public static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORT_ID";
    public static final String EMERGENCY_STATUS = "com.trinreport.m.app.extra.STATUS";
    public static final String EMERGENCY_STATUS_FILTER = "com.trinreport.m.app.action.new_status";

    private String mStatus;
    private TextView mStatusTextview;

    private String mReportId;
    private Location mLocation;

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        mReportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        mStatusTextview = (TextView) findViewById(R.id.statusTextView);
        updateStatus("Not Received");

        // start background service for updating status and gps points
        registerBroadcastReceiver();
        EmergencyStatusService.startActionUpdateStatus(this, mReportId, 0,0);
    }

    private void registerBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra(EMERGENCY_STATUS);
                updateStatus(status);
            }
        };
        IntentFilter progressfilter = new IntentFilter(EMERGENCY_STATUS_FILTER);
        registerReceiver(broadcastReceiver, progressfilter);
    }

    private void updateStatus(String status) {
        mStatusTextview.setText(status);
        if (status.equals("Received")) {
            mStatusTextview.setTextColor(Color.GREEN);
        }
    }
}
