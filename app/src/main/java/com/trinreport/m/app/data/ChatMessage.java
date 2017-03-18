package com.trinreport.m.app.data;

import java.util.Date;

public class ChatMessage {
    public boolean isAdmin;  // sent by admin
    public String message;
    private Date timestamp;

    public ChatMessage(boolean admin, String message, Date timestamp) {
        super();
        this.isAdmin = admin;
        this.message = message;
        this.timestamp = timestamp;
    }
}