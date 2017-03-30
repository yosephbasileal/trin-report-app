package com.trinreport.m.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;
import com.msopentech.thali.toronionproxy.OnionProxyManager;
import com.trinreport.m.app.model.ChatKey;
import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.tor.MyConnectionSocketFactory;
import com.trinreport.m.app.tor.MySSLConnectionSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.ssl.SSLContexts;

/**
 * Singleton class for getting application context
 */
public class ApplicationContext {

    private Context appContext;
    private String mAdminPublicKey;

    // tor client
    private HttpClient mHttpclient;
    private boolean mTorInitialized;
    private HttpClientContext mHttpContext;

    private ApplicationContext(){

    }

    public void init(Context context){
        // initilize context
        if(appContext == null){
            appContext = context;
        }

        // get reference to admin public key
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        mAdminPublicKey = mSharedPrefs.getString("admin_public_key", "");

        updateThreadsFromServer();
    }

    private Context getContext(){
        return appContext;
    }

    public static Context get(){
        return getInstance().getContext();
    }

    private static ApplicationContext instance;

    public static ApplicationContext getInstance(){
        return instance == null ?
                (instance = new ApplicationContext()):
                instance;
    }

    public String encryptForAdmin(String plain) throws Exception {
        return RSA.encrypt(plain, mAdminPublicKey);
    }

    public void updateThreadsFromServer() {
        new GetThreadList2().execute();
    }

    /**
     * Async task for gets thread list in the background
     */
    public class GetThreadList2 extends
            AsyncTask<Void, String, ArrayList<ChatKey>> {

        @Override
        protected ArrayList<ChatKey> doInBackground(Void... params) {
            ChatBook book = ChatBook.getChatBook(appContext);
            ArrayList<ChatKey> keys = book.getKeys();
            return keys;
        }

        @Override
        protected void onPostExecute(ArrayList<ChatKey> result) {
            super.onPostExecute(result);
            Log.d("TAG", "reports " + result.toString());
            for(int i = 0; i < result.size(); i++) {
                getFollowUpThread(result.get(i).getReportId(), result.get(i).getTitle());
            }
            //updateThreadsList();
        }
    }

    private void getFollowUpThread(final String reportId, final String title) {
        // get url
        String url = URL.GET_FOLLOW_UP_THREAD;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(appContext);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("TAG", "Volley Sucess: " + response);
                try {
                    // get report id assigned by authentication server
                    JSONObject jsonobject = new JSONObject(response);

                    Boolean initiated = jsonobject.getBoolean("initiated");
                    if(!initiated) { // follow up thread not initiated
                        return;
                    }

                    jsonobject = jsonobject.getJSONObject("thread");
                    Log.d("TAG", "JsonObject: " + jsonobject);

                    String last_updated = jsonobject.getString("last_updated");
                    String last_message = jsonobject.getString("last_message");
                    String report_id = jsonobject.getString("report_id");

                    com.trinreport.m.app.model.Thread thread = new com.trinreport.m.app.model.
                            Thread(last_message, last_updated, report_id, title);
                    ChatBook.getChatBook(appContext).deleteThread(reportId);
                    ChatBook.getChatBook(appContext).addThread(thread);

                    JSONArray jsonarray = jsonobject.getJSONArray("messages");
                    for (int j = 0; j < jsonarray.length(); j++) {
                        JSONObject jsonobject2 = jsonarray.getJSONObject(j);
                        String timestamp = jsonobject2.getString("timestamp");
                        String content = jsonobject2.getString("content");
                        String from_admin = jsonobject2.getInt("from_admin") + "";

                        ChatMessage message = new ChatMessage(from_admin, content, timestamp, report_id);
                        ChatBook.getChatBook(appContext).addMessage(message);
                    }

                    //updateThreadsList();

                } catch (JSONException e) {
                    Log.d("TAG", "JSONException: " + e.toString());
                }
            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "Volley Error: " + error.toString());
                Toast.makeText(appContext, "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                MyData.put("report_id", reportId);

                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }



    //-----------------------tor----------------------//
    public void initTor() {
        InitializeTor job = new InitializeTor();
        job.execute();
    }

    public boolean isTorReady() {
        return mTorInitialized;
    }

    public HttpClient getTorClient() {
        return mHttpclient;
    }

    public HttpClientContext getTorContext() {
        return mHttpContext;
    }

    private class InitializeTor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {

            OnionProxyManager onionProxyManager = new AndroidOnionProxyManager(appContext, "torr");

            int totalSecondsPerTorStartup = 4 * 60;
            int totalTriesPerTorStartup = 5;
            try {
                boolean ok = onionProxyManager.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup);
                if (!ok)
                    Log.e("TorTest", "Couldn't start Tor!");

                while (!onionProxyManager.isRunning())
                    Thread.sleep(90);

                Log.v("TorTest", "Tor initialized on port " + onionProxyManager.getIPv4LocalHostSocksPort());


                mHttpclient = getNewHttpClient();
                int port = onionProxyManager.getIPv4LocalHostSocksPort();
                InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", port);
                mHttpContext = HttpClientContext.create();
                mHttpContext.setAttribute("socks.address", socksaddr);

                mTorInitialized = true;

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
    //-----------------------end tor----------------------//
}
