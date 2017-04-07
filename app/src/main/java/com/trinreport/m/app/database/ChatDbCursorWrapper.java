package com.trinreport.m.app.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.model.Report;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatDbCursorWrapper extends CursorWrapper {
    public ChatDbCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public ChatMessage getMessage() {
        // get values from databse
        String is_admin = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_IS_ADMIN));
        String content = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_MESSAGE));
        String timestamp = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_TIMESTAMP));
        String report_id = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_REPORT_ID));

        Log.d("Cursor: ", content);

        // create the object and set all the values
        ChatMessage message = new ChatMessage(is_admin, content, timestamp, report_id);
        return message;
    }

    public Report getReport() {
        // get values from databse
        String prv_key = getString(getColumnIndex(ChatDbContract.ReportEntry.COLUMN_PRIVATE_KEY));
        String report_id = getString(getColumnIndex(ChatDbContract.ReportEntry.COLUMN_REPORT_ID));
        String title = getString(getColumnIndex(ChatDbContract.ReportEntry.COLUMN_REPORT_TITLE));
        String pub_key = getString(getColumnIndex(ChatDbContract.ReportEntry.COLUMN_PUBLIC_KEY));
        long date_created = getLong(getColumnIndex(ChatDbContract.ReportEntry.COLUMN_DATE_CREATED));
        String is_anon = getString(getColumnIndex(ChatDbContract.ReportEntry.COLUMN_IS_ANON));
        String status = getString(getColumnIndex(ChatDbContract.ReportEntry.COLUMN_STATUS));

        // create the object and set all the values
        Report report = new Report(report_id, prv_key, title, pub_key, date_created, is_anon, status);
        return report;
    }
}
