package com.trinreport.m.app.tor;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;
import com.msopentech.thali.toronionproxy.OnionProxyManager;
import com.trinreport.m.app.ChatBook;

import java.net.InetSocketAddress;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.ssl.SSLContexts;

public class Tor {

    // constants
    private final String TAG = "Tor";

    // variables
    private boolean mTorReady;

    // other references
    private Context mAppContext;
    private HttpClient mHttpclient;
    private HttpClientContext mHttpContext;
    private OnionProxyManager mOnionProxyManager;

    private static Tor mTor;

    public Tor(Context context) {
        Log.d(TAG, "Tor object being created");
        // initialize tor
        mAppContext = context;
        mTorReady = false;
        Log.d(TAG, "About to initalize");
        InitializeTor job = new InitializeTor();
        job.execute();
        Log.d(TAG, "Called init");
    }

    public boolean isReady() {
        return mTorReady;
    }

    public static Tor getInstance(Context context) {
        if (mTor == null) {
            mTor = new Tor(context);
        }
        return mTor;
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
            Log.d(TAG, "Initializing tor");

            mOnionProxyManager = new AndroidOnionProxyManager(mAppContext, "torr");

            int totalSecondsPerTorStartup = 4 * 60;
            int totalTriesPerTorStartup = 5;
            try {
                boolean ok = mOnionProxyManager.startWithRepeat(totalSecondsPerTorStartup,
                        totalTriesPerTorStartup);
                if (!ok)
                    Log.e("TorTest", "Couldn't start Tor!");

                while (!mOnionProxyManager.isRunning())
                    Thread.sleep(90);

                Log.v("TorTest", "Tor initialized on port " +
                        mOnionProxyManager.getIPv4LocalHostSocksPort());


                mHttpclient = getNewHttpClient();
                int port = mOnionProxyManager.getIPv4LocalHostSocksPort();
                InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", port);
                mHttpContext = HttpClientContext.create();
                mHttpContext.setAttribute("socks.address", socksaddr);

                mTorReady = true;
                Log.d(TAG, "Tor initialized");

            }
            catch (Exception e) {
                Log.d(TAG, "Tor initialization exception");
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    /**
     * Creates a new tor client
     * @return http client
     */
    public HttpClient getNewHttpClient() {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(
                        SSLContexts.createSystemDefault()))
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }
}
