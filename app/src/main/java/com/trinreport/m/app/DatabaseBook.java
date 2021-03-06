package com.trinreport.m.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trinreport.m.app.database.ChatDbContract;
import com.trinreport.m.app.database.ChatDbCursorWrapper;
import com.trinreport.m.app.database.ChatDbHelper;
import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.model.Report;

import java.util.ArrayList;

/**
 * Collection of methods that interface with database helper functions
 */

public class DatabaseBook {
    private static Context mContext;
    private static DatabaseBook mDatabaseBook;
    private static ChatDbHelper mDatabaseHelper;
    private static SQLiteDatabase mDatabase;

    /**
     * Constructor for RecipeBook
     */
    private DatabaseBook(Context context){
        mContext = context.getApplicationContext();
        mDatabaseHelper = new ChatDbHelper(mContext);
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    /**
     * Get the single instance of this class, create new if null
     * @param context applicaiton context
     * @return RecipeBook instance
     */
    public static DatabaseBook getChatBook(Context context) {
        if (mDatabaseBook == null) {
            mDatabaseBook = new DatabaseBook(context);
        }
        return mDatabaseBook;
    }

    /**
     * Delete all data in database
     */
    public void deleteAll() {
        mDatabaseHelper.onUpgrade(mDatabase, 0, 1);
    }

    /**
     * Delete all chat messages of a report
     */
    public void deleteMessages(String reportId) {
        mDatabase.delete(ChatDbContract.MessageEntry.TABLE_NAME, ChatDbContract.MessageEntry.COLUMN_REPORT_ID + " = ?",
                new String[]{reportId});
    }

    /**
     * Delete a report given its ID
     */
    public void deleteReport(String reportId) {
        mDatabase.delete(ChatDbContract.ReportEntry.TABLE_NAME, ChatDbContract.ReportEntry.COLUMN_REPORT_ID + " = ?",
                new String[]{reportId});
        mDatabase.delete(ChatDbContract.MessageEntry.TABLE_NAME, ChatDbContract.MessageEntry.COLUMN_REPORT_ID + " = ?",
                new String[]{reportId});
    }

    /**
     * Add a new message to database
     */
    public void addMessage(ChatMessage message) {
        ContentValues values = getContentValues(message);
        mDatabase.insert(ChatDbContract.MessageEntry.TABLE_NAME, null, values);
    }

    /**
     * Add a new report to database
     */
    public void addReport(Report report) {
        ContentValues values = getContentValues(report);
        mDatabase.insert(ChatDbContract.ReportEntry.TABLE_NAME, null, values);
    }

    /**
     * Save filled our form values of a report to database
     */
    public void addReportForm(String reportId, String urgency, String timestamp, String location, String description, String is_resp, String is_followup) {
        ContentValues values = new ContentValues();
        values.put(ChatDbContract.ReportEntry.COLUMN_URGENCY, urgency);
        values.put(ChatDbContract.ReportEntry.COLUMN_TIMESTAMP, timestamp);
        values.put(ChatDbContract.ReportEntry.COLUMN_LOCATION, location);
        values.put(ChatDbContract.ReportEntry.COLUMN_DESCRIPTION, description);
        values.put(ChatDbContract.ReportEntry.COLUMN_IS_RESP, is_resp);
        values.put(ChatDbContract.ReportEntry.COLUMN_IS_FOLLOWUP, is_followup);

        mDatabase.update(ChatDbContract.ReportEntry.TABLE_NAME, values, ChatDbContract.ReportEntry.COLUMN_REPORT_ID + " = ?",
                new String[]{reportId});
    }

    /**
     * Update report status in database
     */
    public void updateReportStatus(String reportId, String status) {
        ContentValues values = new ContentValues();
        values.put(ChatDbContract.ReportEntry.COLUMN_STATUS, status);
        mDatabase.update(ChatDbContract.ReportEntry.TABLE_NAME, values, ChatDbContract.ReportEntry.COLUMN_REPORT_ID + " = ?",
                new String[]{reportId});
    }

    /**
     * Gets content values of a message
     */
    private static ContentValues getContentValues(ChatMessage message) {
        ContentValues values = new ContentValues();
        values.put(ChatDbContract.MessageEntry.COLUMN_IS_ADMIN, message.getIsAdmin());
        values.put(ChatDbContract.MessageEntry.COLUMN_REPORT_ID, message.getReportId());
        values.put(ChatDbContract.MessageEntry.COLUMN_MESSAGE, message.getMessage());
        values.put(ChatDbContract.MessageEntry.COLUMN_TIMESTAMP, message.getTimestamp());
        return values;
    }

    /**
     * Gets content values of a report
     */
    private static ContentValues getContentValues(Report report) {
        ContentValues values = new ContentValues();
        values.put(ChatDbContract.ReportEntry.COLUMN_PRIVATE_KEY, report.getPrvKey());
        values.put(ChatDbContract.ReportEntry.COLUMN_REPORT_ID, report.getReportId());
        values.put(ChatDbContract.ReportEntry.COLUMN_REPORT_TITLE, report.getTitle());
        values.put(ChatDbContract.ReportEntry.COLUMN_PUBLIC_KEY, report.getPubKey());
        values.put(ChatDbContract.ReportEntry.COLUMN_DATE_CREATED, report.getDateCreated());
        values.put(ChatDbContract.ReportEntry.COLUMN_IS_ANON, report.getIsAnon());
        values.put(ChatDbContract.ReportEntry.COLUMN_STATUS, report.getStatus());
        return values;
    }

    /**
     * Get all messages of a report
     */
    public ArrayList<ChatMessage> getMessages(String reportId) {
        ArrayList<ChatMessage> messages = new ArrayList<>();

        ChatDbCursorWrapper cursor = queryMessages(
                ChatDbContract.MessageEntry.COLUMN_REPORT_ID + " = ?",
                new String[]{reportId});

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                ChatMessage message = cursor.getMessage();
                messages.add(message);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return messages;
    }

    /**
     * Get a report using its ID
     */
    public Report getReport(String reportId) {
        ChatDbCursorWrapper cursor = queryReports(
                ChatDbContract.ReportEntry.COLUMN_REPORT_ID + " = ?",
                new String[]{reportId}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getReport();
        } finally {
            cursor.close();
        }
    }

    /**
     * Get all reports
     */
    public ArrayList<Report> getReports() {
        ArrayList<Report> reports = new ArrayList<>();

        ChatDbCursorWrapper cursor = queryReports(null, null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                Report report = cursor.getReport();
                reports.add(report);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return reports;
    }

    /**
     * Get cursor for querying messages table
     * @param whereClause where clause of query
     * @param whereArgs arguments for query
     * @return cursor wrapper object
     */
    private ChatDbCursorWrapper queryMessages(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ChatDbContract.MessageEntry.TABLE_NAME,
                null, //all column
                whereClause,
                whereArgs,
                null, //group by
                null, //having
                null //order by
        );
        return new ChatDbCursorWrapper(cursor);
    }

    /**
     * Get cursor for querying messages table
     * @param whereClause where clause of query
     * @param whereArgs arguments for query
     * @return cursor wrapper object
     */
    private ChatDbCursorWrapper queryReports(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ChatDbContract.ReportEntry.TABLE_NAME,
                null, //all column
                whereClause,
                whereArgs,
                null, //group by
                null, //having
                null //order by
        );
        return new ChatDbCursorWrapper(cursor);
    }
}
