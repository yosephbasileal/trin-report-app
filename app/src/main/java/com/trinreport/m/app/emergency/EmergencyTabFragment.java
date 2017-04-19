package com.trinreport.m.app.emergency;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.maps.android.PolyUtil;
import com.trinreport.m.app.ApplicationContext;
import com.trinreport.m.app.GPSTracker;
import com.trinreport.m.app.R;
import com.trinreport.m.app.URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.maps.model.LatLng;


/**
 * This is fragment for tab with emergency button
 */
public class EmergencyTabFragment extends Fragment {

    // constants
    private static final String TAG = "EmergencyTabFragment";
    private static int VOLLEY_TIMEOUT_MS = 5000; // 5 seconds
    private static int VOLLEY_RETRY = 10; // 5 times

    // layout references
    private Button mEmergencyButton;
    private Toolbar mToolbar;

    // other references
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_emergency_tab, container, false);

        // get references
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mEmergencyButton = (Button) v.findViewById(R.id.button_emergency);
        mAdminPublicKey = mSharedPrefs.getString("admin_public_key", "");
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        mGpsTracker = new GPSTracker(getActivity());

        // setup toolbar
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        }

        // initialize gps tracker
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

                    // validate location
                    if(!validate_location()) {
                        Toast.makeText(getActivity(), "Call 911. Your are too far from campus.",
                        Toast.LENGTH_LONG).show();
                        return false;
                    }

                    //send request to RDDP
                    notifyEmergency();

                } catch (SecurityException e) {
                    throw e;
                }

                return true;
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // hide toolbar on this page
        mToolbar.setVisibility(View.GONE);
    }

    /**
     * Wrapper for starting EmergencyActivity when button is pressed
     * @param report_id id assigned to current report by rddp server
     */
    private void startEmergencyActivity(String report_id) {
        Intent i = new Intent(getActivity(), Emergency.class);
        i.putExtra(Emergency.EXTRA_REPORT_ID, report_id);
        startActivity(i);
    }

    /**
     * Validate GPS coordinates
     */
    private boolean validate_location() {
        List<LatLng> poly = new ArrayList<>();
        poly.add(new LatLng(41.756097, -72.697784)); // middle of Pope Park
        poly.add(new LatLng(41.739487, -72.697258)); // New Britain and Hillside ave
        poly.add(new LatLng(41.743114, -72.682715)); // Maple ave and Webster st
        poly.add(new LatLng(41.756292, -72.682463)); // Washington st and Ward st
        boolean inside = PolyUtil.containsLocation(
                new LatLng(41.757595, -72.699073), poly, true
        );
        return inside;
    }

    /**
     * Encryption helper method
     */
    private String enc(String plain) throws Exception{
        return ApplicationContext.getInstance().encryptForAdmin(plain,
                mAdminPublicKey);
    }

    /**
     * Sends emergency request to rddp server, send user data and gps location
     */
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
                    // get emergency id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    String emergency_id = jsonObj.get("emergency_id").toString();

                    // open EmergencyActivity
                    startEmergencyActivity(emergency_id);
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
                String auth_token = mSharedPrefs.getString("auth_token", "n/a");
                String name = mSharedPrefs.getString("username", "n/a");
                String phone = mSharedPrefs.getString("userphone", "n/a");
                String userid = mSharedPrefs.getString("userid", "n/a");
                String email = mSharedPrefs.getString("useremail", "n/a");
                String dorm = mSharedPrefs.getString("userdorm", "n/a");
                String longitude = String.valueOf(mLocation.getLongitude());
                String latitude = String.valueOf(mLocation.getLatitude());
                String explanation = "N/A"; // to be sent later from Emergency activity

                // encrypt data
                try {
                    name = enc(name);
                    phone = enc(phone);
                    userid = enc(userid);
                    email = enc(email);
                    dorm = enc(dorm);
                    longitude = enc(longitude);
                    latitude = enc(latitude);
                    explanation = enc(explanation);
                } catch (Exception e) {
                    Log.d(TAG, "Encryption error: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }

                // add data to hashmap
                MyData.put("authtoken", auth_token);
                MyData.put("username", name);
                MyData.put("userphone", phone);
                MyData.put("userid", userid);
                MyData.put("useremail", email);
                MyData.put("userdorm", dorm);
                MyData.put("longitude", longitude);
                MyData.put("latitude", latitude);
                MyData.put("explanation", explanation);

                return MyData;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT_MS,
                VOLLEY_RETRY,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // add to queue
        requestQueue.add(stringRequest);
    }
}
