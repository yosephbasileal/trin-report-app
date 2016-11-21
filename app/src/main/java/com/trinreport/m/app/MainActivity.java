package com.trinreport.m.app;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.trinreport.m.app.MainTabs.EmergencyTabFragment;
import com.trinreport.m.app.MainTabs.HistoryTab;
import com.trinreport.m.app.MainTabs.ReportTabFragment;

public class MainActivity extends AppCompatActivity {

    FragmentManager fm;
    Fragment frag_emergency, frag_report, frag_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getSupportFragmentManager();

        frag_emergency = EmergencyTabFragment.newInstance();
        frag_report = ReportTabFragment.newInstance();
        frag_history = HistoryTab.newInstance();

        fm.beginTransaction().add(R.id.main_activity_container, frag_emergency).commit();

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_emergency) {
                    fm.beginTransaction().replace(R.id.main_activity_container, frag_emergency).commit();
                }
                else if (tabId == R.id.tab_report) {
                    fm.beginTransaction().replace(R.id.main_activity_container, frag_report).commit();
                }
                else if (tabId == R.id.tab_history) {
                    fm.beginTransaction().replace(R.id.main_activity_container, frag_history).commit();
                }
                else if (tabId == R.id.tab_settings) {
                    // The tab with id R.id.tab_settings was selected,
                    // change your content accordingly.
                }
            }
        });
    }
}
