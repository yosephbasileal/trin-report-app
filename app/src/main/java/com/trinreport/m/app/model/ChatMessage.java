package com.trinreport.m.app.model;

import android.content.SharedPreferences;

import java.util.Date;

public class ChatMessage {
    public String isAdmin;  // sent by admin
    public String message;
    private String timestamp;
    private String report_id;

    public ChatMessage(String admin, String message, String timestamp, String report_id) {
        super();
        this.isAdmin = admin;
        this.message = message;
        this.timestamp = timestamp;
        this.report_id = report_id;
    }

    public String getIsAdmin() {
        return isAdmin;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getReportId() {
        return report_id;
    }
}

