package com.trinreport.m.app.database;

import android.provider.BaseColumns;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ChatDbContract() {}

    /* Inner class that defines the table contents */
    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_IS_ADMIN = "is_admin";
        public static final String COLUMN_REPORT_ID = "report_id";
    }

    /* Inner class that defines the table contents */
    public static class ReportEntry implements BaseColumns {
        public static final String TABLE_NAME = "keys";
        public static final String COLUMN_PRIVATE_KEY = "privatekey";
        public static final String COLUMN_REPORT_ID = "report_id";
        public static final String COLUMN_REPORT_TITLE = "title";
        public static final String COLUMN_PUBLIC_KEY = "publickey";
        public static final String COLUMN_DATE_CREATED = "date_created";
        public static final String COLUMN_IS_ANON = "is_anonymous";
    }
}
