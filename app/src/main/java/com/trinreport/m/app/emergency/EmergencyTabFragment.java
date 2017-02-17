package com.trinreport.m.app.emergency;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.trinreport.m.app.GPSTracker;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;
import com.trinreport.m.app.URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * This is fragment for tab with emergency button
 */
public class EmergencyTabFragment extends Fragment {

    // constants
    private static final String TAG = "EmergencyTabFragment";

    // layout references
    private Button mEmergencyButton;

    // other refernces
    private Location mLocation;
    private GPSTracker mGpsTracker;
    private SharedPreferences mSharedPrefs;
    private String mAdminPublicKey;

    /**
     * Factory method to create a new instance of this fragment
     */
    public static EmergencyTabFragment newInstance() {
        EmergencyTabFragment fragment = new EmergencyTabFragment();
        return fragment;
    }

    public EmergencyTabFragment() {
        // empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_emergency_tab, container, false);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mAdminPublicKey = mSharedPrefs.getString("admin_public_key", "");
        mEmergencyButton = (Button) v.findViewById(R.id.button_emergency);

        // initialize gps tracker
        mGpsTracker = new GPSTracker(getActivity());
        if(!mGpsTracker.canGetLocation()) {
            mGpsTracker.showSettingsAlert();
        }

        // add emergency button event lisetner
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

    private void startEmergencyActivity(String report_id) {
        Intent i = new Intent(getActivity(), Emergency.class);
        i.putExtra(Emergency.EXTRA_REPORT_ID, report_id);
        startActivity(i);
    }

    private String encrypt(String plain) throws Exception {
        return RSA.encrypt(plain, mAdminPublicKey);
    }

    private void notifyEmergency() {
        // get url
        String url = URL.SEND_EMERGENCY_REQUEST;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity());

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get user id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    String report_id = jsonObj.get("emergency_id").toString();

                    // open EmergencyActivity
                    startEmergencyActivity(report_id);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "VolleyError: " + error.getMessage());
                Toast.makeText(getActivity(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                // get user data from shared preferences
                String name = mSharedPrefs.getString("username", "");
                String phone = mSharedPrefs.getString("userphone", "");
                String userid = mSharedPrefs.getString("userid", "");
                String longitude = String.valueOf(mLocation.getLongitude());
                String latitude = String.valueOf(mLocation.getLatitude());
                String explanation = "N/A"; // to be added later


                // encrypt data
                try {
                    name = encrypt(name);
                    phone = encrypt(phone);
                    userid = encrypt(userid);
                    //longitude = encrypt(longitude);
                    //latitude = encrypt(latitude);
                    explanation = encrypt(explanation);

                } catch (Exception e) {
                    Log.d(TAG, "Encryption error: " + e.getMessage());
                }

                // add data to hashmap
                MyData.put("username", name);
                MyData.put("userphone", phone);
                MyData.put("userid", userid);
                MyData.put("longitude", longitude);
                MyData.put("latitude", latitude);
                MyData.put("explanation", explanation);

                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }
}
