package com.trinreport.m.app.mainTabs;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.R;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmergencyTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmergencyTabFragment extends Fragment {

    private Location mLocation;
    private LocationManager mLocationManager;

    private Handler mHandler;
    private Runnable mLongPressed;

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

        mEmergencyButton = (Button) v.findViewById(R.id.button_emergency);
        mEmergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, looper);
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    notifyEmergency();
                } catch (SecurityException e) {
                    throw e;
                }

                //TODO: start a new activity, show status of reportll

            }
        });

        /*mHandler = new Handler();
        mLongPressed = new Runnable() {
            public void run() {
                Log.d("", "Long press!");
            }
        };
        mEmergencyButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mHandler.postDelayed(mLongPressed, 3000);
                    v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                    v.invalidate();
                }if((event.getAction() == MotionEvent.ACTION_MOVE)||(event.getAction() == MotionEvent.ACTION_UP))
                    mHandler.removeCallbacks(mLongPressed);
                    v.getBackground().clearColorFilter();
                    v.invalidate();
                return true;
            }
        });*/
        return v;
    }

    private void notifyEmergency() {
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this.getActivity());
        String url = "http://138.197.8.83/emergency";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                MyData.put("username", prefs.getString("username", ""));
                MyData.put("userdorm", prefs.getString("userdorm", ""));
                MyData.put("userphone", prefs.getString("userphone", ""));
                MyData.put("userid", prefs.getString("userid", ""));
                MyData.put("useremail", prefs.getString("useremail", ""));
                MyData.put("longitude", String.valueOf(mLocation.getLongitude()) ); //Add the data you'd like to send to the server.
                MyData.put("latitude", String.valueOf(mLocation.getLatitude()) ); //Add the data you'd like to send to the server.
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }
}
