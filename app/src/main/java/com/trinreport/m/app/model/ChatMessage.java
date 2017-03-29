package com.trinreport.m.app.model;

import android.content.SharedPreferences;

import java.util.Date;

public class ChatMessage {
    public String isAdmin;  // sent by admin
    public String message;
    private String timestamp;
    private String thread_id;

    public ChatMessage(String admin, String message, String timestamp, String thread_id) {
        super();
        this.isAdmin = admin;
        this.message = message;
        this.timestamp = timestamp;
        this.thread_id = thread_id;
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

    public String getThreadId() {
        return thread_id;
    }
}

