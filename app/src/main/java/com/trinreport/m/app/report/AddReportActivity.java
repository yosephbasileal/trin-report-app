package com.trinreport.m.app.report;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
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
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.RSA;
import com.trinreport.m.app.model.Report;
import com.trinreport.m.app.utils.Utilities;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class AddReportActivity extends AppCompatActivity implements
        DatePickerFragment.OnCompleteDateListener, TimePickerFragment.OnCompleteTimeListener{

    // constants
    private static final String TAG = "AddReportFragment";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_IMAGE_UPLOAD = 3;
    private static final int MAX_NUM_IMAGES = 3;

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
    private ProgressDialog mProgressDialog;
    private ProgressBar mLoadingMarker;
    private TextView mErrorText;

    // other references
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    // form inputs
    private String mUrgency;
    private Date mDate;
    private String mLocation;
    private String mType;
    private String mDescription;
    private boolean mIsAnonymous;
    private boolean mFollowupEnabled;
    private boolean mResEmployeeChecked;

    // other variables
    private ArrayList<String> mImagePathList;
    private String mReportId;


    /**
     * Constructor
     */
    public AddReportActivity() {
        mReportId = "";
        mUrgency = "medium";
        mDate = new Date();
        mLocation = "n/a";
        mType = "n/a";
        mDescription = "n/a";
        mIsAnonymous = false;
        mFollowupEnabled = true;
        mResEmployeeChecked = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate layout
        setContentView(R.layout.activity_add_report);

        // get references
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
        mPhotoRecyclerView = (RecyclerView) findViewById(R.id.images_recycler_view);
        mLoadingMarker = (ProgressBar) findViewById(R.id.marker_submit_report);
        mErrorText = (TextView) findViewById(R.id.error_report);

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

        // update time to current time
        updateDate();

        // setup list for selected images
        mImagePathList = new ArrayList<>();
        String imageUri = "drawable://" + R.drawable.add2; // first image is an add button
        mImagePathList.add(imageUri);

        // setup recycler view and adapter
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mPhotoRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PhotoAdapter();
        mPhotoRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // urgency radio button change listener
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

        // date picker listener
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mDate);
                //dialog.setTargetFragment(AddReportFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        // time picker listener
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mDate);
                //dialog.setTargetFragment(AddReportFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        // location text listener
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

        // description text listener
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

        // type of incident text listener
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

        // anonymous checkbox listener
        mAnonymousCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsAnonymous = isChecked;
            }
        });

        // followup checkbox listener
        mFollowupCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFollowupEnabled = isChecked;
            }
        });

        // responsible employee listener
        mResEmployeeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mResEmployeeChecked = isChecked;
            }
        });

        // submit button listener
        mSubmitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReport();
            }
        });
    }

    /**
     * Fires when activities started for result send data
     * @param requestCode
     * @param resultCode
     * @param data
     */
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
        if (requestCode == REQUEST_IMAGE_UPLOAD
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            Uri uri = data.getData();
            String path = getRealPathFromURI_API11to18(this, uri);
            mImagePathList.add(path);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Shows loading marker next to the submit button
     */
    private void showLoadingMarker() {
        mSubmitbutton.setEnabled(false);
        mLoadingMarker.setVisibility(View.VISIBLE);
    }

    /**
     * Hides loading marker next to the submit button
     */
    private void hideLoadingMarker() {
        mSubmitbutton.setEnabled(true);
        mLoadingMarker.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows error under edit text view
     * @param error error message to show
     */
    private void showError(String error) {
        mErrorText.setText(error);
    }

    /**
     * Call back from time picker fragment
     * @param date picked date
     */
    public void onCompleteTime(Date date) {
        mDate = date;
        updateDate();
    }

    /**
     * Call back from date picker fragment
     * @param date picked date
     */
    public void onCompleteDate(Date date) {
        mDate = date;
        updateDate();
    }

    /**
     * Updates date string on screen
     */
    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance().format(mDate));
        mTimeButton.setText(DateFormat.getTimeInstance().format(mDate));
    }

    /**
     * Starts an asynchronous task to send report
     */
    private void sendReport() {
        PrepareSendTask job = new PrepareSendTask();
        job.execute();
    }

    /**
     * Opens image upload dialog
     */
    private void dispathUploadPictureIntent() {
        Intent uploadPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        uploadPictureIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(uploadPictureIntent, "Select Picture"),
                REQUEST_IMAGE_UPLOAD);
    }

    /**
     * Generates a thread ID using sha256
     * @return thread_id string
     */
    private String generateThreadID() {
        String thread_id = "";
        try {
            // generate thread id
            String input = (new Date()).toString(); // using timestamp
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes());
            thread_id = Utilities.bytesToHexString(digest.digest());
            Log.i(TAG, "Hash is " + thread_id);
        } catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        }
        return thread_id;
    }

    /**
     * Converts image content URI to absolute path
     * @param context application context
     * @param contentUri uri of image
     * @return
     */
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

    /**
     * Binds an image to image view
     * @param photoPath absolute path of image
     * @param imageView image view to use for display
     */
    private void setPic(String photoPath, ImageView imageView) {
        // set the dimensions of the view
        // Remark: hardcoded values should be changed in future iterations
        int targetW = 120;
        int targetH = 160;

        // get dimensions of bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // decode the image file into a bitmap sized to fill the view
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        // set bitmap to image view
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }


    /**
     * View holder for recycler view
     */
    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.image_gallery);
        }


        public void bindDrawable(final int Position) {
            // the first image is the add button, so has a separate logic
            if(Position == 0) {
                mItemImageView.setImageDrawable(getApplicationContext()
                        .getResources().getDrawable(R.drawable.add2));
                mItemImageView.setPadding(1,1,1,1);
                mItemImageView.setBackgroundColor(getApplicationContext()
                        .getResources().getColor(R.color.colorPrimary));

                // add click lisenter to open dialog up upload picture
                mItemImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mImagePathList.size() -1 >= MAX_NUM_IMAGES) {
                            Toast.makeText(getApplicationContext(),
                                    "A maximum of " + MAX_NUM_IMAGES + " images allowed",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dispathUploadPictureIntent();
                    }
                });
                return;
            }
            // for all other positions, get path to image file
            String path = mImagePathList.get(Position);

            // display image
            if (path != null) {
                setPic(path, mItemImageView);
            }
        }
    }


    /**
     * Adapter for recycler view
     */
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


    /**
     * Background task for preparing to send report to rddp service
     */
    public class PrepareSendTask extends AsyncTask<String, Void, Boolean> {

        protected void onPreExecute() {
            showLoadingMarker();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            hideLoadingMarker();
            if(success) {
                /*mProgressDialog.dismiss();
                mProgressDialog = null;*/
                mProgressDialog = new ProgressDialog(AddReportActivity.this);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setMessage("Report is being sent. Check followup tab for updates");
                mProgressDialog.setCancelable(false);
                mProgressDialog.setIndeterminateDrawable(getResources().
                        getDrawable(R.drawable.ic_check_black_24dp));
                mProgressDialog.show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        AddReportActivity.this.finish();
                    }
                }, 3000);
            } else {
                // show error
                showError("Something when wrong! Try again.");
                // delete report from db if created
                ChatBook.getChatBook(getApplicationContext()).deleteReport(mReportId);
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
                mReportId = generateThreadID();

                // save in local db
                String is_anon = (mIsAnonymous)? "1": "0";
                String status = "Sending";
                ChatBook.getChatBook(getApplicationContext()).addReport(
                        new Report(mReportId,
                                privateKeyPem,
                                mType,
                                publicKeyPem,
                                System.currentTimeMillis(), is_anon, status)
                );

                // add keys to hashmap
                data.put("public_key", publicKeyPem);
                data.put("report_id", mReportId);

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
                        mReportId,
                        mUrgency,
                        timestamp,
                        mLocation,
                        mDescription,
                        mResEmployeeChecked + "",
                        mFollowupEnabled + ""
                );

                // start service to send report
                SendReportService.startSendingReport(getApplicationContext(), data, mImagePathList,
                        mIsAnonymous);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
