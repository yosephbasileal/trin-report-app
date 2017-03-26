package com.trinreport.m.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.trinreport.m.app.database.ChatDbContract;
import com.trinreport.m.app.database.ChatDbCursorWrapper;
import com.trinreport.m.app.database.ChatDbHelper;
import com.trinreport.m.app.model.ChatKey;
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

    public void deleteAll() {
        mDatabaseHelper.onUpgrade(mDatabase, 0, 1);
    }

    public void deleteThreads() {
        mDatabaseHelper.deleteThreads(mDatabase);
    }

    public void deleteThread(String threadId) {
        mDatabase.delete(ChatDbContract.ThreadEntry.TABLE_NAME, ChatDbContract.ThreadEntry.COLUMN_THREAD_ID + " = ?",
                new String[]{threadId});
        mDatabase.delete(ChatDbContract.MessageEntry.TABLE_NAME, ChatDbContract.MessageEntry.COLUMN_THREAD_ID + " = ?",
                new String[]{threadId});
    }

    public void deleteKeys() {
        mDatabaseHelper.deleteKeys(mDatabase);
    }

    public void addThread(Thread thread) {
        ContentValues values = getContentValues(thread);
        mDatabase.insert(ChatDbContract.ThreadEntry.TABLE_NAME, null, values);
    }

    public void addMessage(ChatMessage message) {
        ContentValues values = getContentValues(message);
        mDatabase.insert(ChatDbContract.MessageEntry.TABLE_NAME, null, values);
    }

    public void addChatKey(ChatKey key) {
        ContentValues values = getContentValues(key);
        mDatabase.insert(ChatDbContract.KeyEntry.TABLE_NAME, null, values);
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
     * Gets content values of a chat key
     */
    private static ContentValues getContentValues(ChatKey key) {
        ContentValues values = new ContentValues();
        values.put(ChatDbContract.KeyEntry.COLUMN_PRIVATE_KEY, key.getPrvKey());
        values.put(ChatDbContract.KeyEntry.COLUMN_REPORT_ID, key.getReportId());
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
    public ArrayList<ChatMessage> getMessages(String threadId) {
        Log.d("Chatbook", threadId);
        ArrayList<ChatMessage> messages = new ArrayList<>();

        ChatDbCursorWrapper cursor = queryMessages(
                ChatDbContract.MessageEntry.COLUMN_THREAD_ID + " = ?",
                new String[]{threadId});

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
     * Get all chat thread keys
     */
    public ArrayList<ChatKey> getKeys() {
        ArrayList<ChatKey> keys = new ArrayList<>();

        ChatDbCursorWrapper cursor = queryKeys(null, null);

        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                ChatKey key = cursor.getKey();
                keys.add(key);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return keys;
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

    /**
     * Get cursor for querying messages table
     * @param whereClause where clause of query
     * @param whereArgs arguments for query
     * @return cursor wrapper object
     */
    private ChatDbCursorWrapper queryKeys(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ChatDbContract.KeyEntry.TABLE_NAME,
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
