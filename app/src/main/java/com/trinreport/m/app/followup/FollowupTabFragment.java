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
import android.widget.Button;
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
 * A simple {@link Fragment} subclass.
 * Use the {@link FollowupTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowupTabFragment extends Fragment {

    // constants
    private static final String TAG = "FollowupTabFragment";

    private List<Report> mReportsList;
    private ImageButton mDeleteAllButton;
    private ImageButton mRefreshButton;
    private RecyclerView mThreadsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private TextView mNotice;

    private SharedPreferences mSharedPreferences;

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FollowupTabFragment.
     */
    public static FollowupTabFragment newInstance() {
        FollowupTabFragment fragment = new FollowupTabFragment();
        return fragment;
    }

    public FollowupTabFragment() {
        // empty
        mReportsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_history_tab, container, false);

        // get list of ids from shared prefs
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // updates threads data from server
        //getFollowUpThreads();
        //new GetReportList2().execute();

        // attach adpater to recycler view
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_main);
        mNotice = (TextView) v.findViewById(R.id.followup_text_notice);
        mDeleteAllButton = (ImageButton) getActivity().findViewById(R.id.delete_all_tables);
        mRefreshButton = (ImageButton) getActivity().findViewById(R.id.refresh_followup);
        mThreadsRecyclerView = (RecyclerView) v.findViewById(R.id.listview_threads);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FollowupTabFragment.ReportsAdapter();
        mThreadsRecyclerView.setAdapter(mAdapter);
        updateReportsList();


        // setup toolbar
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Follow Up");
        }

        mDeleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatBook.getChatBook(getActivity()).deleteAll();
                updateReportsList();
            }
        });

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
        // updates threads data from server
        //getFollowUpThreads();
        //new GetReportList2().execute();
        mToolbar.setVisibility(View.VISIBLE);
        mDeleteAllButton.setVisibility(View.VISIBLE);
        mRefreshButton.setVisibility(View.VISIBLE);
        updateReportsList();
    }

    @Override
    public void onStop() {
        super.onStop();
        mDeleteAllButton.setVisibility(View.GONE);
        mRefreshButton.setVisibility(View.GONE);
    }


    private void startFollowupChatActivity(String report, int position) {
        Intent i = new Intent(getActivity(), FollowupChatActivity.class);
        i.putExtra(FollowupChatActivity.EXTRA_REPORT_ID, report);
        i.putExtra(FollowupChatActivity.EXTRA_THREAD_TITLE, mReportsList.get(position).getTitle());
        startActivity(i);
    }

    /**
     *   ViewHolder for displaying snaps in a RecyclerView
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

            final Report report = mReportsList.get(Position);
            titleView.setText(report.getTitle());
            messageView.setText("Status: " + report.getStatus());

            // TODO: convert date to friendly string
            //lowTextView.setText(thread.getNiceTimestamp());
            lowTextView.setText(report.getNiceTimestamp());

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startFollowupChatActivity(report.getReportId(), Position);
                }
            });

        }
    }

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
     * Async task for gets thread list in the background
     */
    private class GetReportList extends
            AsyncTask<Void, String, ArrayList<Report>> {

        @Override
        protected ArrayList<Report> doInBackground(Void... params) {
            ChatBook book = ChatBook.getChatBook(getActivity());
            ArrayList<Report> reports = book.getReports();
            return reports;
        }

        @Override
        protected void onPostExecute(ArrayList<Report> result) {
            super.onPostExecute(result);
            mReportsList = result;
            mAdapter.notifyDataSetChanged();

            if(mReportsList.size() == 0) {
                mNotice.setText("No reports to show.");
            } else {
                mNotice.setText("");
            }
        }
    }
}
