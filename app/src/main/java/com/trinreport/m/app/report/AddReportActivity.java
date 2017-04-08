package com.trinreport.m.app.report;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
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

import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;
import com.trinreport.m.app.model.Report;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;


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
    private LinearLayoutManager mLayoutManager;
    private ProgressDialog mProgressDialog;

    // form inputs
    private String mUrgency;
    private Date mDate;
    private String mLocation;
    private String mType;
    private String mDescription;
    private boolean mIsAnonymous;
    private boolean mFollowupEnabled;
    private boolean mResEmployeeChecked;

    // other references
    private ArrayList<String> mImagePathList;
    private String mCurrentPhotoPath;
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
            getSupportActionBar().setTitle("New Report");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

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
            sendReport();
            }
        });
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
        if (requestCode == REQUEST_IMAGE_UPLOAD && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            String path = getRealPathFromURI_API11to18(this, uri);
            mImagePathList.add(path);
            mAdapter.notifyDataSetChanged();
        }
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

    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance().format(mDate));
        mTimeButton.setText(DateFormat.getTimeInstance().format(mDate));
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

    private void sendReport() {
        PrepareSendTask job = new PrepareSendTask();
        job.execute();
    }

    public class PrepareSendTask extends AsyncTask<String, Void, Boolean> {

        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(AddReportActivity.this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Please wait...");
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                mProgressDialog = new ProgressDialog(AddReportActivity.this);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setMessage("Your report is being sent. Check Followup tab for updates");
                mProgressDialog.setCancelable(false);
                mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.ic_check_black_24dp));
                mProgressDialog.show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        AddReportActivity.this.finish();
                    }
                }, 3000);
            }
        }

        protected Boolean doInBackground(final String... args) {
            try {
                HashMap<String, String> data = new HashMap();

                // generate RSA keys
                KeyPair keyPair = RSA.generateRsaKeyPair(2048);
                String privateKeyPem = RSA.createStringFromPrivateKey(keyPair.getPrivate());
                String publicKeyPem = RSA.createStringFromPublicKey(keyPair.getPublic());

                // generate cookie
                String reportId = generateCookie();

                // save in local db
                String is_anon = (mIsAnonymous)? "1": "0";
                String status = "Sending";
                ChatBook.getChatBook(getApplicationContext()).addReport(
                        new Report(reportId,
                                privateKeyPem,
                                mType,
                                publicKeyPem,
                                System.currentTimeMillis(), is_anon, status)
                );

                // add keys to hashmap
                data.put("public_key", publicKeyPem);
                data.put("report_id", reportId);

                // format date
                Format formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                String timestamp = formatter.format(mDate);

                // add form data to hashmap
                data.put("type", mType);
                data.put("urgency", mUrgency);
                data.put("timestamp", timestamp);
                data.put("location", mLocation);
                data.put("description", mDescription);
                data.put("is_anonymous", mIsAnonymous + "");
                data.put("is_resp_emp", mResEmployeeChecked + "");
                data.put("follow_up_enabled", mFollowupEnabled + "");

                // save report form to db
                ChatBook.getChatBook(getApplicationContext()).addReportForm(
                        reportId,
                        mUrgency,
                        timestamp,
                        mLocation,
                        mDescription,
                        mResEmployeeChecked + "",
                        mFollowupEnabled + ""
                );

                SendReportService.startSendingReport(getApplicationContext(), data, mImagePathList, mIsAnonymous);

            } catch (Exception e) {
                return false;
            }
            return true;
        }
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
}
