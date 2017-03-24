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

    private static final String SQL_CREATE_THREADS =
            "CREATE TABLE " + ChatDbContract.ThreadEntry.TABLE_NAME + " (" +
                    ChatDbContract.ThreadEntry._ID + " INTEGER PRIMARY KEY," +
                    ChatDbContract.ThreadEntry.COLUMN_THREAD_ID + " TEXT," +
                    ChatDbContract.ThreadEntry.COLUMN_TITLE + " TEXT," +
                    ChatDbContract.ThreadEntry.COLUMN_LAST_MESSAGE + " TEXT," +
                    ChatDbContract.ThreadEntry.COLUMN_LAST_UPDATED + " TEXT)";

    private static final String SQL_CREATE_MESSAGES =
            "CREATE TABLE " + ChatDbContract.MessageEntry.TABLE_NAME + " (" +
                    ChatDbContract.MessageEntry._ID + " INTEGER PRIMARY KEY," +
                    ChatDbContract.MessageEntry.COLUMN_THREAD_ID + " TEXT," +
                    ChatDbContract.MessageEntry.COLUMN_MESSAGE + " TEXT," +
                    ChatDbContract.MessageEntry.COLUMN_IS_ADMIN + " TEXT," +
                    ChatDbContract.MessageEntry.COLUMN_TIMESTAMP + " TEXT)";

    private static final String SQL_DELETE_MESSAGES =
            "DROP TABLE IF EXISTS " + ChatDbContract.ThreadEntry.TABLE_NAME;

    private static final String SQL_DELETE_THREADS =
            "DROP TABLE IF EXISTS " + ChatDbContract.MessageEntry.TABLE_NAME;

    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_THREADS);
        db.execSQL(SQL_CREATE_MESSAGES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_THREADS);
        db.execSQL(SQL_DELETE_MESSAGES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
