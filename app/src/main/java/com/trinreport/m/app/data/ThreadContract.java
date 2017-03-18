package com.trinreport.m.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the weather database.
 */
public class ThreadContract {

    // The "Content authority" is a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.trinreport.m.app.data";


    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_MESSAGE = "message";
    public static final String PATH_THREAD = "thread";

    // Normalize all dates to the start of the the Julian day at UTC.
    public static long normalizeDate(long date) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(date);
        int julianDay = Time.getJulianDay(date, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Defines the table contents of the thread table */
    public static final class ThreadEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_THREAD).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_THREAD;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_THREAD;

        // Table name
        public static final String TABLE_NAME = "threads";

        public static final String COLUMN_THREAD_ID = "thread_id";
        public static final String COLUMN_REPORT_ID = "report_id";
        public static final String COLUMN_LAST_UPDATED= "last_updated";
        public static final String COLUMN_LAST_MESSAGE = "last_message";

        public static Uri buildThreadUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Defines the table contents of the message table */
    public static final class MessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGE;

        public static final String TABLE_NAME = "messages";

        public static final String COLUMN_THREAD_ID = "thread_id"; // foreign key
        public static final String COLUMN_MESSAGE_ID = "message_id";
        public static final String COLUMN_TIMESTAMP= "timestamp";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_FROM_ADMIN = "from_admin";


        public static Uri buildMessageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}