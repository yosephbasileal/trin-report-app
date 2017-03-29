package com.trinreport.m.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Singleton class for getting application context
 */
public class ApplicationContext {

    private Context appContext;
    private String mAdminPublicKey;

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
}
