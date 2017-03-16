package com.trinreport.m.app;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.trinreport.m.app.data.Thread;
import com.trinreport.m.app.report.AddReportFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryTab#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryTab extends Fragment {

    private static int MAX_THREAD_COUNT = 100;

    private List<Thread> mThreadsList;
    private RecyclerView mThreadsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

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
        mThreadsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_history_tab, container, false);

        // add test thread
        List<String> messages = new ArrayList<>();
        messages.add("asdfasdf");
        messages.add("2qwer");
        mThreadsList.add(new Thread(messages, new Date(), 1, "Title"));

        // attach adpater to recycler view
        mThreadsRecyclerView = (RecyclerView) v.findViewById(R.id.listview_threads);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new HistoryTab.ThreadsAdapter();
        mThreadsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return v;
    }

    /**
     *   ViewHolder for displaying snaps in a RecyclerView
     */
    private class ThreadHolder extends RecyclerView.ViewHolder {

        public final ImageView iconView;
        public final TextView titleView;
        public final TextView messageView;
        public final TextView highTextView;
        public final TextView lowTextView;

        public ThreadHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
            messageView = (TextView) view.findViewById(R.id.list_item_last_message_textview);
            highTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }


        public void bindView(final int Position) {

            Thread thread = mThreadsList.get(Position);
            titleView.setText(thread.getTitle());
            messageView.setText(thread.getMessages().get(0));

            // TODO: convert date to friendly string
            lowTextView.setText(thread.getLastUpdated().toString());

        }
    }

    private class ThreadsAdapter extends RecyclerView.Adapter<HistoryTab.ThreadHolder> {

        public ThreadsAdapter() {

        }

        @Override
        public HistoryTab.ThreadHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_thread, viewGroup, false);
            return new HistoryTab.ThreadHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryTab.ThreadHolder photoHolder, int position) {
            photoHolder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return mThreadsList.size();
        }
    }

}
