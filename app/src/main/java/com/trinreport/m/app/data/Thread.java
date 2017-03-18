package com.trinreport.m.app.data;

import java.util.Date;
import java.util.List;

/**
 * Created by bimana2 on 3/16/17.
 */

public class Thread {

    private List<String> mMessages;
    private String mLastMessage;
    private Date mLastUpdated;
    private String mThreadId;
    private String mTitle;

    public Thread(String lastMessage, Date lastUpdated, String threadId, String title) {
        mLastMessage = lastMessage;
        mLastUpdated = lastUpdated;
        mThreadId = threadId;
        mTitle = title;
    }

    public void setMessages(List<String> messages) {
        mMessages = messages;
    }

    public String getLastMessage() {
        return mLastMessage;
    }
    public List<String> getMessages() {
        return mMessages;
    }

    public Date getLastUpdated() {
        return mLastUpdated;
    }

    public String getThreadId() {
        return mThreadId;
    }

    public String getTitle() {
        return mTitle;
    }
}
