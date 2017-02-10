package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nimbusds.jose.jwk.RSAKey;
import com.trinreport.m.app.MainActivity;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;

import org.json.JSONObject;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

public class StartPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is authenticated
        SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(this);
        boolean authenticated = sf.getBoolean("authenticated", false);

        // test
        /*setContentView(R.layout.activity_start_page);
        try {
            KeyPair keyPair = RSA.generateRSAKeys();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            RSAKey publicKeyJwk = new RSAKey.Builder(publicKey).build();
            publishRSAPublicKey(publicKeyJwk);

        } catch (NoSuchAlgorithmException e) {

        }*/

        // TODO: Next two lines for debuggine purpose only
        /*Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
*/
        // Enable this for authentication
        if(authenticated) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else {
            setContentView(R.layout.activity_start_page);

            // Create a fragment and add to view
            FragmentManager fm = getSupportFragmentManager();
            Fragment frag = new StartPageFragment();
            fm.beginTransaction().add(R.id.start_page_activity, frag).commit();
        }
    }

///*    private void publishRSAPublicKey(final RSAKey publicKey) {
//        String url = "http://56e83ff5.ngrok.io/rsa-test";
//        HashMap<String, RSAKey> myData = new HashMap<String, RSAKey>();
//        myData.put("public_key", publicKey);
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(myData), new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        });
//
//        requestQueue.add(req);
//    }*/
}
