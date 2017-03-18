package com.trinreport.m.app;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.data.Thread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryTabFragment extends Fragment {

    // constants
    private static final String TAG = "HistoryTabFragment";

    private static int MAX_THREAD_COUNT = 100;

    private List<String> mReportIdList;
    private List<Thread> mThreadsList;
    private RecyclerView mThreadsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SharedPreferences mSharedPreferences;

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HistoryTabFragment.
     */
    public static HistoryTabFragment newInstance() {
        HistoryTabFragment fragment = new HistoryTabFragment();
        return fragment;
    }

    public HistoryTabFragment() {
        // empty
        mThreadsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_history_tab, container, false);

        // get list of ids from shared prefs
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mReportIdList = getReportIds();

        // get threads from server
        getFollowUpThreads();

        // attach adpater to recycler view
        mThreadsRecyclerView = (RecyclerView) v.findViewById(R.id.listview_threads);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new HistoryTabFragment.ThreadsAdapter();
        mThreadsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return v;
    }

    private List<String> getReportIds() {
        // get list
        Set<String> reportsSet = mSharedPreferences
                .getStringSet("reports_set", new HashSet<String>());
        List<String> reports = new ArrayList<>(reportsSet);
        return reports;
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
            messageView.setText(thread.getLastMessage());

            // TODO: convert date to friendly string
            lowTextView.setText(thread.getLastUpdated().toString());

        }
    }

    private class ThreadsAdapter extends RecyclerView.Adapter<HistoryTabFragment.ThreadHolder> {

        public ThreadsAdapter() {

        }

        @Override
        public HistoryTabFragment.ThreadHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_thread, viewGroup, false);
            return new HistoryTabFragment.ThreadHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryTabFragment.ThreadHolder photoHolder, int position) {
            photoHolder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return mThreadsList.size();
        }
    }

    private void getFollowUpThreads() {
        // get url
        String url = URL.GET_FOLLOW_UP_THREADS;

        // create request
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Volley Sucess: " + response);
                try {
                    // get report id assigned by authentication server
                    JSONObject jsonObj = new JSONObject(response);
                    Log.d(TAG, "JstonObject: " + jsonObj);
                    JSONArray jsonarray = jsonObj.getJSONArray("threads");
                    Log.d(TAG, "JsonArray: " + jsonarray);

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String title = jsonobject.getString("title");
                        String last_updated = jsonobject.getString("last_updated");
                        String last_message = jsonobject.getString("last_message");
                        String report_id = jsonobject.getString("report_id");

                        Thread thread = new Thread(last_message, new Date(), report_id, title);
                        mThreadsList.add(thread);
                        mAdapter.notifyDataSetChanged();

                    }

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.toString());
                }
            }
        }, new Response.ErrorListener() { //listener to handle errors
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());
                Toast.makeText(getActivity(), "Connection failed! Try again.",
                        Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                String auth_token = mSharedPreferences.getString("auth_token", "");
                MyData.put("auth_token", auth_token);

                return MyData;
            }
        };

        // add to queue
        requestQueue.add(stringRequest);
    }

}
