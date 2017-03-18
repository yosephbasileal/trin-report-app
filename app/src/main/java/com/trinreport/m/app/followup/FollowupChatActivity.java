package com.trinreport.m.app.followup;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.R;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.data.ChatMessage;
import com.trinreport.m.app.data.Thread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowupChatActivity extends AppCompatActivity {

    private static final String TAG = "FollowupChatActivity";

    public static final String EXTRA_THREAD_ID = "com.trinreport.m.app.extra.REPORT_ID2";

    private static final int VIEW_TYPE_LEFT = 0;
    private static final int VIEW_TYPE_RIGHT = 1;

    private ChatArrayAdapter mChatArrayAdapter;
    private ListView mListView;
    private EditText mChatText;
    private Button mButtonSend;
    private boolean mSide = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private List<ChatMessage> mMessagesList = new ArrayList<>();
    private String mThreadId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_followup_chat);

        mThreadId = getIntent().getStringExtra(EXTRA_THREAD_ID);

        mButtonSend = (Button) findViewById(R.id.send);

/*        mListView = (ListView) findViewById(R.id.msgview);
        mChatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_view_right);
        mListView.setAdapter(mChatArrayAdapter);*/

        mRecyclerView = (RecyclerView) findViewById(R.id.msgview);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

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

        getFollowUpMessages();

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

    private boolean sendChatMessage() {
        String message = mChatText.getText().toString();
        Log.d(TAG, message);
        sendMessage(message); // send to server
        boolean fromAdmin = false;
        mMessagesList.add(new ChatMessage(fromAdmin, message, new Date()));
        mAdapter.notifyDataSetChanged();
        mChatText.setText("");
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
            if (mMessagesList.get(position).isAdmin) {
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

    private void getFollowUpMessages() {
        // get url
        String url = URL.GET_FOLLOW_UP_MESSAGES;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get report id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    Log.d(TAG, "JsonObject: " + jsonObj);
                    JSONArray jsonarray = jsonObj.getJSONArray("messages");
                    Log.d(TAG, "JsonArray: " + jsonarray);

                    //mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
                    //mRecyclerView.removeAllViewsInLayout();
                    mMessagesList.clear();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String timestamp = jsonobject.getString("timestamp");
                        String content = jsonobject.getString("content");
                        boolean from_admin = (jsonobject.getInt("from_admin") != 0);

                        ChatMessage message = new ChatMessage(from_admin, content, new Date());
                        mMessagesList.add(message);
                        mAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
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
                MyData.put("thread_id", mThreadId);

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
                try {
                    // get report id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    /*Log.d(TAG, "JsonObject: " + jsonObj);
                    JSONArray jsonarray = jsonObj.getJSONArray("messages");
                    Log.d(TAG, "JsonArray: " + jsonarray);

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String timestamp = jsonobject.getString("timestamp");
                        String content = jsonobject.getString("content");
                        boolean from_admin = (jsonobject.getInt("from_admin") != 0);

                        ChatMessage message = new ChatMessage(from_admin, content, new Date());
                        mMessagesList.add(message);
                        mAdapter.notifyDataSetChanged();

                    }*/

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
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
                MyData.put("message", message);
                MyData.put("thread_id", mThreadId);

                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }
}
