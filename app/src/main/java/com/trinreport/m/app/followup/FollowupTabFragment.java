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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trinreport.m.app.ChatBook;
import com.trinreport.m.app.R;
import com.trinreport.m.app.URL;
import com.trinreport.m.app.model.ChatKey;
import com.trinreport.m.app.model.ChatMessage;
import com.trinreport.m.app.model.Thread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FollowupTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowupTabFragment extends Fragment {

    // constants
    private static final String TAG = "FollowupTabFragment";

    private static int MAX_THREAD_COUNT = 100;

    private List<String> mReportIdList;
    private List<Thread> mThreadsList;
    private Button mDeleteAllButton;
    private RecyclerView mThreadsRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toolbar mToolbar;

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

        // updates threads data from server
        //getFollowUpThreads();
        //new GetThreadList2().execute();

        // attach adpater to recycler view
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_followup);
        mDeleteAllButton = (Button) v.findViewById(R.id.delete_all_tables);
        mThreadsRecyclerView = (RecyclerView) v.findViewById(R.id.listview_threads);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mThreadsRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FollowupTabFragment.ThreadsAdapter();
        mThreadsRecyclerView.setAdapter(mAdapter);
        updateThreadsList();

        // setup toolbar
        if (mToolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Follow Up");
        }

        mDeleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatBook.getChatBook(getActivity()).deleteAll();
                updateThreadsList();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // updates threads data from server
        //getFollowUpThreads();
        //new GetThreadList2().execute();
        updateThreadsList();
    }

    private List<String> getReportIds() {
        // get list
        Set<String> reportsSet = mSharedPreferences
                .getStringSet("reports_set", new HashSet<String>());
        List<String> reports = new ArrayList<>(reportsSet);
        return reports;
    }

        private void startFollowupChatActivity(String thread_id, int position) {
        Intent i = new Intent(getActivity(), FollowupChatActivity.class);
        i.putExtra(FollowupChatActivity.EXTRA_THREAD_ID, thread_id);
        i.putExtra(FollowupChatActivity.EXTRA_THREAD_TITLE, mThreadsList.get(position).getTitle());
        startActivity(i);
    }

    /**
     *   ViewHolder for displaying snaps in a RecyclerView
     */
    private class ThreadHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout mView;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView messageView;
        private final TextView highTextView;
        private final TextView lowTextView;

        public ThreadHolder(View view) {
            super(view);
            mView = (RelativeLayout) view.findViewById(R.id.list_item_thread);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
            messageView = (TextView) view.findViewById(R.id.list_item_last_message_textview);
            highTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }


        public void bindView(final int Position) {

            final Thread thread = mThreadsList.get(Position);
            titleView.setText(thread.getTitle());
            messageView.setText(thread.getLastMessage());

            // TODO: convert date to friendly string
            lowTextView.setText(thread.getNiceTimestamp());

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startFollowupChatActivity(thread.getThreadId(), Position);
                }
            });

        }
    }

    private class ThreadsAdapter extends RecyclerView.Adapter<FollowupTabFragment.ThreadHolder> {

        public ThreadsAdapter() {

        }

        @Override
        public FollowupTabFragment.ThreadHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_thread, viewGroup, false);
            return new FollowupTabFragment.ThreadHolder(view);
        }

        @Override
        public void onBindViewHolder(FollowupTabFragment.ThreadHolder photoHolder, int position) {
            photoHolder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return mThreadsList.size();
        }
    }

    private void updateThreadsList() {
        new GetThreadList().execute();
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

                    //mThreadsList.clear();
                    ChatBook.getChatBook(getActivity()).deleteThreads();

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String title = jsonobject.getString("title");
                        String last_updated = jsonobject.getString("last_updated");
                        String last_message = jsonobject.getString("last_message");
                        String thread_id = jsonobject.getString("id");

                        Thread thread = new Thread(last_message, last_updated, thread_id, title);
                        ChatBook.getChatBook(getActivity()).addThread(thread);

                        JSONArray jsonarray2 = jsonobject.getJSONArray("messages");
                        for (int j = 0; j < jsonarray2.length(); j++) {
                            JSONObject jsonobject2 = jsonarray2.getJSONObject(j);
                            String timestamp = jsonobject2.getString("timestamp");
                            String content = jsonobject2.getString("content");
                            String from_admin = jsonobject2.getInt("from_admin") + "";

                            ChatMessage message = new ChatMessage(from_admin, content, timestamp, thread_id);
                            ChatBook.getChatBook(getActivity()).addMessage(message);
                        }

                        //mThreadsList.add(thread);
                        //mAdapter.notifyDataSetChanged();
                        // add to database

                    }
                    //mAdapter.notifyDataSetChanged();
                    updateThreadsList();


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




    /**
     * Async task for gets thread list in the background
     */
    private class GetThreadList extends
            AsyncTask<Void, String, ArrayList<Thread>> {

        @Override
        protected ArrayList<Thread> doInBackground(Void... params) {
            ChatBook book = ChatBook.getChatBook(getActivity());
            ArrayList<Thread> threads = book.getThreads();
            return threads;
        }

        @Override
        protected void onPostExecute(ArrayList<Thread> result) {
            super.onPostExecute(result);
            mThreadsList = result;
            mAdapter.notifyDataSetChanged();
        }
    }



}
