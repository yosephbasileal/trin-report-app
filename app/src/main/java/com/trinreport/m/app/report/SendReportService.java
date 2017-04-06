package com.trinreport.m.app.report;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
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
import com.trinreport.m.app.AES;
import com.trinreport.m.app.ApplicationContext;
import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.tor.MyConnectionSocketFactory;
import com.trinreport.m.app.tor.MySSLConnectionSocketFactory;
import com.trinreport.m.app.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

public class SendReportService extends IntentService {
    private static String TAG = "SendReportService";
    private static int MY_SOCKET_TIMEOUT_MS = 15000; // 15 seconds

    private static final String EXTRA_REPORT_HASHMAP = "com.trinreport.m.app.extra.FORMDATA";
    private static final String EXTRA_REPORT_IMAGELIST = "com.trinreport.m.app.extra.IMAGEDATA";
    private static final String EXTRA_REPORT_ISANON = "com.trinreport.m.app.extra.ISANON";

    private boolean mIsAnon;
    private HashMap<String, String> mReportData;
    private HashMap<String, String> mEncryptedData;
    private ArrayList<String> mImagesPathList;
    private String mReportId;

    private SharedPreferences mSharedPreferences;
    private String mAdminPublicKey;

    private ApplicationContext mApplicationContext;

    // tor client
    private HttpClient mHttpclient;
    private HttpClientContext mHttpContext;
    private boolean mTorReady;

    public static void startSendingReport(Context context, HashMap<String, String> data, ArrayList<String> imagesList, boolean isAnon) {
        Intent intent = new Intent(context, SendReportService.class);
        intent.putExtra(EXTRA_REPORT_HASHMAP, data);
        intent.putExtra(EXTRA_REPORT_ISANON, isAnon);
        intent.putStringArrayListExtra(EXTRA_REPORT_IMAGELIST, imagesList);
        context.startService(intent);
        Log.d(TAG, "SendReportService service started");
    }

    public SendReportService() {
        super("SendReportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mAdminPublicKey = mSharedPreferences.getString("admin_public_key", "");
            mApplicationContext = ApplicationContext.getInstance();
            mEncryptedData = new HashMap<>();

            mIsAnon = intent.getBooleanExtra(EXTRA_REPORT_ISANON, false);
            mReportData = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_REPORT_HASHMAP);
            mImagesPathList = intent.getStringArrayListExtra(EXTRA_REPORT_IMAGELIST);
            mReportId = mReportData.get("report_id");

            Log.d(TAG, "report id: " + mReportId);
            Log.d(TAG, "report images: " + mImagesPathList.toString());
            Log.d(TAG, "report data: " + mReportData.toString());

            // encrypt data
            encrypt();

            // send data to server
            if(!mIsAnon) {
                // send
                sendReport(mEncryptedData);
            } else {
                // init tor
                mTorReady = false;
                initTor();
                // send
                if(mTorReady) {
                    sendReportTor();
                }
            }
        }
    }

    private void encrypt() {
        try {
            // add keys
            mEncryptedData.put("public_key", mReportData.get("public_key"));
            mEncryptedData.put("report_id", mReportData.get("report_id"));

            // encrypt form inputs
            mEncryptedData.put("type", mApplicationContext.encryptForAdmin(
                    mReportData.get("type"), mAdminPublicKey));
            mEncryptedData.put("urgency", mApplicationContext.encryptForAdmin(
                    mReportData.get("urgency"), mAdminPublicKey));
            mEncryptedData.put("location", mApplicationContext.encryptForAdmin(
                    mReportData.get("location"), mAdminPublicKey));
            mEncryptedData.put("description", mApplicationContext.encryptForAdmin(
                    mReportData.get("description"), mAdminPublicKey));

            // add other data
            mEncryptedData.put("timestamp", mReportData.get("timestamp"));
            mEncryptedData.put("mIsAnonymous", mReportData.get("is_anonymous"));
            mEncryptedData.put("mResEmployeeChecked", mReportData.get("is_resp_emp"));
            mEncryptedData.put("mFollowupEnabled", mReportData.get("follow_up_enabled"));

            // add reporter data if not anonymous
            if(!mIsAnon) {
                mEncryptedData.put("username", mApplicationContext.encryptForAdmin(
                        mSharedPreferences.getString("username", "n/a"), mAdminPublicKey));
                mEncryptedData.put("userphone", mApplicationContext.encryptForAdmin(
                        mSharedPreferences.getString("userphone", "n/a"), mAdminPublicKey));
                mEncryptedData.put("userid", mApplicationContext.encryptForAdmin(
                        mSharedPreferences.getString("userid", "n/a"), mAdminPublicKey));
                mEncryptedData.put("useremail", mApplicationContext.encryptForAdmin
                        (mSharedPreferences.getString("useremail", "n/a"), mAdminPublicKey));
                mEncryptedData.put("userdorm", mApplicationContext.encryptForAdmin(
                        mSharedPreferences.getString("userdorm", "n/a"), mAdminPublicKey));
            }

            // encrypt and add images to map
            // image upload, allows only 3
            int imageCount = mImagesPathList.size() - 1;
            if (imageCount > 3) {
                imageCount = 3;
            }
            mEncryptedData.put("images_count", imageCount + "");
            if(imageCount > 0) {
                byte[] keyBytes = AES.generateKey();
                byte[] ivBytes = AES.generateIV();

                for(int i = 1; i <= imageCount; i++) {
                    String path = mImagesPathList.get(i);
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    String image_str = Utilities.getStringImage(bitmap);
                    SecretKey key = new SecretKeySpec(keyBytes, "AES");
                    String cipherImage = AES.encryptAES(key, ivBytes, image_str);
                    mEncryptedData.put("image"+i, cipherImage);
                }

                String key_str = Base64.encodeToString(keyBytes, Base64.DEFAULT);
                String iv_str = Base64.encodeToString(ivBytes, Base64.DEFAULT);
                mEncryptedData.put("images_key", key_str);
                mEncryptedData.put("images_iv", iv_str);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            recordFailure();
        }
    }

    private void sendReport(final HashMap <String, String> data) {
        // get url
        String url = URL.SEND_REPORT;

        // create request
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getApplicationContext());

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get status and send broadcast intent
                    JSONObject jsonObj = new JSONObject(response);
                    boolean succeeded = jsonObj.getBoolean("succeeded");
                    if(succeeded) {
                        recordSucess();
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                recordFailure();
            }
        }) {
            protected Map<String, String> getParams() {
                return data;
            }
        };

        MyStringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MyRequestQueue.add(MyStringRequest);
    }

    private void initTor() {
        OnionProxyManager onionProxyManager = new AndroidOnionProxyManager(getApplicationContext(), "torr");

        int totalSecondsPerTorStartup = 4 * 60;
        int totalTriesPerTorStartup = 5;
        try {
            boolean ok = onionProxyManager.startWithRepeat(totalSecondsPerTorStartup, totalTriesPerTorStartup);
            if (!ok)
                Log.e(TAG, "Couldn't start Tor!");

            while (!onionProxyManager.isRunning())
                Thread.sleep(90);

            Log.v(TAG, "Tor initialized on port " + onionProxyManager.getIPv4LocalHostSocksPort());


            mHttpclient = getNewHttpClient();
            int port = onionProxyManager.getIPv4LocalHostSocksPort();
            InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", port);
            mHttpContext = HttpClientContext.create();
            mHttpContext.setAttribute("socks.address", socksaddr);

            mTorReady = true;

        }
        catch (Exception e) {
            e.printStackTrace();
            recordFailure();
        }
    }

    private void sendReportTor() {
        try {
            // get URL
            HttpPost httpost = new HttpPost(URL.SEND_REPORT);

            // convert hashmap to json object
            JSONObject holder = new JSONObject(mEncryptedData);

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
                JSONObject jsonObj = new JSONObject(result);
                boolean succeeded = jsonObj.getBoolean("succeeded");
                if(succeeded) {
                    recordSucess();
                }
                instream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            recordFailure();
        }
    }

    private void recordFailure() {
        ChatBook.getChatBook(getApplicationContext()).updateReportStatus(mReportId, "Failed to send");
    }

    private void recordSucess() {
        ChatBook.getChatBook(getApplicationContext()).updateReportStatus(mReportId, "Sent");
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
}
