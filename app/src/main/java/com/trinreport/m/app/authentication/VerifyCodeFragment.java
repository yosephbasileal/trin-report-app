package com.trinreport.m.app.authentication;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nimbusds.jose.jwk.RSAKey;
import com.trinreport.m.app.MainActivity;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import com.nimbusds.jose.jwk.RSAKey;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * VerifyCodeFragment
 */
public class VerifyCodeFragment extends Fragment {

    private static final String TAG = "VerifyCodeFragment";

    private EditText verifCodeText;

    SharedPreferences mSharedPref;

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

        mSharedPref =  PreferenceManager.getDefaultSharedPreferences(getActivity());

        Button verifyTokenButton = (Button) v.findViewById(R.id.verify_code_button);
        verifCodeText = (EditText) v.findViewById(R.id.verification_code);

        verifyTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verif_code = verifCodeText.getText().toString();

                // send post request to authentication server
                String key = "auth_user_id";
                String auth_user_id = mSharedPref.getString(key, "");
                verifyCode(verif_code, auth_user_id);
            }
        });
        return v;
    }

    private void verifyCode(final String verif_code, final String auth_user_id) {
        String url = "http://trinreport.appspot.com/verify";
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

                    // save auth token to shared prefs
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putString(key, auth_token);

                    // update user state to authenticated
                    editor.putBoolean("authenticated", true);

                    // generate RSA keys
                    KeyPair keyPair = RSA.generateRSAKeys();
                    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

                    // save private key and public key in shared prefs
                    RSAKey keyPairJwk = new RSAKey.Builder(publicKey)
                            .privateKey(privateKey)
                            .build();
                    String keyPairJsonString = keyPairJwk.toString();
                    editor.putString("key_pair", keyPairJsonString);

                    // publish public key to server
                    RSAKey publicKeyJwk = new RSAKey.Builder(publicKey)
                            .build();
                    publishRSAPublicKey(auth_token, publicKeyJwk);

                    // save shared prefs
                    editor.commit();

                    // open MainActivity
                    Intent i = new Intent(getActivity(), MainActivity.class);
                    startActivity(i);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                    verifCodeText.setError("Authentication failed! Enter a valid code.");
                } catch (NoSuchAlgorithmException e) {

                }

            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> data = new HashMap();
                data.put("code", verif_code);
                data.put("auth_user_id", auth_user_id);

                return data;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void publishRSAPublicKey(final String auth_token, final RSAKey publicKey) {
        String url = "http://56e83ff5.ngrok.io/api/app/publish-user-public-key";
        HashMap<String, RSAKey> myData = new HashMap<String, RSAKey>();
        myData.put(auth_token, publicKey);

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(myData), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(req);
    }
}
