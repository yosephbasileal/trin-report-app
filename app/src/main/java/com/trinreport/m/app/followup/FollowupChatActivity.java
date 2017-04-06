package com.trinreport.m.app.followup;

import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;
import com.msopentech.thali.toronionproxy.OnionProxyManager;
import com.trinreport.m.app.ApplicationContext;
import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.model.Report;
import com.trinreport.m.app.tor.MyConnectionSocketFactory;
import com.trinreport.m.app.tor.MySSLConnectionSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.ssl.SSLContexts;

public class FollowupChatActivity extends AppCompatActivity {

    private static final String TAG = "FollowupChatActivity";
    private static int MY_SOCKET_TIMEOUT_MS = 10000; // 10 seconds

    public static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORT_ID2";
    public static final String EXTRA_THREAD_TITLE = "com.trinreport.m.app.extra_THREAD_TITLE";

    private static final int VIEW_TYPE_LEFT = 0;
    private static final int VIEW_TYPE_RIGHT = 1;

    private ListView mListView;
    private EditText mChatText;
    private ImageButton mButtonSend;
    private ImageButton mButtonRefresh;
    private boolean mSide = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private Toolbar mToolbar;
    private TextView mNotice;
    private LinearLayout mForm;

    private List<ChatMessage> mMessagesList = new ArrayList<>();
    private String mReportId;
    private Report mReport;
    private String mReportTitle;

    SharedPreferences mSharedPref;
    private String mAdminPublicKey;

    // tor client
    private HttpClient mHttpclient;
    private boolean mTorInitialized;
    private HttpClientContext mHttpContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_followup_chat);

        mReportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        mReportTitle = getIntent().getStringExtra(EXTRA_THREAD_TITLE);
        mReport = ChatBook.getChatBook(this).getReport(mReportId);

        Log.d(TAG, "Report: " + mReport.toString());

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mAdminPublicKey = mSharedPref.getString("admin_public_key", "");

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

        mNotice = (TextView) findViewById(R.id.chat_text_notice);
        mButtonSend = (ImageButton) findViewById(R.id.send);
        mButtonRefresh = (ImageButton) findViewById(R.id.refresh_chat);
        mRecyclerView = (RecyclerView) findViewById(R.id.msgview);
        mChatText = (EditText) findViewById(R.id.msg);
        mForm = (LinearLayout) findViewById(R.id.form);


        if(mReport.getStatus().equals("Failed to send")) {
            mButtonSend.setVisibility(View.GONE);
            mForm.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mNotice.setText("Your report was not sent successully. Try again!");
            return;
        }

        if(mReport.getStatus().equals("Sending")) {
            mButtonSend.setVisibility(View.GONE);
            mForm.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mNotice.setText("Your report is being processed to be sent. Check back later.");
            return;
        }


        getMessages();
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter();
        mRecyclerView.setAdapter(mAdapter);
        updateMessagesList();


        mChatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });

        mButtonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages();
            }
        });

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });
    }

    private void updateMessagesList() {
        new GetMessageList().execute();
    }

    private boolean sendChatMessage() {
        // send to server
        String content = mChatText.getText().toString();
        if(!mReport.isAnon()) {
            sendMessage(content);
        } else {
            sendMessageThroughTor(content);
        }

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

            if(mMessagesList.size() == 0) {
                mNotice.setText("No follow up messages. You can send message by writing below.");
            } else {
                mNotice.setText("");
            }
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
                    e.printStackTrace();
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

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // add to queue
        requestQueue.add(stringRequest);
    }

    private void initTor() {
        InitializeTor job = new InitializeTor();
        job.execute();
    }

    private void sendMessageThroughTor(String message) {
        MessageThroughTor job = new MessageThroughTor();
        job.execute();
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
                    String message_admin = ApplicationContext.getInstance().encryptForAdmin(message, mAdminPublicKey);
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

    private class MessageThroughTor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            try {
                // wait if not tor initilized yet
                while(!mTorInitialized) {
                    Thread.sleep(90);
                }
                // get URL
                HttpPost httpost = new HttpPost(URL.SEND_FOLLOW_UP_MESSAGE);

                Map<String, String> MyData = new HashMap<>();

                String message = params[0];
                String user_public_key_pem = mReport.getPubKey();

                try {
                    String message_user = ApplicationContext.getInstance().encryptForUser(message, user_public_key_pem);
                    String message_admin = ApplicationContext.getInstance().encryptForAdmin(message, mAdminPublicKey);
                    MyData.put("message_user", message_user);
                    MyData.put("message_admin", message_admin);
                } catch (Exception e) {
                    Log.d(TAG, "Encryption error: " + e.getMessage());
                }

                MyData.put("report_id", mReportId);

                // convert hashmap to json object
                JSONObject holder = new JSONObject(MyData);

                // pass results to a string builder
                StringEntity se = new StringEntity(holder.toString());

                // set the post request as the resulting string
                httpost.setEntity(se);

                // set request headers
                httpost.setHeader("Accept", "application/json");
                httpost.setHeader("Content-type", "application/json");

                // set timeout
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, MY_SOCKET_TIMEOUT_MS);

                // handle reponse
                HttpResponse response;
                response = mHttpclient.execute(httpost, mHttpContext);
                // Examine the response status
                Log.i("TorTest", response.getStatusLine().toString());
                // Get hold of the response entity
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release
                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);

                    // get report id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(result);
                    String report_id = jsonObj.get("report_id").toString();


                    Log.d("TorTest", "Result: " + result);
                    // now you have the string representation of the HTML request
                    instream.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }

            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    private class InitializeTor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {

            OnionProxyManager onionProxyManager = new AndroidOnionProxyManager(getApplicationContext(), "torr");

            int totalSecondsPerTorStartup = 4 * 60;
            int totalTriesPerTorStartup = 5;
            try {
                boolean ok = onionProxyManager.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup);
                if (!ok)
                    Log.e("TorTest", "Couldn't start Tor!");

                while (!onionProxyManager.isRunning())
                    Thread.sleep(90);

                Log.v("TorTest", "Tor initialized on port " + onionProxyManager.getIPv4LocalHostSocksPort());


                mHttpclient = getNewHttpClient();
                int port = onionProxyManager.getIPv4LocalHostSocksPort();
                InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", port);
                mHttpContext = HttpClientContext.create();
                mHttpContext.setAttribute("socks.address", socksaddr);

                mTorInitialized = true;

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    public HttpClient getNewHttpClient() {

        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    // source: http://stackoverflow.com/questions/4457492/how-do-i-use-the-simple-http-client-in-android
    private static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
