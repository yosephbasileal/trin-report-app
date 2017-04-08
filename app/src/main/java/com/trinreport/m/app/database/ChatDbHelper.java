package com.trinreport.m.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Chat.db";

    private static final String SQL_CREATE_MESSAGES =
            "CREATE TABLE " + ChatDbContract.MessageEntry.TABLE_NAME + " (" +
                    ChatDbContract.MessageEntry._ID + " INTEGER PRIMARY KEY," +
                    ChatDbContract.MessageEntry.COLUMN_REPORT_ID + " TEXT," +
                    ChatDbContract.MessageEntry.COLUMN_MESSAGE + " TEXT," +
                    ChatDbContract.MessageEntry.COLUMN_IS_ADMIN + " TEXT," +
                    ChatDbContract.MessageEntry.COLUMN_TIMESTAMP + " TEXT)";

    private static final String SQL_CREATE_REPORTS =
            "CREATE TABLE " + ChatDbContract.ReportEntry.TABLE_NAME + " (" +
                    ChatDbContract.ReportEntry._ID + " INTEGER PRIMARY KEY," +
                    ChatDbContract.ReportEntry.COLUMN_PRIVATE_KEY + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_REPORT_ID + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_REPORT_TITLE + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_PUBLIC_KEY + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_DATE_CREATED + " INTEGER," +
                    ChatDbContract.ReportEntry.COLUMN_IS_ANON + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_STATUS + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_URGENCY + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_TIMESTAMP + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_LOCATION + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_DESCRIPTION + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_IS_RESP + " TEXT," +
                    ChatDbContract.ReportEntry.COLUMN_IS_FOLLOWUP + " TEXT)";


    private static final String SQL_DELETE_MESSAGES =
            "DROP TABLE IF EXISTS " + ChatDbContract.MessageEntry.TABLE_NAME;

    private static final String SQL_DELETE_REPORTS =
            "DROP TABLE IF EXISTS " + ChatDbContract.ReportEntry.TABLE_NAME;

    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MESSAGES);
        db.execSQL(SQL_CREATE_REPORTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_MESSAGES);
        db.execSQL(SQL_DELETE_REPORTS);
        onCreate(db);
    }

    public void deleteMessages(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_MESSAGES);
        db.execSQL(SQL_CREATE_MESSAGES);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
