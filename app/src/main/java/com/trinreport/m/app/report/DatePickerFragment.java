package com.trinreport.m.app.report;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.trinreport.m.app.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Fragment for picking date of incident
 */
public class DatePickerFragment extends DialogFragment {

    // constants
    public static final String EXTRA_DATE = "com.trinreport.m.app.report.date";
    private static final String ARG_DATE = "date";

    // references
    private DatePicker mDatePicker;
    private Date mDate;
    private OnCompleteDateListener mListener;

    /**
     * Wrapper for instantiating date picker fragment
     * @param date date currently displayed on screen
     */
    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // get date from intent
        mDate = (Date) getArguments().getSerializable(ARG_DATE);

        // get current values shown on screen
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        final TimeZone tmz = calendar.getTimeZone();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        // update date picker with current date
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_date, null);
        mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
        mDatePicker.init(year, month, day, null);

        // show dialog and add listener to button
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();

                        GregorianCalendar newCalendar = new GregorianCalendar(
                                year, month, day, hour, minute);
                        newCalendar.setTimeZone(tmz);
                        mDate = newCalendar.getTime();
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
            this.mListener.onCompleteDate(date);
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
            this.mListener = (OnCompleteDateListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement OnCompleteListener");
        }
    }

    /**
     * This interface is implemented by parent activity
     */
    public interface OnCompleteDateListener {
        void onCompleteDate(Date date);
    }
}
