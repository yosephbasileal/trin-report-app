package com.trinreport.m.app;

import android.content.Context;

/**
 * Singleton class for getting application context
 */
public class ApplicationContext {

    private Context appContext;

    private ApplicationContext(){

    }

    public void init(Context context){
        if(appContext == null){
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
}
