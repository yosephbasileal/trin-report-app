package com.trinreport.m.app.followup;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.model.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * This is fragment for followup tab with list of past reports
 */
public class FollowupTabFragment extends Fragment {

    // constants
    private static final String TAG = "FollowupTabFragment";

    // layout references
    private List<Report> mReportsList;
    private ImageButton mDeleteAllButton;
    private ImageButton mRefreshButton;
    private RecyclerView mThreadsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private TextView mNotice;

    // other references
    private SharedPreferences mSharedPreferences;

    /**
     * Wrapper method for creating an instance of this fragment
     */
    public static FollowupTabFragment newInstance() {
        FollowupTabFragment fragment = new FollowupTabFragment();
        return fragment;
    }

    /**
     * Constructor
     */
    public FollowupTabFragment() {
        mReportsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_history_tab, container, false);

        // get references
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        mNotice = (TextView) v.findViewById(R.id.followup_text_notice);
        mDeleteAllButton = (ImageButton) getActivity().findViewById(R.id.delete_all_tables);
        mRefreshButton = (ImageButton) getActivity().findViewById(R.id.refresh_followup);
        mThreadsRecyclerView = (RecyclerView) v.findViewById(R.id.listview_threads);

        // attach adapter to recycler view
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FollowupTabFragment.ReportsAdapter();
        mThreadsRecyclerView.setAdapter(mAdapter);

        // update report list from server
        updateReportsList();

        // setup toolbar
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Follow Up");
        }

        // add listener for delete all button
        mDeleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatBook.getChatBook(getActivity()).deleteAll();
                updateReportsList();
            }
        });

        // add listener for refresh button
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReportsList();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // show toolbar and buttons
        mToolbar.setVisibility(View.VISIBLE);
        mDeleteAllButton.setVisibility(View.VISIBLE);
        mRefreshButton.setVisibility(View.VISIBLE);
        updateReportsList();
    }

    @Override
    public void onStop() {
        super.onStop();
        // hide buttons
        mDeleteAllButton.setVisibility(View.GONE);
        mRefreshButton.setVisibility(View.GONE);
    }

    /**
     * Wrapper method for starting FollowUpChatActivity
     * @param report report id
     * @param position position in list
     */
    private void startFollowupChatActivity(String report, int position) {
        Intent i = new Intent(getActivity(), FollowupChatActivity.class);
        i.putExtra(FollowupChatActivity.EXTRA_REPORT_ID, report);
        i.putExtra(FollowupChatActivity.EXTRA_THREAD_TITLE, mReportsList.get(position).getTitle());
        startActivity(i);
    }

    /**
     *  ViewHolder for displaying reports in a RecyclerView
     */
    private class ReportHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout mView;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView messageView;
        private final TextView lowTextView;

        public ReportHolder(View view) {
            super(view);
            mView = (RelativeLayout) view.findViewById(R.id.list_item_thread);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
            messageView = (TextView) view.findViewById(R.id.list_item_last_message_textview);
            lowTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }


        public void bindView(final int Position) {

            // get report
            final Report report = mReportsList.get(Position);

            // set layout elements
            titleView.setText(report.getTitle());
            messageView.setText("Status: " + report.getStatus());
            lowTextView.setText(report.getNiceTimestamp());

            // add click listener
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startFollowupChatActivity(report.getReportId(), Position);
                }
            });

        }
    }

    /**
     *  Adapter for displaying reports in a RecyclerView
     */
    private class ReportsAdapter extends RecyclerView.Adapter<FollowupTabFragment.ReportHolder> {

        public ReportsAdapter() {

        }

        @Override
        public FollowupTabFragment.ReportHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_thread, viewGroup, false);
            return new FollowupTabFragment.ReportHolder(view);
        }

        @Override
        public void onBindViewHolder(FollowupTabFragment.ReportHolder reportHolder, int position) {
            reportHolder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return mReportsList.size();
        }
    }

    private void updateReportsList() {
        new GetReportList().execute();
    }

    /**
     * Async task for gets thread list from db in the background
     */
    private class GetReportList extends
            AsyncTask<Void, String, ArrayList<Report>> {

        @Override
        protected ArrayList<Report> doInBackground(Void... params) {
            // get reports from local db
            ChatBook book = ChatBook.getChatBook(getActivity());
            ArrayList<Report> reports = book.getReports();
            return reports;
        }

        @Override
        protected void onPostExecute(ArrayList<Report> result) {
            super.onPostExecute(result);
            // updates list and notify adapter
            mReportsList = result;
            mAdapter.notifyDataSetChanged();

            // check how many reports
            if(mReportsList.size() == 0) {
                mNotice.setText("No reports to show.");
            } else {
                mNotice.setText("");
            }
        }
    }
}
