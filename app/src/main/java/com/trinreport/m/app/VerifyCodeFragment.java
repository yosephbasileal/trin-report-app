package com.trinreport.m.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * VerifyCodeFragment
 */
public class VerifyCodeFragment extends Fragment {

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VerifyCodeFragment.
     */
    public static VerifyCodeFragment newInstance() {
        VerifyCodeFragment fragment = new VerifyCodeFragment();
        return fragment;
    }

    public VerifyCodeFragment() {
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
        View v = inflater.inflate(R.layout.fragment_verify_code, container, false);

        // get verif code form edit text
        // get u_id from shared prefs
        // onclick send both to server
        // receive authentication token and save in shared prefs
        // save user state to shared prefs so that they dont have to authenticate again

        return v;
    }
}
