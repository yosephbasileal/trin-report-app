package com.trinreport.m.app.report;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.trinreport.m.app.R;


/**
 * This is fragment for tab with create report button
 */
public class ReportTabFragment extends Fragment {

    // constants
    private static final String TAG = "ReportTabFragment";
    private static final String CS_PHONE_NUMBER = "8602972222";
    private static final String T9_PHONE_NUMBER = "8602975146";
    private static final String HC_PHONE_NUMBER = "8602972020";
    private static final String CC_PHONE_NUMBER = "8602972415";

    // layout references
    private Button mAddReportButton;
    private Button mCallCsSafetyButton;
    private Button mCallCounselingCentButton;
    private Button mCallTitleNineButton;
    private Button mCallHealthCenterButton;
    private Toolbar mToolbar;


    /**
     * Factory method to create a new instance of this fragment
     */
    public static ReportTabFragment newInstance() {
        ReportTabFragment fragment = new ReportTabFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_report_tab, container, false);

        // get references
        mAddReportButton = (Button) v.findViewById(R.id.add_report_button);
        mCallCsSafetyButton = (Button) v.findViewById(R.id.call_campus_safety);
        mCallCounselingCentButton = (Button) v.findViewById(R.id.call_counselling_center);
        mCallTitleNineButton = (Button) v.findViewById(R.id.call_title_9);
        mCallHealthCenterButton = (Button) v.findViewById(R.id.call_health_center);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);

        // setup toolbar
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        }

        mAddReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddReportActivity.class);
                startActivity(i);
            }
        });

        // button for calling campus safety office
        mCallCsSafetyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + CS_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {
                    showCallErrorToast();
                }
            }
        });

        // button for calling counseling center
        mCallCounselingCentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + CC_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {
                    showCallErrorToast();
                }
            }
        });

        // button for calling title IX office
        mCallTitleNineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + T9_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {
                    showCallErrorToast();
                }
            }
        });

        // button for calling health center
        mCallHealthCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel: " + HC_PHONE_NUMBER));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e) {
                    showCallErrorToast();
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setVisibility(View.GONE);
    }

    /**
     * Shows toast message when call button fails
     */
    private void showCallErrorToast() {
        Toast.makeText(getActivity(), "Something went wrong! Try again.",
                Toast.LENGTH_LONG).show();
    }
}
