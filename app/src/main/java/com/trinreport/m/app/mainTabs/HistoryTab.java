package com.trinreport.m.app.mainTabs;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trinreport.m.app.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryTab#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryTab extends Fragment {

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistoryTab.
     */
    public static HistoryTab newInstance() {
        HistoryTab fragment = new HistoryTab();
        return fragment;
    }

    public HistoryTab() {
        // empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_tab, container, false);
    }


}
