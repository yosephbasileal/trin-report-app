package com.trinreport.m.app.report;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.trinreport.m.app.R;

import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;
import com.msopentech.thali.toronionproxy.OnionProxyManager;
import com.trinreport.m.app.tor.MyConnectionSocketFactory;
import com.trinreport.m.app.tor.MySSLConnectionSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.ssl.SSLContexts;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportTabFragment extends Fragment {

    private static final String CS_PHONE_NUMBER = "8602972222";
    private static final String T9_PHONE_NUMBER = "8602972222";
    private static final String HC_PHONE_NUMBER = "8602972222";
    private static final String CC_PHONE_NUMBER = "8602972222";

    private Button mAddReportButton;
    private Button mCallCsSafetyButton;
    private Button mCallCounselingCentButton;
    private Button mCallTitleNineButton;
    private Button mCallHealthCenterButton;
    private Toolbar mToolbar;

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReportTabFragment.
     */
    public static ReportTabFragment newInstance() {
        ReportTabFragment fragment = new ReportTabFragment();
        return fragment;
    }

    public ReportTabFragment() {
        // empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_report_tab, container, false);

        mAddReportButton = (Button) v.findViewById(R.id.add_report_button);
        mCallCsSafetyButton = (Button) v.findViewById(R.id.call_campus_safety);
        mCallCounselingCentButton = (Button) v.findViewById(R.id.call_counselling_center);
        mCallTitleNineButton = (Button) v.findViewById(R.id.call_title_9);
        mCallHealthCenterButton = (Button) v.findViewById(R.id.call_health_center);

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        }

        mAddReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddReportActivity.class);
                startActivity(i);
            }
        });

        //BackgroundJob job = new BackgroundJob();
        //job.execute("abc");

        // button for calling campus safety office
        mCallCsSafetyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + CS_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {

                }
            }
        });

        // button for calling counseling center
        mCallCounselingCentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + CC_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {

                }
            }
        });

        // button for calling title IX office
        mCallTitleNineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + T9_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {

                }
            }
        });

        // button for calling health center
        mCallHealthCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + HC_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {

                }
            }
        });

        return v;
    }

    public HttpClient getNewHttpClient() {

        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }


    private class BackgroundJob extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {

            OnionProxyManager onionProxyManager = new AndroidOnionProxyManager(getActivity(), "torr");

            int totalSecondsPerTorStartup = 4 * 60;
            int totalTriesPerTorStartup = 5;
            try {
                boolean ok = onionProxyManager.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup);
                if (!ok)
                    Log.e("TorTest", "Couldn't start Tor!");

                while (!onionProxyManager.isRunning())
                    Thread.sleep(90);

                Log.v("TorTest", "Tor initialized on port " + onionProxyManager.getIPv4LocalHostSocksPort());

                HttpClient httpclient = getNewHttpClient();
                int port = onionProxyManager.getIPv4LocalHostSocksPort();
                InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", port);
                HttpClientContext context = HttpClientContext.create();
                context.setAttribute("socks.address", socksaddr);

                // Prepare a request object
                //HttpGet httpget = new HttpGet("https://check.torproject.org/");
                HttpGet httpget = new HttpGet("https://trinreport.com/ip");
                // Execute the request
                HttpResponse response;
                response = httpclient.execute(httpget, context);
                // Examine the response status
                Log.i("TorTest", response.getStatusLine().toString());
                // Get hold of the response entity
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release
                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);
                    Log.d("TorTest", "Result: " + result);
                    // now you have the string representation of the HTML request
                    instream.close();
                }

                // try post request
                //url with the post data
                HttpPost httpost = new HttpPost("https://trinreport.com/tor-test");

                //convert parameters into JSON object
                Map<String, String> data = new HashMap();
                data.put("urgency", "Not urgent" );

                JSONObject holder = new JSONObject(data);

                //passes the results to a string builder/entity
                StringEntity se = new StringEntity(holder.toString());

                //sets the post request as the resulting string
                httpost.setEntity(se);
                //sets a request header so the page receving the request
                //will know what to do with it
                httpost.setHeader("Accept", "application/json");
                httpost.setHeader("Content-type", "application/json");

                //Handles what is returned from the page
                response = httpclient.execute(httpost, context);
                // Examine the response status
                Log.i("TorTest", response.getStatusLine().toString());
                // Get hold of the response entity
                entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release
                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);
                    Log.d("TorTest", "Result: " + result);
                    // now you have the string representation of the HTML request
                    instream.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }


            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    // sourec: http://stackoverflow.com/questions/4457492/how-do-i-use-the-simple-http-client-in-android
    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
