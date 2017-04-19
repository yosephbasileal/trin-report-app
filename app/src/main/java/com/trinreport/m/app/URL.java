package com.trinreport.m.app;

/**
 * URL mappings
 */

public class URL {

    // domains
    public static String AUTH_SERVER_DOMAIN = "https://trinreport.appspot.com";
    //public static String RDDP_SERVER_DOMAIN = "https://trinreport.com";
    public static String RDDP_SERVER_DOMAIN = "https://c62a14e0.ngrok.io";

    // authentication urls
    public static String REQUEST_AUTH_TOKEN = AUTH_SERVER_DOMAIN + "/request";
    public static String VERIFY_AUTH_CODE = AUTH_SERVER_DOMAIN + "/verify";
    public static String PUBLISH_PUBLIC_KEY = RDDP_SERVER_DOMAIN + "/api/app/publish-user-public-key";

    // emergency urls
    public static String SEND_EMERGENCY_REQUEST = RDDP_SERVER_DOMAIN + "/api/app/emergency-request";
    public static String SEND_EMERGENCY_EXPLANATION = RDDP_SERVER_DOMAIN + "/api/app/emergency-explanation";
    public static String EMERGENCY_CALLME_CHECKBOX = RDDP_SERVER_DOMAIN + "/api/app/emergency-callme-checkbox";
    public static String CHECK_EMERGENCY_STATUS = RDDP_SERVER_DOMAIN + "/api/app/update-emergency-status";
    public static String MARK_EMERGENCY_AS_DONE = RDDP_SERVER_DOMAIN + "/api/app/emergency-done";

    // report urls
    public static String SEND_REPORT = RDDP_SERVER_DOMAIN + "/api/app/report-request";

    // followup urls
    public static String GET_FOLLOW_UP_MESSAGES = RDDP_SERVER_DOMAIN + "/api/app/followup-messages";
    public static String SEND_FOLLOW_UP_MESSAGE = RDDP_SERVER_DOMAIN + "/api/app/add-new-message";
}
