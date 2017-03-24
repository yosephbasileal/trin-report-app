package com.trinreport.m.app.data;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatDbCursorWrapper extends CursorWrapper {
    public ChatDbCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Thread getThread() {
        // get values from databse
        String title = getString(getColumnIndex(ChatDbContract.ThreadEntry.COLUMN_TITLE));
        String last_message = getString(getColumnIndex(ChatDbContract.ThreadEntry.COLUMN_LAST_MESSAGE));
        String last_updated = getString(getColumnIndex(ChatDbContract.ThreadEntry.COLUMN_LAST_UPDATED));
        String thread_id = getString(getColumnIndex(ChatDbContract.ThreadEntry.COLUMN_THREAD_ID));

        // create the object and set all the values
        Thread thread = new Thread(last_message,last_updated,thread_id,title);
        return thread;
    }

    public ChatMessage getMessage() {
        // get values from databse
        String is_admin = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_IS_ADMIN));
        String content = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_MESSAGE));
        String timestamp = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_TIMESTAMP));
        String thread_id = getString(getColumnIndex(ChatDbContract.MessageEntry.COLUMN_THREAD_ID));

        // create the object and set all the values
        ChatMessage message = new ChatMessage(is_admin, content, timestamp, thread_id);
        return message;
    }
}
