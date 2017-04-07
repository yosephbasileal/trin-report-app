package com.trinreport.m.app.model;

import android.text.format.DateUtils;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bimana2 on 3/24/17.
 */

public class Report {

    private String reportId;
    private String prv_key;
    private String title;
    private String pub_key;
    private long date_created;
    private String is_anon;
    private String status;

    public Report(String reportId, String prv_key, String title, String pub_key, long date, String is_anon, String status) {
        this.reportId = reportId;
        this.prv_key = prv_key;
        this.title = title;
        this.pub_key = pub_key;
        this.date_created = date;
        this.is_anon = is_anon;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public String getIsAnon() {
        return is_anon;
    }

    public boolean isAnon() {
        return is_anon.equals("1");
    }

    public String getNiceTimestamp() {
        try {
            Format formatter;
            Date date = new Date(date_created);

            if(DateUtils.isToday(date.getTime())) {
                formatter = new SimpleDateFormat("KK:mm a");
                return formatter.format(date);

            } else {
                formatter = new SimpleDateFormat("MMM dd");
                return formatter.format(date);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String toString() {
        return "Title: " + title + "  ID: " + reportId + "  isAnon: " + is_anon;
    }
}
