package com.trinreport.m.app.emergency;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.GPSTracker;
import com.trinreport.m.app.R;


import org.json.JSONException;
import org.json.JSONObject;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmergencyTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmergencyTabFragment extends Fragment {

    private static final String TAG = "EmergencyTabFragment";

    private Location mLocation;

    private Handler mHandler;
    private Runnable mLongPressed;

    private GPSTracker mGpsTracker;

    NetSocket netSocket;
    NetLayer netLayer;
    TcpipNetAddress remoteAddress;

    private Button mEmergencyButton;

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EmergencyTabFragment.
     */
    public static EmergencyTabFragment newInstance() {
        EmergencyTabFragment fragment = new EmergencyTabFragment();
        return fragment;
    }

    public EmergencyTabFragment() {
        // empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_emergency_tab, container, false);

        // initialize gps tracker
        mGpsTracker = new GPSTracker(getActivity());
        if(!mGpsTracker.canGetLocation()) {
            mGpsTracker.showSettingsAlert();
        }

        // Add emergency button event listner
        mEmergencyButton = (Button) v.findViewById(R.id.button_emergency);

        mEmergencyButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    //get current location
                    mLocation = mGpsTracker.getLocation();

                    //send request to RDDP
                    notifyEmergency();

                } catch (SecurityException e) {
                    throw e;
                }

                return false;
            }
        });

        return v;
    }

    private void notifyEmergency() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this.getActivity());
        String url = "http://83a7d733.ngrok.io/emergency-request";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // get user id assigned by authentication server
                    String key = "emergency_id";
                    JSONObject jsonObj = new JSONObject(response);
                    String report_id = jsonObj.get(key).toString();
                    Log.d(TAG, "Emergency report created ID: " + report_id);
                    // open EmergencyActivity
                    Intent i = new Intent(getActivity(), Emergency.class);
                    i.putExtra(Emergency.EXTRA_REPORT_ID, report_id);
                    startActivity(i);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "VolleyError");
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                MyData.put("username", prefs.getString("username", ""));
                MyData.put("userdorm", prefs.getString("userdorm", ""));
                MyData.put("userphone", prefs.getString("userphone", ""));
                MyData.put("userid", prefs.getString("userid", ""));
                MyData.put("useremail", prefs.getString("useremail", ""));
                MyData.put("longitude", String.valueOf(mLocation.getLongitude()));
                MyData.put("latitude", String.valueOf(mLocation.getLatitude()) );

                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }
}
