package ee.juhan.meetingorganizer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.Date;
import ee.juhan.meetingorganizer.models.Time;

public class NewMeetingFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private MainActivity activity;
    private static LinearLayout newMeetingLayout;

    public NewMeetingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        newMeetingLayout = (LinearLayout) inflater.inflate(R.layout.fragment_new_meeting, container, false);
        setButtonListeners();
        return newMeetingLayout;
    }


    private void setButtonListeners() {
        TextView dateButton = (TextView) newMeetingLayout
                .findViewById(R.id.date_button);
        TextView startTimeButton = (TextView) newMeetingLayout
                .findViewById(R.id.start_time_button);
        TextView endTimeButton = (TextView) newMeetingLayout
                .findViewById(R.id.end_time_button);
        Button continueButton = (Button) newMeetingLayout
                .findViewById(R.id.continue_button);

        dateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getFragmentManager(), "datePicker");
            }
        });

        startTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(R.id.start_time_button);
                timePickerFragment.show(getFragmentManager(), "timePicker");
            }
        });

        endTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(R.id.end_time_button);
                timePickerFragment.show(getFragmentManager(), "timePicker");
            }
        });

        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                activity.changeFragment(new ChooseLocationFragment(), "Choose location");
            }
        });

    }

    private void saveData() {

    }

    public static void changeDate(Date date) {
        TextView dateButton = (TextView) newMeetingLayout
                .findViewById(R.id.date_button);
        dateButton.setText(underlineString(date.toString()));
    }

    public static void changeTime(Time time, int viewId) {
        TextView dateButton = (TextView) newMeetingLayout
                .findViewById(viewId);
        dateButton.setText(underlineString(time.toString()));
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    private static android.text.Spanned underlineString(String s) {
        return Html.fromHtml("<u>" + s + "</u>");
    }

}
