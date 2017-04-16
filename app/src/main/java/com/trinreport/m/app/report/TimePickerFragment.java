package com.trinreport.m.app.report;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import com.trinreport.m.app.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Fragment for picking time of incident
 */
public class TimePickerFragment extends DialogFragment {

    // constants
    public static final String EXTRA_TIME = "com.trinreport.m.app.report.time";
    private static final String ARG_TIME = "time";

    // references
    private TimePicker mTimePicker;
    private Date mDate;
    private OnCompleteTimeListener mListener;

    /**
     * Wrapper for instantiating time picker fragment
     * @param date date currently displayed on screen
     */
    public static TimePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, date);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // get date from intent
        mDate = (Date) getArguments().getSerializable(ARG_TIME);

        // get current values shown on screen
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        final TimeZone tmz = calendar.getTimeZone();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        // update time picker with current time
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time, null);

        mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_time_picker);
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);

        // add listener for when the time is changed
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hour, int minute) {
                GregorianCalendar newCalendar = new GregorianCalendar(
                        year, month, day, hour, minute);
                newCalendar.setTimeZone(tmz);
                mDate = newCalendar.getTime();
            }
        });

        // show dialog and add listener to button
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, mDate);
                    }
                })
                .create();
    }

    /**
     * Send result to parent activity
     * @param resultCode result code
     * @param date data
     */
    private void sendResult(int resultCode, Date date) {
        if(resultCode == Activity.RESULT_OK) {
            this.mListener.onCompleteTime(date);
        }
    }

    /**
     * Fired when fragment attaches to activity
     * @param activity parent activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteTimeListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement OnCompleteListener");
        }
    }

    /**
     * This interface is implemented by parent activity
     */
    public interface OnCompleteTimeListener {
        void onCompleteTime(Date time);
    }
}
