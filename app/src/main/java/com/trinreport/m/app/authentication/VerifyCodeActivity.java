package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.MainActivity;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;
import com.trinreport.m.app.URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

/**
 * In this page, user enters the verif code they they
 * recieved through email
 */
public class VerifyCodeActivity extends AppCompatActivity {

    // constants
    private static final String TAG = "VerifyCodeActivity";

    // layout references
    private EditText mVerifCodeText;
    private Button mVerifyTokenButton;

    // other preferences
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate layout
        setContentView(R.layout.activity_verify_code);

        // get references
        mSharedPref =  PreferenceManager.getDefaultSharedPreferences(this);
        mVerifyTokenButton = (Button) findViewById(R.id.verify_code_button);
        mVerifCodeText = (EditText) findViewById(R.id.verification_code);

        // add listener to verify token button
        mVerifyTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send code to authentication server for verification
                String verif_code = mVerifCodeText.getText().toString();
                String auth_user_id = mSharedPref.getString("auth_user_id", "");
                verifyCode(verif_code, auth_user_id);
            }
        });
    }

    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void authenticate(String auth_token) {
        String name = "auth_token";

        try {
            // generate RSA keys
            KeyPair keyPair = RSA.generateRsaKeyPair(2048);
            String privateKeyPem = RSA.createStringFromPrivateKey(keyPair.getPrivate());
            String publicKeyPem = RSA.createStringFromPublicKey(keyPair.getPublic());

            // save auth token to key
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(name, auth_token); // save auth token
            editor.putBoolean("authenticated", true); // authenticate user
            editor.putString("private_key", privateKeyPem); // save private key
            editor.commit();

            // publish public key pem to server
            publishPublicKey(auth_token, publicKeyPem);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
        }
    }

    private void verifyCode(final String verif_code, final String auth_user_id) {
        // get url
        String url = URL.VERIFY_AUTH_CODE;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get auth token assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    String auth_token = jsonObj.get("auth_token").toString();

                    // save authentication info in shared prefs, also generates RSA keys
                    authenticate(auth_token);

                    // open main activity of the app
                    startMainActivity();
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> data = new HashMap<>();
                data.put("code", verif_code);
                data.put("auth_user_id", auth_user_id);
                return data;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }

    private void publishPublicKey(final String auth_token, final String publicKey) {
        // get url
        String url = URL.PUBLISH_PUBLIC_KEY;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get public key of admin
                    String key = "public_key";
                    JSONObject jsonObj = new JSONObject(response);
                    String publicKeyPem = jsonObj.get(key).toString();

                    // save admin public key shared prefs
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putString("admin_public_key", publicKeyPem);
                    Log.d(TAG, "Key pem: " + publicKeyPem);
                    editor.apply();

                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap();
                MyData.put("public_key", publicKey);
                MyData.put("auth_token", auth_token);
                return MyData;
            }
        };

        // add to queue
        requestQueue.add(MyStringRequest);
    }
}
