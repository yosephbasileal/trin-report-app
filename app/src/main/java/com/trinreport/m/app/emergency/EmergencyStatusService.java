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

import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class EmergencyStatusService extends IntentService {
    private static final String TAG = "EmergencyStatusService";

    private static final String ACTION_STATUS = "com.trinreport.m.app.action.check_status";
    private static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORTID";

    private GPSTracker mGpsTracker;
    private Location mLocation;
    private String mReportId;

    public EmergencyStatusService() {
        super("EmergencyStatusService");
        mGpsTracker = new GPSTracker(ApplicationContext.get());
    }


    public static void startActionUpdateStatus(Context context, String report_id) {
        Intent intent = new Intent(context, EmergencyStatusService.class);
        intent.setAction(ACTION_STATUS);
        intent.putExtra(EXTRA_REPORT_ID, report_id);
        context.startService(intent);
        Log.d(TAG, "Status service started");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_STATUS.equals(action)) {
                mReportId = intent.getStringExtra(EXTRA_REPORT_ID);

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


    private void getEmergencyStatus() {
        Log.d(TAG, "getEmergencyStatus called");
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://83a7d733.ngrok.io/check-emergency-status";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // get status
                    String key = "handled_status";
                    JSONObject jsonObj = new JSONObject(response);
                    Boolean received = (Boolean) jsonObj.get(key);
                    Log.d(TAG, "STATUS... Received: " + received);
                    announceStatusChange(received);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }
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
                MyData.put("longitude", String.valueOf(mLocation.getLongitude()));
                MyData.put("latitude", String.valueOf(mLocation.getLatitude()));
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }

    private void announceStatusChange(Boolean receieved)//this method sends broadcast messages
    {
        Intent intent = new Intent(Emergency.EMERGENCY_STATUS_FILTER);
        intent.putExtra(Emergency.EMERGENCY_STATUS, receieved);

        sendBroadcast(intent);
    }
}
