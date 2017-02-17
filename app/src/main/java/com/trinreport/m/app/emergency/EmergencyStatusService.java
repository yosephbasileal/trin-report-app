package com.trinreport.m.app.emergency;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.ApplicationContext;
import com.trinreport.m.app.GPSTracker;
import com.trinreport.m.app.RSA;
import com.trinreport.m.app.URL;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Service for handling asynchronous requests to rddp server
 * to request updated status and send updated gps points
 */
public class EmergencyStatusService extends IntentService {

    // constants
    private static final String TAG = "EmergencyStatusService";
    private static final String ACTION_STATUS = "com.trinreport.m.app.action.check_status";
    private static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORTID";

    // variables
    private GPSTracker mGpsTracker;
    private Location mLocation;
    private String mReportId;
    private String mAdminPublicKey;
    private SharedPreferences mSharedPrefs;


    /**
     * Factory method to start this service
     */
    public static void startActionUpdateStatus(Context context, String report_id) {
        Intent intent = new Intent(context, EmergencyStatusService.class);
        intent.setAction(ACTION_STATUS);
        intent.putExtra(EXTRA_REPORT_ID, report_id);
        context.startService(intent);
        Log.d(TAG, "Status service started");
    }

    public EmergencyStatusService() {
        super("EmergencyStatusService");
        mGpsTracker = new GPSTracker(ApplicationContext.get());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_STATUS.equals(action)) {
                mReportId = intent.getStringExtra(EXTRA_REPORT_ID);

                mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                mAdminPublicKey = mSharedPrefs.getString("admin_public_key", "");

                // run request every five seconds
                HandlerThread handlerThread = new HandlerThread("HandlerThread");
                handlerThread.start();
                final Handler mHandler = new Handler(handlerThread.getLooper());
                mHandler.post(new Runnable() {
                    public void run() {
                        mLocation = mGpsTracker.getLocation();
                        getEmergencyStatus();
                        mHandler.postDelayed(this, 5000);
                    }
                });
            }
        }
    }

    /**
     * This method broadcasts updated status
     */
    private void announceStatusChange(Boolean receieved) {
        Intent intent = new Intent(Emergency.EMERGENCY_STATUS_FILTER);
        intent.putExtra(Emergency.EMERGENCY_STATUS, receieved);

        sendBroadcast(intent);
    }

    private String encrypt(String plain) throws Exception {
        return RSA.encrypt(plain, mAdminPublicKey);
    }


    private void getEmergencyStatus() {
        // get url
        String url = URL.CHECK_EMERGENCY_STATUS;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get status and send broadcast intent
                    JSONObject jsonObj = new JSONObject(response);
                    Boolean received = (Boolean) jsonObj.get("handled_status");
                    announceStatusChange(received);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "VolleyError: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap();

                String longitude = String.valueOf(mLocation.getLongitude());
                String latitude = String.valueOf(mLocation.getLatitude());

                // encrypt data
                try {
                    //longitude = encrypt(longitude);
                    //latitude = encrypt(latitude);

                } catch (Exception e) {
                    Log.d(TAG, "Encryption error: " + e.getMessage());
                }

                // add data to hashmap
                MyData.put("longitude", longitude);
                MyData.put("latitude", latitude);
                MyData.put("emergency_id", mReportId);
                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }
}
