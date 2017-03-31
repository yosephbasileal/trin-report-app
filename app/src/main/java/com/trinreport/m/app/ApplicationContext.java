package com.trinreport.m.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;
import com.msopentech.thali.toronionproxy.OnionProxyManager;

import com.trinreport.m.app.tor.MyConnectionSocketFactory;
import com.trinreport.m.app.tor.MySSLConnectionSocketFactory;

import java.net.InetSocketAddress;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
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
            Log.d("TAG", "Application context initilized");
            appContext = context;

            // get reference to admin public key
            SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            mAdminPublicKey = mSharedPrefs.getString("admin_public_key", "");
        }
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

    public String encryptForUser(String plain, String pubKey) throws Exception {
        return RSA.encrypt(plain, pubKey);
    }

    public String decryptForUser(String cipher, String prvKey) throws Exception {
        return RSA.decrypt(cipher, prvKey);
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
