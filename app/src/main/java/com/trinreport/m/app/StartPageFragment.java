package com.trinreport.m.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * StartPageFragment
 */
public class StartPageFragment extends Fragment {

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StartPageFragment.
     */
    public static StartPageFragment newInstance() {
        StartPageFragment fragment = new StartPageFragment();
        return fragment;
    }

    public StartPageFragment() {
        // empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_page, container, false);
    }



}
