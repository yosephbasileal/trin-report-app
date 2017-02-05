package com.trinreport.m.app.authentication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.trinreport.m.app.R;
import com.trinreport.m.app.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * GetTokenFragment
 */
public class RequestTokenFragment extends Fragment {

    private static final String TAG = "GetTokenFragment";

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GetTokenFragment.
     */
    public static RequestTokenFragment newInstance() {
        RequestTokenFragment fragment = new RequestTokenFragment();
        return fragment;
    }

    public RequestTokenFragment() {
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
        View v = inflater.inflate(R.layout.fragment_request_token, container, false);

        Button getTokenButton = (Button) v.findViewById(R.id.get_token_button);
        final EditText emailEditText = (EditText) v.findViewById(R.id.email_address);

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // empty
            }

            @Override
            public void afterTextChanged(Editable s) {
                emailEditText.setError(null);
            }
        });
        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();

                if (!Utilities.validate_email(email, "trincoll.edu")) {
                    emailEditText.setError("Invalid emaill address! Try again.");
                    return;
                }

                // send post request to authentication server
                String url = "http://trinreport.appspot.com/request";
                Map<String, String> data = new HashMap();
                data.put("email", email);
                requestAuthToken(url, data);
            }
        });
        return v;
    }

    private void requestAuthToken(final String url, final Map<String,String> data) {
        RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Response: " + response);
                try {
                    // get user id assigned by authentication server
                    String key = "auth_user_id";
                    JSONObject jsonObj = new JSONObject(response);
                    String auth_user_id = jsonObj.get(key).toString();
                    Log.d(TAG, "User ID: " + auth_user_id);

                    // save user id to shared prefs
                    SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sf.edit();
                    editor.putString(key, auth_user_id);
                    editor.commit();

                    // open VerifyCodeActivity
                    Intent i = new Intent(getActivity(), VerifyCodeActivity.class);
                    startActivity(i);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
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
