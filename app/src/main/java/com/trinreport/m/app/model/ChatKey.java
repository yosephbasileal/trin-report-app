package com.trinreport.m.app.model;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatKey {

    private String reportId;
    private String prv_key;
    private String title;

    public ChatKey(String reportId, String prv_key, String title) {
        this.reportId = reportId;
        this.prv_key = prv_key;
        this.title = title;
    }

    public String getReportId() {
        return reportId;
    }

    public String getPrvKey() {
        return prv_key;
    }

    public String getTitle() {
        return title;
    }
}
