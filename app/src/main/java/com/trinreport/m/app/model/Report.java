package com.trinreport.m.app.model;

/**
 * Created by bimana2 on 3/24/17.
 */

public class Report {

    private String reportId;
    private String prv_key;
    private String title;
    private String pub_key;
    private long date_created;

    public Report(String reportId, String prv_key, String title, String pub_key, long date) {
        this.reportId = reportId;
        this.prv_key = prv_key;
        this.title = title;
        this.pub_key = pub_key;
        this.date_created = date;
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

    public String getPubKey() {
        return pub_key;
    }

    public long getDateCreated() {
        return date_created;
    }
}
