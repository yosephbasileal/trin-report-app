package com.trinreport.m.app.MainTabs;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        return inflater.inflate(R.layout.fragment_report_tab, container, false);
    }


}
