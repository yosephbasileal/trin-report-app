package com.trinreport.m.app;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
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
    private static final String EXTRA_LONGTIUDE = "com.trinreport.m.app.extra.LONGTIUDE";
    private static final String EXTRA_LATITUDE = "com.trinreport.m.app.extra.LATITUDE";

    public EmergencyStatusService() {
        super("EmergencyStatusService");
    }

    /**
     * Starts this service to request updated status from RDDP. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateStatus(Context context, String report_id, double lngt, double lat) {
        Intent intent = new Intent(context, EmergencyStatusService.class);
        intent.setAction(ACTION_STATUS);
        intent.putExtra(EXTRA_REPORT_ID, report_id);
        intent.putExtra(EXTRA_LONGTIUDE, lngt);
        intent.putExtra(EXTRA_LATITUDE, lat);
        context.startService(intent);
        Log.d(TAG, "Status service started");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_STATUS.equals(action)) {
                final String report_id = intent.getStringExtra(EXTRA_REPORT_ID);
                final double lngt = intent.getDoubleExtra(EXTRA_LONGTIUDE, 0);
                final double lat = intent.getDoubleExtra(EXTRA_LATITUDE, 0);

                // run request every five seconds
                /*new Handler().postDelayed(new Runnable() {
                    public void run() {
                        getEmergencyStatus(report_id, lngt, lat);
                    }
                }, 10000);*/

                int delay = 0; // delay for 0 sec.
                int period = 5000; // repeat every 10 sec.
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask()
                {
                    public void run()
                    {
                        getEmergencyStatus(report_id, lngt, lat);
                    }
                }, delay, period);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void getEmergencyStatus(final String reportId, final double lngt, final double lat) {
        Log.d(TAG, "getEmergencyStatus called");
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://83a7d733.ngrok.io/check-emergency-status";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // get status
                    String key = "status";
                    JSONObject jsonObj = new JSONObject(response);
                    String status = jsonObj.get(key).toString();
                    Log.d(TAG, "Status: " + status);
                    announceStatusChange(status);
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
                MyData.put("emergency_id", reportId);
                MyData.put("longitude", String.valueOf(lngt));
                MyData.put("latitude", String.valueOf(lat));
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }

    private void announceStatusChange(String status)//this method sends broadcast messages
    {
        Intent intent = new Intent(Emergency.EMERGENCY_STATUS_FILTER);
        intent.putExtra(Emergency.EMERGENCY_STATUS, status);

        sendBroadcast(intent);
    }
}
