package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String USER_ID_KEY = "auth_user_id";

    // layout references
    private Button mGetTokenButton;
    private EditText mEmailEditText;
    private Toolbar mToolbar;
    private TextView mErrorText;
    private ProgressBar mLoadingMarker;

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
        mToolbar = (Toolbar) findViewById(R.id.toolbar_request);
        mErrorText = (TextView) findViewById(R.id.error_request);
        mLoadingMarker = (ProgressBar) findViewById(R.id.marker_progress_request);

        // setup toolbar back button
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                onBackPressed();
            }
        });

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
                if (!Utilities.validateEmail(email)) {
                    showError("Invalid emaill address! Try again.");
                    return;
                }

                // send post request to authentication server
                requestAuthToken(email);
            }
        });
    }

    /**
     * Saves ID assigned by authentication server in shared prefernces
     * @param authID authentication ID assigned by auth server
     */
    private void saveUserID(String authID) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(USER_ID_KEY, authID);
        editor.apply();
    }

    /**
     * Shows error under edit text view
     * @param error error message to show
     */
    private void showError(String error) {
        mErrorText.setText(error);
    }

    /**
     * Wrapper for starting VerifyCodeActivity
     */
    private void startVerifyCodeActivity() {
        Intent i = new Intent(this, VerifyCodeActivity.class);
        startActivity(i);
    }

    private void showLoadingMarker() {
        mGetTokenButton.setEnabled(false);
        mLoadingMarker.setVisibility(View.VISIBLE);
    }

    private void hideLoadingMarker() {
        mGetTokenButton.setEnabled(true);
        mLoadingMarker.setVisibility(View.INVISIBLE);
    }

    /**
     * Sends a request to auth server to validate email and request auth token
     * @param email email address to validate
     */
    private void requestAuthToken(final String email) {
        // get url
        String url = URL.REQUEST_AUTH_TOKEN;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get user id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    String auth_user_id = jsonObj.get(USER_ID_KEY).toString();

                    // save user auth id to shared prefs
                    saveUserID(auth_user_id);

                    // open VerifyCodeActivity
                    hideLoadingMarker();
                    startVerifyCodeActivity();

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                    hideLoadingMarker();
                    showError("Something went wrong! Try again.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
                hideLoadingMarker();
                showError("Something went wrong! Try again.");
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
        showLoadingMarker();
    }
}
