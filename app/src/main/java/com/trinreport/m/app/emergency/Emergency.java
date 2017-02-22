package com.trinreport.m.app.emergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;
import com.trinreport.m.app.URL;

import java.util.HashMap;
import java.util.Map;

/**
 * This activity is opened when use presses the emergency button
 */
public class Emergency extends AppCompatActivity {

    // constants
    private static final String TAG = "EmergencyActivity";
    public static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORT_ID";
    public static final String EMERGENCY_STATUS = "com.trinreport.m.app.extra.STATUS";
    public static final String EMERGENCY_STATUS_FILTER = "com.trinreport.m.app.action.new_status";
    private static final String CS_PHONE_NUMBER = "8602972222";

    // layour references
    private Toolbar mToolbar;
    private TextView mStatusTextview;
    private LinearLayout mExplanationLayout;
    private Button mExplanationButton;
    private EditText mExplanationEditText;
    private TextView mExplanationSentTextView;
    private CheckBox mCallmeCheckbox;
    private Button mCallCsSafetyButton;

    // other references
    BroadcastReceiver broadcastReceiver;
    private String mAdminPublicKey;
    private SharedPreferences mSharedPrefs;

    // variables
    private String mExplanation;
    private String mReportId;
    private Boolean mReceieved = false;
    private Boolean mCanCallMe = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate layout
        setContentView(R.layout.activity_emergency);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAdminPublicKey = mSharedPrefs.getString("admin_public_key", "");

        // get references
        mToolbar = (Toolbar) findViewById(R.id.toolbar_emergency);
        mStatusTextview = (TextView) findViewById(R.id.statusTextView);
        mExplanationLayout = (LinearLayout) findViewById(R.id.explanation_layout);
        mExplanationSentTextView = (TextView) findViewById(R.id.explanation_sent_textview);
        mExplanationEditText = (EditText) findViewById(R.id.explanation);
        mExplanationButton = (Button) findViewById(R.id.send_explanation);
        mCallmeCheckbox = (CheckBox) findViewById(R.id.callme_checkbox);
        mCallCsSafetyButton = (Button) findViewById(R.id.call_csafety_button);

        // setup toolbar
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }

        // get report id from intent
        mReportId = getIntent().getStringExtra(EXTRA_REPORT_ID);

        // initialize status to not recieved
        updateStatus();

        // start background service for requesting updated status drom rddp and sending gps points
        registerBroadcastReceiver();
        EmergencyStatusService.startActionUpdateStatus(this, mReportId);

        // add listener to text field for adding explanation about emergency situation
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

        // button click sends explanation about the emergency to rddp server
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

        // checkbox to indicate if campus safety can call
        mCallmeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCanCallMe = isChecked;

                // send update to server
                updateCallmeCheckbox();
            }
        });

        // button for calling campus safety office
        mCallCsSafetyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + CS_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {

                }
            }
        });
    }

    @Override
    protected  void onResume() {
        super.onResume();
        Log.d(TAG, "Emergency activity resumed");
        registerBroadcastReceiver();
    }

    @Override
    protected  void onStop() {
        Log.d(TAG, "Emergency activity stopped");
        // stop emergency status service
        unregisterReceiver(broadcastReceiver);
        stopService(new Intent(this, EmergencyStatusService.class));
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Emergency activity destroyed");
        try {
            if(broadcastReceiver != null)
                unregisterReceiver(broadcastReceiver);
        } catch(Exception e) {

        }
        super.onDestroy();
    }

    /**
     * This method will register a reciever that will recieve intent from EmergencyStatusService
      */
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

    /**
     * Updates emergency status on the screen
     */
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

    private String encrypt(String plain) throws Exception {
        return RSA.encrypt(plain, mAdminPublicKey);
    }

    private void sendExplanation() {
        // get url
        String url = URL.SEND_EMERGENCY_EXPLANATION;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                String explanation = mExplanation;

                // encrypt data
                try {
                    explanation = encrypt(explanation);

                } catch (Exception e) {
                    Log.d(TAG, "Encryption error: " + e.getMessage());
                }


                MyData.put("emergency_id", mReportId);
                MyData.put("explanation", explanation);
                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }

    private void updateCallmeCheckbox() {
        // get url
        String url = URL.EMERGENCY_CALLME_CHECKBOX;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("emergency_id", mReportId);
                MyData.put("callme", mCanCallMe.toString());
                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }
}
