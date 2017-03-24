package com.trinreport.m.app.model;


import android.text.format.DateUtils;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by bimana2 on 3/16/17.
 */

public class Thread {

    private List<String> mMessages;
    private String mLastMessage;
    private String mLastUpdated;
    private String mThreadId;
    private String mTitle;

    public Thread(String lastMessage, String lastUpdated, String threadId, String title) {
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

    public String getLastUpdated() {
        return mLastUpdated;
    }

    public String getThreadId() {
        return mThreadId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getNiceTimestamp() {
        //String pattern = "E, dd MMM yyyy HH:mm:ss z";
        //SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            //Date date = format.parse(mLastUpdated);
            Format formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
            Date date = (Date) formatter.parseObject(mLastUpdated);
            System.out.println(date);

            if(DateUtils.isToday(date.getTime())) {
                formatter = new SimpleDateFormat("KK:mm a");
                return formatter.format(date);

            } else {
                formatter = new SimpleDateFormat("MMM dd");
                return formatter.format(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mLastUpdated.toString();
    }
}

