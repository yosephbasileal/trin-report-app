package com.trinreport.m.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.trinreport.m.app.emergency.EmergencyTabFragment;
import com.trinreport.m.app.report.ReportTabFragment;

public class MainActivity extends AppCompatActivity {

    FragmentManager fm;
    Fragment frag_emergency, frag_report, frag_history, frag_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setTitle("TrinReport");
        setSupportActionBar(toolbar);

        fm = getFragmentManager();

        frag_emergency = EmergencyTabFragment.newInstance();
        frag_report = ReportTabFragment.newInstance();
        frag_history = HistoryTab.newInstance();
        frag_settings = SettingsTabFragment.newInstance();

        fm.beginTransaction().add(R.id.main_activity_container, frag_emergency).commit();

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
                @Override
                public void onTabSelected(@IdRes int tabId) {
                    if (tabId == R.id.tab_emergency) {
                        setTitle("");
                        fm.beginTransaction().replace(R.id.main_activity_container, frag_emergency).commit();
                    } else if (tabId == R.id.tab_report) {
                        setTitle("Incident Report");
                        fm.beginTransaction().replace(R.id.main_activity_container, frag_report).commit();
                    } else if (tabId == R.id.tab_history) {
                        setTitle("Archive");
                        fm.beginTransaction().replace(R.id.main_activity_container, frag_history).commit();
                    } else if (tabId == R.id.tab_settings) {
                        setTitle("Settings");
                        fm.beginTransaction().replace(R.id.main_activity_container, frag_settings).commit();
                    }
                }
            });
        }
    }
}
