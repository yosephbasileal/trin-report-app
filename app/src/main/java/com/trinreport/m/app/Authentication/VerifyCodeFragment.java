package com.trinreport.m.app.Authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.MainActivity;
import com.trinreport.m.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * VerifyCodeFragment
 */
public class VerifyCodeFragment extends Fragment {

    private static final String TAG = "VerifyCodeFragment";

    private EditText verifCodeText;

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VerifyCodeFragment.
     */
    public static VerifyCodeFragment newInstance() {
        VerifyCodeFragment fragment = new VerifyCodeFragment();
        return fragment;
    }

    public VerifyCodeFragment() {
        // empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_verify_code, container, false);

        Button verifyTokenButton = (Button) v.findViewById(R.id.verify_code_button);
        verifCodeText = (EditText) v.findViewById(R.id.verification_code);

        verifyTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verif_code = verifCodeText.getText().toString();

                // send post request to authentication server
                String key = "auth_user_id";
                SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String auth_u_id = sf.getString(key, "");
                String url = "http://trinreport.appspot.com/verify";
                Map<String, String> data = new HashMap();
                data.put("code", verif_code);
                data.put(key, auth_u_id);
                verifyCode(url, data);
            }
        });
        return v;
    }

    private void verifyCode(final String url, final Map<String,String> data) {
        RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Response: " + response);
                try {
                    // get auth token assigned by authentication server
                    String key = "auth_token";
                    JSONObject jsonObj = new JSONObject(response);
                    String auth_token = jsonObj.get(key).toString();
                    Log.d(TAG, "Auth token: " + auth_token);
                    // save user id to shared prefs
                    SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sf.edit();
                    editor.putString(key, auth_token);
                    // update user state to authenticated
                    editor.putBoolean("authenticated", true);
                    editor.commit();
                    // open MainActivity
                    Intent i = new Intent(getActivity(), MainActivity.class);
                    startActivity(i);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                    verifCodeText.setError("Authentication failed! Enter a valid code.");
                }

            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                return data;
            }
        };

        requestQueue.add(stringRequest);
    }
}
