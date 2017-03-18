package com.trinreport.m.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.trinreport.m.app.data.ThreadContract.ThreadEntry;
import com.trinreport.m.app.data.ThreadContract.MessageEntry;

/**
 * Manages a local database for thread data.
 */
public class ThreadDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "thread.db";

    public ThreadDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_THREAD_TABLE = "CREATE TABLE " + ThreadEntry.TABLE_NAME + " (" +
                ThreadEntry._ID + " INTEGER PRIMARY KEY," +
                ThreadEntry.COLUMN_THREAD_ID + " INTEGER UNIQUE NOT NULL, " +
                ThreadEntry.COLUMN_REPORT_ID + " INTEGER NOT NULL, " +
                ThreadEntry.COLUMN_LAST_UPDATED + " INTEGER NOT NULL, " +
                ThreadEntry.COLUMN_LAST_MESSAGE + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_MESSAGE_TABLE = "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MessageEntry.COLUMN_MESSAGE_ID + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_THREAD_ID + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                MessageEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_FROM_ADMIN + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + MessageEntry.COLUMN_THREAD_ID + ") REFERENCES " +
                ThreadEntry.TABLE_NAME + " (" + MessageEntry.COLUMN_THREAD_ID + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_THREAD_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ThreadEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}