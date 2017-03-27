package com.trinreport.m.app.report;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;
import com.msopentech.thali.toronionproxy.OnionProxyManager;
import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.model.ChatKey;
import com.trinreport.m.app.tor.MyConnectionSocketFactory;
import com.trinreport.m.app.tor.MySSLConnectionSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
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
import cz.msebera.android.httpclient.ssl.SSLContexts;

public class AddReportActivity extends AppCompatActivity implements  DatePickerFragment.OnCompleteDateListener, TimePickerFragment.OnCompleteTimeListener{

    // constants
    private static final String TAG = "AddReportFragment";

    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_UPLOAD = 3;

    // layout references
    private Toolbar mToolbar;
    private RadioGroup mRadioGroup;
    private Button mDateButton;
    private Button mTimeButton;
    private EditText mLocationText;
    private EditText mTypeText;
    private EditText mDescriptionText;
    private CheckBox mAnonymousCheckbox;
    private CheckBox mFollowupCheckbox;
    private CheckBox mResEmployeeCheckbox;
    private Button mSubmitbutton;

    private RecyclerView mPhotoRecyclerView;
    private RecyclerView.Adapter mAdapter;
    LinearLayoutManager mLayoutManager;
    private List<String> mImagePathList;
    private String mCurrentPhotoPath;

    // Form inputs
    private String mUrgency;
    private Date mDate;
    private String mLocation;
    private String mType;
    private String mDescription;
    private boolean mIsAnonymous;
    private boolean mFollowupEnabled;
    private boolean mResEmployeeChecked;

    private SharedPreferences mSharedPreferences;
    private String mAdminPublicKey;

    // tor client
    private HttpClient mHttpclient;
    private boolean mTorInitialized;
    private HttpClientContext mHttpContext;

    public AddReportActivity() {
        mUrgency = "medium";
        mDate = new Date();
        mLocation = "n/a";
        mType = "n/a";
        mDescription = "n/a";
        mIsAnonymous = false;
        mFollowupEnabled = true;
        mResEmployeeChecked = false;

        mTorInitialized = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_report);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_urgency);
        mDateButton = (Button) findViewById(R.id.report_date);
        mTimeButton = (Button) findViewById(R.id.report_time);
        mLocationText = (EditText) findViewById(R.id.edit_text_location);
        mTypeText = (EditText) findViewById(R.id.edit_text_type);
        mDescriptionText = (EditText) findViewById(R.id.edit_text_describe);
        mAnonymousCheckbox = (CheckBox) findViewById(R.id.checkbox_anonymous);
        mFollowupCheckbox = (CheckBox) findViewById(R.id.checkbox_followup);
        mResEmployeeCheckbox = (CheckBox) findViewById(R.id.checkbox_responsible);
        mSubmitbutton = (Button) findViewById(R.id.button_submit);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAdminPublicKey = mSharedPreferences.getString("admin_public_key", "");


        // setup toolbar
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });

        // initialize tor client
        //initTor();

        // update time
        updateDate();

        // setup list, adapter and recyclerview for selected images
        mImagePathList = new ArrayList<>();
        String imageUri = "drawable://" + R.drawable.add2;
        mImagePathList.add(imageUri);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mPhotoRecyclerView = (RecyclerView) findViewById(R.id.images_recycler_view);

        mPhotoRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PhotoAdapter();
        mPhotoRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // urgency radio button
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was clicked
                switch(checkedId) {
                    case R.id.radio_high:
                        mUrgency = "high";
                        break;
                    case R.id.radio_medium:
                        mUrgency = "medium";
                        break;
                    case R.id.radio_low:
                        mUrgency = "low";
                        break;
                }
            }
        });

        // date picker
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mDate);
                //dialog.setTargetFragment(AddReportFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        // time picker
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mDate);
                //dialog.setTargetFragment(AddReportFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        // location text
        mLocationText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLocation = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // description text
        mDescriptionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDescription = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // type of incident text
        mTypeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mType = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //
        mAnonymousCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsAnonymous = isChecked;
            }
        });

        mFollowupCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFollowupEnabled = isChecked;
            }
        });

        mResEmployeeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mResEmployeeChecked = isChecked;
            }
        });


        mSubmitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsAnonymous) {
                    sendReportAnonymous();
                } else {
                    sendReportNonAnonymous();
                }

            }
        });
    }

    // call back from time picker fragment
    public void onCompleteTime(Date date) {
        mDate = date;
        updateDate();
    }

    // call back from date picker fragment
    public void onCompleteDate(Date date) {
        mDate = date;
        updateDate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }

        if(requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mDate = date;
            updateDate();
        }

        if(requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mDate = date;
            updateDate();
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);

            //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, null);
            //mImageView.setImageBitmap(bitmap);
            //setPic(mCurrentPhotoPath, mImageView);
            //mImagePathList.add(mCurrentPhotoPath);
            //mAdapter.notifyDataSetChanged();
        }
        if (requestCode == REQUEST_IMAGE_UPLOAD && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            String path = getRealPathFromURI_API11to18(this, uri);
            //setPic(path, mImageView);
            mImagePathList.add(path);
            mAdapter.notifyDataSetChanged();
            /*try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance().format(mDate));
        mTimeButton.setText(DateFormat.getTimeInstance().format(mDate));
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic(String photoPath, ImageView imageView) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // TODO: remove this
        targetW = 120;
        targetH = 160;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    private void dispatchTakePictureIntent2() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispathUploadPictureIntent() {
        Intent uploadPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        uploadPictureIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(uploadPictureIntent, "Select Picture"), REQUEST_IMAGE_UPLOAD);
    }


    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.image_gallery);
        }


        public void bindDrawable(final int Position) {
            if(Position == 0) {
                mItemImageView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.add2));
                mItemImageView.setPadding(1,1,1,1);
                mItemImageView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.colorPrimary));
                // onclick listener that opens ViewPager acticity when clicked on an image
                mItemImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dispathUploadPictureIntent();
                    }
                });
                return;
            }
            // get path to image file
            String path = mImagePathList.get(Position);

            // display image
            if (path == null) {
                //mItemImageView.setImageDrawable(null);
            } else {
                setPic(path, mItemImageView);
            }

        }
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        public PhotoAdapter() {
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = inflater.inflate(R.layout.image_gallery, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            photoHolder.bindDrawable(position);
        }

        @Override
        public int getItemCount() {
            return mImagePathList.size();
        }
    }

    private void initTor() {
        InitializeTor job = new InitializeTor();
        job.execute();
    }
    private void sendReportAnonymous() {
        ReportThroughTor job = new ReportThroughTor();
        job.execute();
    }

    private void sendReportNonAnonymous() {
        // get url
        String url = URL.SEND_REPORT;

        // create request
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get report id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    String report_id = jsonObj.get("report_id").toString();
                    addReportToList(report_id);

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap();

                String auth_token = mSharedPreferences.getString("auth_token", "");
                MyData.put("auth_token", auth_token);
                MyData.put("type", mType);
                MyData.put("urgency", mUrgency );
                MyData.put("year", 1900 + mDate.getYear() +  "" );
                MyData.put("month", 1 + mDate.getMonth() +  "" );
                MyData.put("day", mDate.getDate() + "");
                MyData.put("hour", mDate.getHours() +  "" );
                MyData.put("minute", mDate.getMinutes() +  "" );
                MyData.put("location", mLocation);
                MyData.put("description", mDescription);
                MyData.put("is_anonymous", mIsAnonymous + "");
                MyData.put("is_resp_emp", mResEmployeeChecked + "");
                MyData.put("follow_up_enabled", mFollowupEnabled + "");

                try{
                    //////
                    // generate RSA keys
                    KeyPair keyPair = RSA.generateRsaKeyPair(2048);
                    String privateKeyPem = RSA.createStringFromPrivateKey(keyPair.getPrivate());
                    String publicKeyPem = RSA.createStringFromPublicKey(keyPair.getPublic());

                    // generate cookie
                    String reportId = generateCookie();
                    ChatBook.getChatBook(getApplicationContext()).addChatKey(
                            new ChatKey(reportId, privateKeyPem)
                    );
                    MyData.put("public_key", publicKeyPem);
                    MyData.put("report_id", reportId);
                } catch (Exception e) {

                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                MyData.put("username", prefs.getString("username", ""));
                MyData.put("userdorm", prefs.getString("userdorm", ""));
                MyData.put("useremail", prefs.getString("useremail", ""));
                MyData.put("userphone", prefs.getString("userphone", ""));
                MyData.put("userid", prefs.getString("userid", ""));


                //// test encrypted image uplaod
                Bitmap bitmap = BitmapFactory.decodeFile(mImagePathList.get(1));
                String image = getStringImage(bitmap);
                
                try {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    SecureRandom random = new SecureRandom();
                    byte[] keyBytes = generateKey(random);
                    byte[] ivBytes = generateIV(cipher, random);
                    SecretKey key = new SecretKeySpec(keyBytes, "AES");
                    String cipherImage = encryptAES(cipher, key, ivBytes, image);
                    String key_str = Base64.encodeToString(keyBytes, Base64.DEFAULT);
                    String iv_str = Base64.encodeToString(ivBytes, Base64.DEFAULT);

                    MyData.put("image", cipherImage);
                    MyData.put("image_key", key_str);
                    MyData.put("image_iv", iv_str);
                } catch (Exception e ) {

                }


                ///// end test
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }

    // source: https://www.simplifiedcoding.net/android-volley-tutorial-to-upload-image-to-server/
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
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

    private String encrypt(String plain) throws Exception {
        return RSA.encrypt(plain, mAdminPublicKey);
    }

    private class ReportThroughTor extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            try {
                while(!mTorInitialized) {
                    Thread.sleep(90);
                }
                //url with the post data
                HttpPost httpost = new HttpPost(URL.SEND_REPORT);

                //convert parameters into JSON object
                Map<String, String> MyData = new HashMap();
                //String auth_token = mSharedPreferences.getString("auth_token", "");
                //MyData.put("auth_token", auth_token);
                MyData.put("type", mType);
                MyData.put("urgency", mUrgency );
                MyData.put("year", 1900 + mDate.getYear() +  "" );
                MyData.put("month", 1 + mDate.getMonth() +  "" );
                MyData.put("day", mDate.getDate() + "");
                MyData.put("hour", mDate.getHours() +  "" );
                MyData.put("minute", mDate.getMinutes() +  "" );
                MyData.put("location", mLocation);
                MyData.put("description", mDescription);
                MyData.put("is_anonymous", mIsAnonymous + "");
                MyData.put("is_resp_emp", mResEmployeeChecked + "");
                MyData.put("follow_up_enabled", mFollowupEnabled + "");

                //////
                // generate RSA keys
                KeyPair keyPair = RSA.generateRsaKeyPair(2048);
                String privateKeyPem = RSA.createStringFromPrivateKey(keyPair.getPrivate());
                String publicKeyPem = RSA.createStringFromPublicKey(keyPair.getPublic());

                // generate cookie
                String reportId = generateCookie();
                ChatBook.getChatBook(getApplicationContext()).addChatKey(
                        new ChatKey(reportId, privateKeyPem)
                );
                MyData.put("public_key", publicKeyPem);
                MyData.put("report_id", reportId);
                //////

                JSONObject holder = new JSONObject(MyData);

                //passes the results to a string builder/entity
                StringEntity se = new StringEntity(holder.toString());

                //sets the post request as the resulting string
                httpost.setEntity(se);
                //sets a request header so the page receving the request
                //will know what to do with it
                httpost.setHeader("Accept", "application/json");
                httpost.setHeader("Content-type", "application/json");

                //Handles what is returned from the page
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
                    addReportToList(report_id);


                    Log.d("TorTest", "Result: " + result);
                    // now you have the string representation of the HTML request
                    instream.close();
                }
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

    private void addReportToList(String reportId) {
        // get list
        Set<String> reportsSet = mSharedPreferences
                .getStringSet("reports_set", new HashSet<String>());
        List<String> reports = new ArrayList<>(reportsSet);
        // add report id to list
        reports.add(reportId);
        // commit to shared prefs
        reportsSet = new HashSet<>(reports);
        mSharedPreferences
                .edit()
                .putStringSet("reports_set", reportsSet)
                .apply();
    }

    private String generateCookie() {
        try {
            // generate thread cookie
            String input = (new Date()).toString(); // using timestamp for now
            MessageDigest digest;
            String cookie;
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes());
            cookie = bytesToHexString(digest.digest());
            Log.i(TAG, "Hash is " + cookie);
            return cookie;

        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        }
        return "";
    }


    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private byte[] generateKey(SecureRandom random) {
        try {
            String password  = "password";
            int iterationCount = 1000;
            int keyLength = 256;
            int saltLength = keyLength / 8; // same size as key output
            byte[] salt = new byte[saltLength];
            random.nextBytes(salt);
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                    iterationCount, keyLength);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

            return keyBytes;
        } catch(Exception e) {

        }
        return null;
    }

    private byte[] generateIV(Cipher cipher, SecureRandom random) {
        try  {
            byte[] iv = new byte[cipher.getBlockSize()];
            random.nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            return iv;
        } catch (Exception e) {

        }
        return null;
    }

    private String encryptAES(Cipher cipher, SecretKey key, byte[] iv, String plaintext) {
        try {
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            byte[] encodedBytes = Base64.encode(cipher.doFinal(plaintext.getBytes("UTF-8")), Base64.DEFAULT);
            String cipherText = new String(encodedBytes, "UTF-8");
            return cipherText;
        } catch (Exception e) {

        }
        return null;
    }
}
