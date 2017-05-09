package com.trinreport.m.app;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * This is fragment for tab with user settings
 */
public class SettingsTabFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    // layout references
    private Toolbar mToolbar;

    /**
     * Factory method to create a new instance of
     * this fragment
     */
    public static SettingsTabFragment newInstance() {
        SettingsTabFragment fragment = new SettingsTabFragment();
        return fragment;
    }

    public SettingsTabFragment() {
        // empty
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup toolbar
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");
        }

        // add refernce xml
        addPreferencesFromResource(R.xml.preferences);

        // bind summaries to value
        bindPreferenceSummaryToValue(findPreference("username"));
        bindPreferenceSummaryToValue(findPreference("userphone"));
        bindPreferenceSummaryToValue(findPreference("userid"));
        bindPreferenceSummaryToValue(findPreference("useremail"));
        bindPreferenceSummaryToValue(findPreference("userdorm"));
        //bindPreferenceSummaryToValue(findPreference("notifications"));

    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setVisibility(View.VISIBLE);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // set the listener to watch for value changes
        preference.setOnPreferenceChangeListener(this);

        // check if notification setting is changed
        if(preference.getKey().equals("notifications")) {
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), true));
        } else {
            // all other settings
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringvalue = value.toString();
        preference.setSummary(stringvalue);
        return true;
    }
}
