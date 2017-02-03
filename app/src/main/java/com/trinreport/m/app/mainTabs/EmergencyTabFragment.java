package com.trinreport.m.app.mainTabs;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.Emergency;
import com.trinreport.m.app.R;
import com.trinreport.m.app.authentication.VerifyCodeActivity;


import org.json.JSONException;
import org.json.JSONObject;
import org.silvertunnel_ng.netlib.adapter.java.JvmGlobalUtil;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePortPrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtil;
import org.silvertunnel_ng.netlib.util.ByteArrayUtil;
import org.silvertunnel_ng.netlib.util.HttpUtil;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
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
    private LocationManager mLocationManager;

    private Handler mHandler;
    private Runnable mLongPressed;

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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_emergency_tab, container, false);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Add emergency button event listner
        mEmergencyButton = (Button) v.findViewById(R.id.button_emergency);

        mEmergencyButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    //get current location
                    //mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, looper);
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    //send request to RDDP
                    notifyEmergency();

                    Intent i = new Intent(getActivity(), Emergency.class);
                    i.putExtra(Emergency.EXTRA_REPORT_ID, 1);
                    startActivity(i);

                } catch (SecurityException e) {
                    throw e;
                }

                return false;
            }
        });

/*        mHandler = new Handler();
        mLongPressed = new Runnable() {
            public void run() {
                Log.d("TAG", "long press emergencybuttun\n\n\n\n\n\n\n!");
            }
        };
        mEmergencyButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mHandler.postDelayed(mLongPressed, 3000);
                    //v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                    //v.invalidate();
                }if((event.getAction() == MotionEvent.ACTION_MOVE)||(event.getAction() == MotionEvent.ACTION_UP))
                    mHandler.removeCallbacks(mLongPressed);
                    //v.getBackground().clearColorFilter();
                    //v.invalidate();
                return true;
            }
        });*/

        return v;
    }

    private void notifyEmergency() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this.getActivity());
        String url = "http://83a7d733.ngrok.io/emergency-request";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d(TAG, "VolleySucess");
                    // get user id assigned by authentication server
                    String key = "emergency_id";
                    JSONObject jsonObj = new JSONObject(response);
                    String report_id = jsonObj.get(key).toString();
                    Log.d(TAG, "Report ID: " + report_id);
                    // open MergencyActivity
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
                if(mLocation != null) {
                    MyData.put("longitude", String.valueOf(mLocation.getLongitude()) ); //Add the data you'd like to send to the server.
                    MyData.put("latitude", String.valueOf(mLocation.getLatitude()) ); //Add the data you'd like to send to the server.
                }

                else {
                    MyData.put("longitude", String.valueOf(41.744513) ); //Add the data you'd like to send to the server.
                    MyData.put("latitude", String.valueOf(-72.691198) );
                }

                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }
}
