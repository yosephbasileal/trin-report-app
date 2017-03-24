package com.trinreport.m.app.data;

import android.provider.BaseColumns;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ChatDbContract() {}

    /* Inner class that defines the thread table contents */
    public static class ThreadEntry implements BaseColumns {
        public static final String TABLE_NAME = "thread";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_LAST_MESSAGE = "last_message";
        public static final String COLUMN_LAST_UPDATED = "last_updated";
        public static final String COLUMN_THREAD_ID = "thread_id";
    }

    /* Inner class that defines the table contents */
    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_IS_ADMIN = "is_admin";
        public static final String COLUMN_THREAD_ID = "thread_id";
    }
}
