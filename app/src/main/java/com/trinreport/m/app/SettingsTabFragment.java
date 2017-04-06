package com.trinreport.m.app;


import android.os.Bundle;
import android.app.Fragment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsTabFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private Toolbar mToolbar;

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsTabFragment.
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

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        // setup toolbar
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");
        }

        addPreferencesFromResource(R.xml.preferences);

        bindPreferenceSummaryToValue(findPreference("username"));
        bindPreferenceSummaryToValue(findPreference("userphone"));
        bindPreferenceSummaryToValue(findPreference("userid"));
        bindPreferenceSummaryToValue(findPreference("useremail"));
        bindPreferenceSummaryToValue(findPreference("userdorm"));

    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // set the listener to watch for value changes
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringvalue = value.toString();
        preference.setSummary(stringvalue);
        return true;
    }
}
