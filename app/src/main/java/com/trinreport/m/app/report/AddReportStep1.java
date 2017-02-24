package com.trinreport.m.app.report;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.trinreport.m.app.R;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddReportStep1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddReportStep1 extends Fragment {

    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_UPLOAD = 3;

    private RadioGroup mRadioGroup;
    private Button mDateButton;
    private Button mTimeButton;
    private EditText mLocationText;
    private EditText mTypeText;
    private CheckBox mAnonymousCheckbox;
    private CheckBox mFollowupCheckbox;
    private Button mNextbutton;
    private ImageView mImageView;
    private Button mImageButton;

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
    private boolean mIsAnonymous;
    private boolean mFollowupEnabled;

    public AddReportStep1() {
        mUrgency = "medium";
        mDate = new Date();
        mLocation = "n/a";
        mType = "n/a";
        mIsAnonymous = false;
        mFollowupEnabled = true;
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddReportStep1.
     */
    public static AddReportStep1 newInstance() {
        AddReportStep1 fragment = new AddReportStep1();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_add_report_step1, container, false);


        mRadioGroup = (RadioGroup) v.findViewById(R.id.radio_urgency);
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


        mDateButton = (Button) v.findViewById(R.id.report_date);
        mTimeButton = (Button) v.findViewById(R.id.report_time);
        updateDate();

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mDate);
                dialog.setTargetFragment(AddReportStep1.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mDate);
                dialog.setTargetFragment(AddReportStep1.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mLocationText = (EditText) v.findViewById(R.id.edit_text_location);
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

        mTypeText = (EditText) v.findViewById(R.id.edit_text_type);
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

        mAnonymousCheckbox = (CheckBox) v.findViewById(R.id.checkbox_anonymous);
        mAnonymousCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsAnonymous = isChecked;
            }
        });

        mFollowupCheckbox = (CheckBox) v.findViewById(R.id.checkbox_followup);
        mFollowupCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFollowupEnabled = isChecked;
            }
        });

        // test

        mImageView = (ImageView) v.findViewById(R.id.image_view);
        mImageButton = (Button) v.findViewById(R.id.button_take_picture);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dispatchTakePictureIntent2();
                dispathUploadPictureIntent();
            }
        });

        mImagePathList = new ArrayList<>();
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.images_recycler_view);

        mPhotoRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PhotoAdapter();
        mPhotoRecyclerView.setAdapter(mAdapter);

        // end test

        mNextbutton = (Button) v.findViewById(R.id.button_to_step2);
        mNextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReport();

            }
        });
        return v;
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
            setPic(mCurrentPhotoPath, mImageView);
            mImagePathList.add(mCurrentPhotoPath);
            mAdapter.notifyDataSetChanged();
        }
        if (requestCode == REQUEST_IMAGE_UPLOAD && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            String path = getRealPathFromURI_API11to18(getActivity(), uri);
            setPic(path, mImageView);
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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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


    private void sendReport() {

        String url = "http://1dc04038.ngrok.io/report";

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this.getActivity());
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap();
                MyData.put("urgency", mUrgency ); //Add the data you'd like to send to the server.
                MyData.put("year", 1900 + mDate.getYear() +  "" );
                MyData.put("month", 1 + mDate.getMonth() +  "" );
                MyData.put("day", mDate.getDate() + "");
                MyData.put("hour", mDate.getHours() +  "" );
                MyData.put("minute", mDate.getMinutes() +  "" );
                MyData.put("location", mLocation);
                MyData.put("type", mType);
                MyData.put("is_anonymous", mIsAnonymous + "");
                MyData.put("follow_up_enabled", mFollowupEnabled + "");

                if (!mIsAnonymous) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    MyData.put("username", prefs.getString("username", ""));
                    MyData.put("userdorm", prefs.getString("userdorm", ""));
                    MyData.put("useremail", prefs.getString("useremail", ""));
                    MyData.put("userphone", prefs.getString("userphone", ""));
                    MyData.put("userid", prefs.getString("userid", ""));
                }
                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }



    /**
     *   ViewHolder for displaying snaps in a RecyclerView
     */
    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.image);
        }


        public void bindDrawable(final int Position) {
            // get path to image file
            String path = mImagePathList.get(Position);

            // display image
            if (path == null) {
                //mItemImageView.setImageDrawable(null);
            } else {
                setPic(path, mItemImageView);
            }

            // onclick listener that opens ViewPager acticity when clicked on an image
            mItemImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }


    /**
     *   Adapter for displaying snaps in a RecyclerView
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        public PhotoAdapter() {
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
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
}
