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

    private ApplicationContext(){
    }

    public void init(Context context){
        // initialize context
        if(appContext == null){
            Log.d("TAG", "Application context initilized");
            appContext = context;
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

    public String encryptForAdmin(String plain, String pubKey) throws Exception {
        return RSA.encrypt(plain, pubKey);
    }

    public String encryptForUser(String plain, String pubKey) throws Exception {
        return RSA.encrypt(plain, pubKey);
    }

    public String decryptForUser(String cipher, String prvKey) throws Exception {
        return RSA.decrypt(cipher, prvKey);
    }
}
