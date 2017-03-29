package com.trinreport.m.app.model;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatKey {

    private String reportId;
    private String prv_key;

    public ChatKey(String reportId, String prv_key) {
        this.reportId = reportId;
        this.prv_key = prv_key;
    }

    public String getReportId() {
        return reportId;
    }

    public String getPrvKey() {
        return prv_key;
    }
}
