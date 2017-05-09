package com.trinreport.m.app.followup;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.trinreport.m.app.DatabaseBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.model.Report;
import com.trinreport.m.app.report.SendReportService;
import com.trinreport.m.app.tor.MyConnectionSocketFactory;
import com.trinreport.m.app.tor.MySSLConnectionSocketFactory;
import com.trinreport.m.app.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
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

    // constants
    private static final String TAG = "FollowupChatActivity";
    private static int MY_SOCKET_TIMEOUT_MS = 10000; // 10 seconds
    public static final String EXTRA_REPORT_ID = "com.trinreport.m.app.extra.REPORT_ID2";
    public static final String EXTRA_THREAD_TITLE = "com.trinreport.m.app.extra_THREAD_TITLE";
    private static final int VIEW_TYPE_LEFT = 0;
    private static final int VIEW_TYPE_RIGHT = 1;

    // layout references
    private EditText mChatText;
    private ImageButton mButtonSend;
    private ImageButton mButtonRefresh;
    private LinearLayout mNoticeButtons;
    private Button mButtonRetry;
    private Button mButtonView;
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private TextView mNotice;
    private LinearLayout mForm;
    private ProgressDialog mProgressDialog;

    // variables
    private List<ChatMessage> mMessagesList = new ArrayList<>();
    private String mReportId;
    private Report mReport;
    private String mReportTitle;
    private String mAdminPublicKey;

    // other references
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private SharedPreferences mSharedPref;

    // tor client references
    private HttpClient mHttpclient;
    private HttpClientContext mHttpContext;
    private OnionProxyManager mOnionProxyManager;
    private boolean mTorReady;
    private boolean mTorInitializing;

    public FollowupChatActivity() {
        mTorReady = false;
        mTorInitializing = false;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // infalte layout
        setContentView(R.layout.activity_followup_chat);

        // get data from intent
        mReportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        mReportTitle = getIntent().getStringExtra(EXTRA_THREAD_TITLE);
        mReport = DatabaseBook.getChatBook(this).getReport(mReportId);
        Log.d(TAG, "Report: " + mReport.toString());

        // get references
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mAdminPublicKey = mSharedPref.getString("admin_public_key", "");
        mToolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        mNotice = (TextView) findViewById(R.id.chat_text_notice);
        mButtonSend = (ImageButton) findViewById(R.id.send);
        mButtonRefresh = (ImageButton) findViewById(R.id.refresh_chat);
        mButtonRetry = (Button) findViewById(R.id.button_retry);
        mButtonView = (Button) findViewById(R.id.button_view);
        mNoticeButtons = (LinearLayout) findViewById(R.id.chat_notice_buttons) ;
        mRecyclerView = (RecyclerView) findViewById(R.id.msgview);
        mChatText = (EditText) findViewById(R.id.msg);
        mForm = (LinearLayout) findViewById(R.id.form);

        // setup toolbar
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(mReportTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_36dp);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // if report failed to send
        if(mReport.getStatus().equals("Failed to send")) {
            // hide chat UI
            mButtonSend.setVisibility(View.GONE);
            mForm.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);

            // show "not sent" message
            mNotice.setText("Your report was not sent successully. Try again!");
            mNoticeButtons.setVisibility(View.VISIBLE);

            // add listener for retry button
            mButtonRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resendReport();
                }
            });
            return;
        }

        // if report is current being sent
        if(mReport.getStatus().equals("Sending")) {
            // hide chat UI
            mButtonSend.setVisibility(View.GONE);
            mForm.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);

            // show "being sent" message
            mNotice.setText("Your report is being processed to be sent. Check back later.");
            mNoticeButtons.setVisibility(View.INVISIBLE);
            return;
        }

        // initialize adapter for displaying messages in recycler view
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // get messages from local db
        updateMessagesList();

        // if report has been sent, get followup messages from rddp, if any, and store in local db
        getMessagesFromServer();

        // add listener to chat text view
        mChatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return prepareAndSend();
                }
                return false;
            }
        });

        // add listener to refresh button
        mButtonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get updated data from rddp server and store in db
                getMessagesFromServer();
            }
        });

        // add listener to send message button
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                prepareAndSend();
            }
        });
    }

    @Override
    protected  void onStop() {
        Log.d(TAG, "FollowupChat activity stopped");
        try {
            if(mOnionProxyManager != null)
                mOnionProxyManager.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    private void getMessagesFromServer() {
        if(mReport.isAnon()) {
            if(!mTorReady && !mTorInitializing) {
                mTorInitializing = true;
                InitializeTor job1 = new InitializeTor();
                job1.execute();
            }
            GetMessagesThroughTor job = new GetMessagesThroughTor();
            job.execute();
        } else {
            getMessages();
        }
    }

    private void updateMessagesList() {
        new GetMessageList().execute();
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
            DatabaseBook book = DatabaseBook.getChatBook(getApplicationContext());
            ArrayList<ChatMessage> messages = book.getMessages(mReportId);
            return messages;
        }

        @Override
        protected void onPostExecute(ArrayList<ChatMessage> result) {
            super.onPostExecute(result);
            mMessagesList = result;
            mAdapter.notifyDataSetChanged();
            mLayoutManager.scrollToPosition(mMessagesList.size() - 1);
            Log.d(TAG, "Messages: " + mMessagesList.toString());
            if(mMessagesList.size() == 0) {
                mNotice.setText("No follow up messages. You can send message by writing below.");
                mNoticeButtons.setVisibility(View.INVISIBLE);
            } else {
                mNotice.setText("");
            }
        }
    }


    private boolean prepareAndSend() {
        // get message string
        String content = mChatText.getText().toString();

        // check if message is empty
        if (content.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Message is empty!",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // add to list
        String fromAdmin = "0";
        String timestamp = new Date().toString();
        ChatMessage message = new ChatMessage(fromAdmin, content, timestamp, mReportId);
        mMessagesList.add(message);

        // save to local db
        DatabaseBook.getChatBook(this).addMessage(message);
        updateMessagesList();

        // send to server
        if(!mReport.isAnon()) {
            sendMessage(content);
        } else {
            sendMessageThroughTor(content);
        }

        // reset edit text
        mChatText.setText("");

        // scroll to bottom of screen
        mLayoutManager.scrollToPosition(mMessagesList.size() - 1);

        return true;
    }

    /**
     * Starts background task for resending report
     */
    private void resendReport() {
        PrepareReSendTask job = new PrepareReSendTask();
        job.execute();
    }

    /**
     * Sends message through tor in the background
     * @param message message to be sent
     */
    private void sendMessageThroughTor(String message) {
        if(!mTorReady && !mTorInitializing) {
            mTorInitializing = true;
            InitializeTor job1 = new InitializeTor();
            job1.execute();
        }
        MessageThroughTor job2 = new MessageThroughTor();
        job2.execute(message);
    }

    /**
     * Process received messages
     */
    private void processMessages(String response) {
        try {
            // get json object
            JSONObject jsonObj = new JSONObject(response);

            // get json array of messages
            JSONArray jsonarray = jsonObj.getJSONArray("messages");

            // delete existing messages
            DatabaseBook.getChatBook(getApplicationContext()).deleteMessages(mReportId);

            // add new messages to db
            for (int j = 0; j < jsonarray.length(); j++) {
                // get message
                JSONObject m = jsonarray.getJSONObject(j);
                String timestamp = m.getString("timestamp");
                String content = m.getString("content");
                String from_admin = m.getInt("from_admin") + "";

                // decrypt message
                String prvKey = mReport.getPrvKey();
                content = ApplicationContext.getInstance().decryptForUser(content, prvKey);

                // create Message object and add to db
                ChatMessage message = new ChatMessage(from_admin, content, timestamp, mReportId);
                DatabaseBook.getChatBook(getApplicationContext()).addMessage(message);
            }

            // update list to refresh UI
            updateMessagesList();
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Gets followup messages from rddp server and stores in local db
     */
    private void getMessages() {
        // get url
        String url = URL.GET_FOLLOW_UP_MESSAGES;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                processMessages(response);
            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("report_id", mReportId);
                return MyData;
            }
        };

        // set timeout and retry params
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // add to queue
        requestQueue.add(stringRequest);
    }



    private void sendMessage(final String message) {
        // get url
        String url = URL.SEND_FOLLOW_UP_MESSAGE;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                processMessages(response);
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
                    String message_user = ApplicationContext.getInstance().encryptForUser(
                            message, user_public_key_pem);
                    String message_admin = ApplicationContext.getInstance().encryptForAdmin(
                            message, mAdminPublicKey);
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

    private class InitializeTor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            Log.d(TAG, "Initializing tor");

            mOnionProxyManager = new AndroidOnionProxyManager(getApplicationContext(), "torr");

            int totalSecondsPerTorStartup = 4 * 60;
            int totalTriesPerTorStartup = 5;
            try {
                boolean ok = mOnionProxyManager.startWithRepeat(totalSecondsPerTorStartup,
                        totalTriesPerTorStartup);
                if (!ok)
                    Log.e("TorTest", "Couldn't start Tor!");

                while (!mOnionProxyManager.isRunning())
                    Thread.sleep(90);

                Log.v("TorTest", "Tor initialized on port " +
                        mOnionProxyManager.getIPv4LocalHostSocksPort());


                mHttpclient = getNewHttpClient();
                int port = mOnionProxyManager.getIPv4LocalHostSocksPort();
                InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", port);
                mHttpContext = HttpClientContext.create();
                mHttpContext.setAttribute("socks.address", socksaddr);

                mTorReady = true;
                Log.d(TAG, "Tor initialized");

            }
            catch (Exception e) {
                Log.d(TAG, "Tor initialization exception");
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    private class GetMessagesThroughTor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            try {
                while(!mTorReady) {
                    Thread.sleep(90);
                }
                Log.d(TAG, "Tor is ready");

                // get URL
                HttpPost httpost = new HttpPost(URL.GET_FOLLOW_UP_MESSAGES);

                // encrypt data and add to hashmap
                Map<String, String> MyData = new HashMap<>();
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

                // execute request
                HttpResponse response;
                response = mHttpclient.execute(httpost, mHttpContext);
                Log.i(TAG, "Tor response status: " + response.getStatusLine().toString());

                // get response entity
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String result = Utilities.convertStreamToString(instream);
                    Log.d(TAG, "Tor response result: " + result);
                    processMessages(result);
                    instream.close();
                }
                Log.d(TAG, "Done sending");
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String message) {
            //
        }
    }

    private class MessageThroughTor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            try {
                Log.d(TAG, "Sending through tor");
                while(!mTorReady) {
                    Thread.sleep(90);
                }
                Log.d(TAG, "Tor is ready");

                // get URL
                HttpPost httpost = new HttpPost(URL.SEND_FOLLOW_UP_MESSAGE);

                // encrypt data and add to hashmap
                Map<String, String> MyData = new HashMap<>();
                String message = params[0];
                String user_public_key_pem = mReport.getPubKey();
                try {
                    String message_user = ApplicationContext.getInstance().encryptForUser(
                            message, user_public_key_pem);
                    String message_admin = ApplicationContext.getInstance().encryptForAdmin(
                            message, mAdminPublicKey);
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

                // execute request
                HttpResponse response;
                response = mHttpclient.execute(httpost, mHttpContext);
                Log.i(TAG, "Tor response status: " + response.getStatusLine().toString());

                // get response entity
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String result= Utilities.convertStreamToString(instream);
                    Log.d(TAG, "Tor response result: " + result);
                    processMessages(result);
                    instream.close();
                }
                Log.d(TAG, "Done sending");
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String message) {
            updateMessagesList();
        }
    }

    /**
     * Background task for resending report if it failed
     */
    public class PrepareReSendTask extends AsyncTask<String, Void, Boolean> {

        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(FollowupChatActivity.this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Report is being resent. Check followup tab for updates");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.ic_check_black_24dp));
            mProgressDialog.show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mProgressDialog.dismiss();
                    FollowupChatActivity.this.finish();
                }
            }, 3000);
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }

        protected Boolean doInBackground(final String... args) {
            try {
                HashMap<String, String> data = new HashMap();

                // chagne status to sending
                String status = "Sending";
                DatabaseBook.getChatBook(getApplicationContext()).updateReportStatus(mReportId, status);

                // get data from db
                data.put("public_key", mReport.getPubKey());
                data.put("report_id", mReportId);
                data.put("type", mReport.getTitle());
                data.put("urgency", mReport.getUrgency());
                data.put("timestamp", mReport.getTimestamp());
                data.put("location", mReport.getLocation());
                data.put("description", mReport.getDescription());
                data.put("is_anonymous", mReport.isAnon() + "");
                data.put("is_resp_emp", mReport.getIsResp());
                data.put("follow_up_enabled", mReport.getIsFollowup());

                // TODO: currently there is no way to include images in resend
                ArrayList<String > mImagePathList = new ArrayList<>();

                SendReportService.startSendingReport(getApplicationContext(), data, mImagePathList, mReport.isAnon());

            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }

    public HttpClient getNewHttpClient() {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(
                        SSLContexts.createSystemDefault()))
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }
}
