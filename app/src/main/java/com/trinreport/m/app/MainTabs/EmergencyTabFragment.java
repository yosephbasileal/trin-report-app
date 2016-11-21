package com.trinreport.m.app.MainTabs;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trinreport.m.app.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmergencyTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmergencyTabFragment extends Fragment {

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EmergencyTabFragment.
     */
    public static EmergencyTabFragment newInstance() {
        EmergencyTabFragment fragment = new EmergencyTabFragment();
        return fragment;
    }

    public EmergencyTabFragment() {
        // empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emergency_tab, container, false);
    }


}
