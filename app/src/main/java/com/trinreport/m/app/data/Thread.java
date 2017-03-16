package com.trinreport.m.app.data;

import java.util.Date;
import java.util.List;

/**
 * Created by bimana2 on 3/16/17.
 */

public class Thread {

    private List<String> mMessages;
    private Date mLastUpdated;
    private int mReportId;
    private String mTitle;

    public Thread(List<String> messages, Date lastUpdated, int reportId, String title) {
        mMessages = messages;
        mLastUpdated = lastUpdated;
        mReportId = reportId;
        mTitle = title;
    }

    public List<String> getMessages() {
        return mMessages;
    }

    public Date getLastUpdated() {
        return mLastUpdated;
    }

    public int getReportId() {
        return mReportId;
    }

    public String getTitle() {
        return mTitle;
    }
}
