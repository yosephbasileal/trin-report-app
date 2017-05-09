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
    private static ApplicationContext mInstance;

    private ApplicationContext(){
    }

    /**
     * Initializes application context
     */
    public void init(Context context){
        // initialize context
        if(appContext == null){
            Log.d("TAG", "Application context initilized");
            appContext = context;
        }
    }

    /**
     * Returns a reference to application context
     */
    private Context getContext(){
        return appContext;
    }

    /**
     * Another way to get application context
     */
    public static Context get(){
        return getInstance().getContext();
    }

    /**
     * Get application context, creates new one if none exists
     */
    public static ApplicationContext getInstance(){
        return mInstance == null ?
                (mInstance = new ApplicationContext()):
                mInstance;
    }

    /**
     * Helper method for encrypting a string using admins public key
     */
    public String encryptForAdmin(String plain, String pubKey) throws Exception {
        return RSA.encrypt(plain, pubKey);
    }

    /**
     * Helper method for encrypting a string using user's public key
     */
    public String encryptForUser(String plain, String pubKey) throws Exception {
        return RSA.encrypt(plain, pubKey);
    }

    /**
     * Helper method for decrypting a string using user's private key
     */
    public String decryptForUser(String cipher, String prvKey) throws Exception {
        return RSA.decrypt(cipher, prvKey);
    }
}
