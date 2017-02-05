package com.trinreport.m.app.emergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.R;

import java.util.HashMap;
import java.util.Map;

public class Emergency extends AppCompatActivity {
    private static final String TAG = "EmergencyActivity";

    public static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORT_ID";
    public static final String EMERGENCY_STATUS = "com.trinreport.m.app.extra.STATUS";
    public static final String EMERGENCY_STATUS_FILTER = "com.trinreport.m.app.action.new_status";

    private static final String CS_PHONE_NUMBER = "8602972222";

    // view components
    private Toolbar mToolbar;
    private TextView mStatusTextview;
    private LinearLayout mExplanationLayout;
    private Button mExplanationButton;
    private EditText mExplanationEditText;
    private TextView mExplanationSentTextView;
    private CheckBox mCallmeCheckbox;
    private Button mCallCsSafetyButton;

    private String mExplanation;
    private String mReportId;
    private Boolean mReceieved = false;
    private Boolean mCanCallMe = true;

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // setup toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_emergency);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }

        // get report id
        mReportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        mStatusTextview = (TextView) findViewById(R.id.statusTextView);

        // initialize status to not recieved
        updateStatus();

        // start background service for requesting updated status drom rddp and sending gps points
        registerBroadcastReceiver();
        EmergencyStatusService.startActionUpdateStatus(this, mReportId);

        // text field for adding explanation about emergency situation
        mExplanationLayout = (LinearLayout) findViewById(R.id.explanation_layout);
        mExplanationSentTextView = (TextView) findViewById(R.id.explanation_sent_textview);
        mExplanationEditText = (EditText) findViewById(R.id.explanation);
        mExplanationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mExplanation = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // button click sends explanation to rddp
        mExplanationButton = (Button) findViewById(R.id.send_explanation);
        mExplanationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send to rddp server
                sendExplanation();

                // hide text field layout
                mExplanationLayout.setVisibility(View.GONE);

                // show sent text
                mExplanationSentTextView.setText("You sent: " + mExplanation);
                mExplanationSentTextView.setVisibility(View.VISIBLE);
            }
        });

        // checkbox
        mCallmeCheckbox = (CheckBox) findViewById(R.id.callme_checkbox);
        mCallmeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCanCallMe = isChecked;

                // send update to server
                updateCallmeCheckbox();
            }
        });

        mCallCsSafetyButton = (Button) findViewById(R.id.call_csafety_button);
        mCallCsSafetyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + CS_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {

                }
            }
        });
    }

    @Override
    protected  void onStop() {
        super.onStop();
        Log.d(TAG, "Emergency activity stopped");

        // stop emergency status service
        stopService(new Intent(this, EmergencyStatusService.class));
    }

    // This method will register a reciever that will recieve intent from EmergencyStatusService
    private void registerBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mReceieved = intent.getBooleanExtra(EMERGENCY_STATUS, false);
                updateStatus();
            }
        };
        IntentFilter filter = new IntentFilter(EMERGENCY_STATUS_FILTER);
        registerReceiver(broadcastReceiver, filter);
    }

    // Updates status text on screen
    private void updateStatus() {
        if(!mReceieved) {
            mStatusTextview.setText("Pending");
            mStatusTextview.setTextColor(Color.RED);
        }
        else {
            mStatusTextview.setText("Received");
            mStatusTextview.setTextColor(Color.GREEN);
        }
    }

    private void sendExplanation() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://83a7d733.ngrok.io/emergency-explanation";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap();
                MyData.put("emergency_id", mReportId);
                MyData.put("explanation", mExplanation);
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }

    private void updateCallmeCheckbox() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://83a7d733.ngrok.io/emergency-callme-checkbox";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap();
                MyData.put("emergency_id", mReportId);
                MyData.put("callme", mCanCallMe.toString());
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }
}
