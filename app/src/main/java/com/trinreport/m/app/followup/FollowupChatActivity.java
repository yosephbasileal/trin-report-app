package com.trinreport.m.app.followup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.ApplicationContext;
import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.model.Report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowupChatActivity extends AppCompatActivity {

    private static final String TAG = "FollowupChatActivity";

    public static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORT_ID2";
    public static final String EXTRA_THREAD_TITLE = "com.trinreport.m.app.extra_THREAD_TITLE";

    private static final int VIEW_TYPE_LEFT = 0;
    private static final int VIEW_TYPE_RIGHT = 1;

    private ListView mListView;
    private EditText mChatText;
    private ImageButton mButtonSend;
    private boolean mSide = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private Toolbar mToolbar;

    private List<ChatMessage> mMessagesList = new ArrayList<>();
    private String mReportId;
    private Report mReport;
    private String mReportTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_followup_chat);

        mReportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        mReportTitle = getIntent().getStringExtra(EXTRA_THREAD_TITLE);

        getMessages();

        mReport = ChatBook.getChatBook(this).getReport(mReportId);

        mButtonSend = (ImageButton) findViewById(R.id.send);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_chat);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(mReportTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

/*        mListView = (ListView) findViewById(R.id.msgview);
        mChatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_view_right);
        mListView.setAdapter(mChatArrayAdapter);*/

        mRecyclerView = (RecyclerView) findViewById(R.id.msgview);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter();
        mRecyclerView.setAdapter(mAdapter);
        updateMessagesList();

        mChatText = (EditText) findViewById(R.id.msg);
        mChatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        //getFollowUpMessages();

/*        mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setAdapter(mChatArrayAdapter);*/

        //to scroll the list view to bottom on data change
/*        mChatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mListView.setSelection(mChatArrayAdapter.getCount() - 1);
            }
        });*/
    }

    private void updateMessagesList() {
        new GetMessageList().execute();
    }

    private boolean sendChatMessage() {
        // send to server
        String content = mChatText.getText().toString();
        sendMessage(content); 
        // add to list
        String fromAdmin = "0";
        String timestamp = new Date().toString();
        ChatMessage message = new ChatMessage(fromAdmin, content, timestamp, mReportId);
        mMessagesList.add(message);
        // save to local db
        ChatBook.getChatBook(this).addMessage(message);
        updateMessagesList();
        // reset edit text
        mChatText.setText("");
        // scroll to bottom of screen
        mLayoutManager.scrollToPosition(mMessagesList.size() - 1);
        return true;
    }

    private class MessageHolder extends RecyclerView.ViewHolder {

        private TextView mMessageTextView;

        public MessageHolder(View itemView, int viewType) {
            super(itemView);

            switch (viewType) {
                case VIEW_TYPE_LEFT:
                    mMessageTextView = (TextView) itemView.findViewById(R.id.msgl);
                    break;
                case VIEW_TYPE_RIGHT:
                    mMessageTextView = (TextView) itemView.findViewById(R.id.msgr);
                    break;
                default:
                    mMessageTextView = (TextView) itemView.findViewById(R.id.msgl);
            }

        }

        public void bindDrawable(final int Position) {
            mMessageTextView.setText(mMessagesList.get(Position).message);
        }
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

        public MessageAdapter() {
        }

        @Override
        public MessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = null;
            Log.d(TAG, "ViewType" + viewType);
            switch (viewType) {
                case VIEW_TYPE_LEFT:
                    view = inflater.inflate(R.layout.chat_view_left, viewGroup, false);
                    break;
                case VIEW_TYPE_RIGHT:
                    view = inflater.inflate(R.layout.chat_view_right, viewGroup, false);
                    break;
            }

            return new MessageHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(MessageHolder photoHolder, int position) {
            photoHolder.bindDrawable(position);
        }

        @Override
        public int getItemViewType(int position) {
            int viewType = VIEW_TYPE_LEFT;
            if (mMessagesList.get(position).isAdmin.equals("1")) {
                viewType = VIEW_TYPE_LEFT;
            } else {
                viewType = VIEW_TYPE_RIGHT;
            }
            return viewType;
        }

        @Override
        public int getItemCount() {
            return mMessagesList.size();
        }
    }

    private void getMessages() {
        // get url
        String url = URL.GET_FOLLOW_UP_MESSAGES;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get json object
                    JSONObject jsonObj = new JSONObject(response);
                    
                    // get json array of messages
                    JSONArray jsonarray = jsonObj.getJSONArray("messages");

                    ChatBook.getChatBook(getApplicationContext()).deleteMessages(mReportId);

                    for (int j = 0; j < jsonarray.length(); j++) {
                        JSONObject m = jsonarray.getJSONObject(j);
                        String timestamp = m.getString("timestamp");
                        String content = m.getString("content");
                        String from_admin = m.getInt("from_admin") + "";

                        // decrypt message
                        String prvKey = mReport.getPrvKey();
                        content = ApplicationContext.getInstance().decryptForUser(content, prvKey);

                        ChatMessage message = new ChatMessage(from_admin, content, timestamp, mReportId);
                        ChatBook.getChatBook(getApplicationContext()).addMessage(message);
                    }
                    updateMessagesList();
                } catch (Exception e) {
                    Log.d(TAG, "Exception: " + e.toString());
                }
            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                MyData.put("report_id", mReportId);

                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }

    private void sendMessage(final String message) {
        // get url
        String url = URL.SEND_FOLLOW_UP_MESSAGE;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                String user_public_key_pem = mReport.getPubKey();

                try {
                    String message_user = ApplicationContext.getInstance().encryptForUser(message, user_public_key_pem);
                    String message_admin = ApplicationContext.getInstance().encryptForAdmin(message);
                    MyData.put("message_user", message_user);
                    MyData.put("message_admin", message_admin);
                } catch (Exception e) {
                    Log.d(TAG, "Encryption error: " + e.getMessage());
                }

                MyData.put("report_id", mReportId);

                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }

    /**
     * Async task for gets thread list in the background
     */
    private class GetMessageList extends
            AsyncTask<Void, String, ArrayList<ChatMessage>> {

        @Override
        protected ArrayList<ChatMessage> doInBackground(Void... params) {
            ChatBook book = ChatBook.getChatBook(getApplicationContext());
            ArrayList<ChatMessage> messages = book.getMessages(mReportId);
            return messages;
        }

        @Override
        protected void onPostExecute(ArrayList<ChatMessage> result) {
            super.onPostExecute(result);
            mMessagesList = result;
            mAdapter.notifyDataSetChanged();
            mLayoutManager.scrollToPosition(mMessagesList.size() - 1);
        }
    }
}
