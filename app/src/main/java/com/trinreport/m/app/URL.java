package com.trinreport.m.app;

/**
 * URL mappings
 */

public class URL {

    // domains
    public static String AUTH_SERVER_DOMAIN = "http://trinreport.appspot.com";
    //public static String RDDP_SERVER_DOMAIN = "https://trinreport.com";
    public static String RDDP_SERVER_DOMAIN = "http://7ef3b12c.ngrok.io";

    // authentication urls
    public static String REQUEST_AUTH_TOKEN = AUTH_SERVER_DOMAIN + "/request";
    public static String VERIFY_AUTH_CODE = AUTH_SERVER_DOMAIN + "/verify";
    public static String PUBLISH_PUBLIC_KEY = RDDP_SERVER_DOMAIN + "/api/app/publish-user-public-key";

    // emergency urls
    public static String SEND_EMERGENCY_REQUEST = RDDP_SERVER_DOMAIN + "/emergency-request";
    public static String SEND_EMERGENCY_EXPLANATION = RDDP_SERVER_DOMAIN + "/emergency-explanation";
    public static String EMERGENCY_CALLME_CHECKBOX = RDDP_SERVER_DOMAIN + "/emergency-callme-checkbox";
    public static String CHECK_EMERGENCY_STATUS = RDDP_SERVER_DOMAIN + "/check-emergency-status";

    // report urls
    public static String SEND_REPORT = RDDP_SERVER_DOMAIN + "/report";
    public static String SEND_REPORT_ANON = RDDP_SERVER_DOMAIN + "/report-anonymous";

    // followup urls
    public static String GET_FOLLOW_UP_THREADS = RDDP_SERVER_DOMAIN + "/get-followup-threads";
    public static String GET_FOLLOW_UP_MESSAGES = RDDP_SERVER_DOMAIN + "/get-followup-messages";
    public static String SEND_FOLLOW_UP_MESSAGE = RDDP_SERVER_DOMAIN + "/send-followup-message";
}
