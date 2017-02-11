package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.R;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This page is where a user can request an authentication
 * token by providing their school provided email address
 */
public class RequestTokenActivity extends AppCompatActivity {

    // constants
    private static final String TAG = "RequestTokenActivity";

    // layout references
    private Button mGetTokenButton;
    private EditText mEmailEditText;

    // other references
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate layout
        setContentView(R.layout.activity_request_token);

        // get references
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mGetTokenButton = (Button) findViewById(R.id.get_token_button);
        mEmailEditText = (EditText) findViewById(R.id.email_address);

        // add listener for email text field
        mEmailEditText.addTextChangedListener(new TextWatcher() {
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
                mEmailEditText.setError(null);
            }
        });

        // add listener for submit button
        mGetTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEditText.getText().toString();

                // check if a valid email address
                if (!Utilities.validate_email(email, "trincoll.edu")) {
                    mEmailEditText.setError("Invalid emaill address! Try again.");
                    return;
                }

                // send post request to authentication server
                requestAuthToken(email);
            }
        });
    }

    private void saveUserId(String auth_user_id) {
        String key = "auth_user_id";
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(key, auth_user_id);
        editor.apply();
    }

    private void startVerifyCodeActivity() {
        Intent i = new Intent(this, VerifyCodeActivity.class);
        startActivity(i);
    }

    private void requestAuthToken(final String email) {
        // get url
        String url = URL.REQUEST_AUTH_TOKEN;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get user id assigned by authentication server
                    String key = "auth_user_id";
                    JSONObject jsonObj = new JSONObject(response);
                    String auth_user_id = jsonObj.get(key).toString();

                    // save user auth id to shared prefs
                    saveUserId(auth_user_id);

                    // open VerifyCodeActivity
                    startVerifyCodeActivity();

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> data = new HashMap<>();
                data.put("email", email);
                return data;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }
}
