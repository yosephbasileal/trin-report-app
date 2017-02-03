package com.trinreport.m.app.report;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


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

    private RadioGroup mRadioGroup;
    private Button mDateButton;
    private Button mTimeButton;
    private EditText mLocationText;
    private EditText mTypeText;
    private CheckBox mAnonymousCheckbox;
    private CheckBox mFollowupCheckbox;
    private Button mNextbutton;

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
        if(resultCode != Activity.RESULT_OK) {
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
    }

    private void updateDate() {
        mDateButton.setText(DateFormat.getDateInstance().format(mDate));
        mTimeButton.setText(DateFormat.getTimeInstance().format(mDate));
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
}
