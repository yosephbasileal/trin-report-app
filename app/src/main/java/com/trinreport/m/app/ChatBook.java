package com.trinreport.m.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trinreport.m.app.database.ChatDbContract;
import com.trinreport.m.app.database.ChatDbCursorWrapper;
import com.trinreport.m.app.database.ChatDbHelper;
import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.model.Thread;

import java.util.ArrayList;

/**
 * Created by bimana2 on 3/24/17.
 */

public class ChatBook {
    private static Context mContext;
    private static ChatBook mChatBook;
    private static ChatDbHelper mDatabaseHelper;
    private static SQLiteDatabase mDatabase;

    /**
     * Constructor for RecipeBook
     * @param context application context
     */
    private ChatBook(Context context){
        mContext = context.getApplicationContext();
        mDatabaseHelper = new ChatDbHelper(mContext);
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    /**
     * Get the single instance of this class, create new if null
     * @param context applicaiton context
     * @return RecipeBook instance
     */
    public static ChatBook getChatBook(Context context) {
        if (mChatBook == null) {
            mChatBook = new ChatBook(context);
        }
        return mChatBook;
    }

    public void deleteThreads() {
        mDatabaseHelper.onUpgrade(mDatabase, 0, 1);
    }

    public void addThread(Thread thread) {
        ContentValues values = getContentValues(thread);
        mDatabase.insert(ChatDbContract.ThreadEntry.TABLE_NAME, null, values);
    }

    public void addMessage(ChatMessage message) {
        ContentValues values = getContentValues(message);
        mDatabase.insert(ChatDbContract.MessageEntry.TABLE_NAME, null, values);
    }

    /**
     * Gets content values of a thread
     */
    private static ContentValues getContentValues(Thread thread) {
        ContentValues values = new ContentValues();
        values.put(ChatDbContract.ThreadEntry.COLUMN_TITLE, thread.getTitle());
        values.put(ChatDbContract.ThreadEntry.COLUMN_THREAD_ID, thread.getThreadId());
        values.put(ChatDbContract.ThreadEntry.COLUMN_LAST_MESSAGE, thread.getLastMessage());
        values.put(ChatDbContract.ThreadEntry.COLUMN_LAST_UPDATED, thread.getLastUpdated());
        return values;
    }

    /**
     * Gets content values of a message
     */
    private static ContentValues getContentValues(ChatMessage message) {
        ContentValues values = new ContentValues();
        values.put(ChatDbContract.MessageEntry.COLUMN_IS_ADMIN, message.getIsAdmin());
        values.put(ChatDbContract.MessageEntry.COLUMN_THREAD_ID, message.getThreadId());
        values.put(ChatDbContract.MessageEntry.COLUMN_MESSAGE, message.getMessage());
        values.put(ChatDbContract.MessageEntry.COLUMN_TIMESTAMP, message.getTimestamp());
        return values;
    }

    /**
     * Get all threads
     */
    public ArrayList<Thread> getThreads() {
        ArrayList<Thread> threads = new ArrayList<>();

        ChatDbCursorWrapper cursor = queryThreads(null,null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                Thread thread = cursor.getThread();
                threads.add(thread);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return threads;
    }

    /**
     * Get all messages of a thread
     */
    public ArrayList<ChatMessage> getMessages(String thread_id) {
        ArrayList<ChatMessage> messages = new ArrayList<>();

        ChatDbCursorWrapper cursor = queryMessages(
                ChatDbContract.MessageEntry.COLUMN_THREAD_ID + " = ?",
                new String[]{thread_id});

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
     * Get cursor for querying threads table
     * @param whereClause where clause of query
     * @param whereArgs arguments for query
     * @return cursor wrapper object
     */
    private ChatDbCursorWrapper queryThreads(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ChatDbContract.ThreadEntry.TABLE_NAME,
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
}
