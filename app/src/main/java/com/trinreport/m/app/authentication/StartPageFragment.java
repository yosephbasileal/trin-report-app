package com.trinreport.m.app.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.trinreport.m.app.R;


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
        View v =  inflater.inflate(R.layout.fragment_start_page, container, false);

        Button getStartedButton = (Button) v.findViewById(R.id.get_started_button);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start RequestTokenActivity
                Intent i = new Intent(getActivity(), RequestTokenActivity.class);
                startActivity(i);
            }
        });

        return v;
    }



}
