package com.trinreport.m.app.report;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.trinreport.m.app.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportTabFragment extends Fragment {

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReportTabFragment.
     */
    public static ReportTabFragment newInstance() {
        ReportTabFragment fragment = new ReportTabFragment();
        return fragment;
    }

    public ReportTabFragment() {
        // empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_report_tab, container, false);

        Button addReportButton = (Button) v.findViewById(R.id.add_report_button);
        addReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddReportActivity.class);
                startActivity(i);
            }
        });

        return v;
    }


}
