package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.Meeting;

public class MeetingInfoFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LinearLayout currentMeetingLayout;
    private Meeting meeting;

    public MeetingInfoFragment() {
        meeting = null;
    }

    @SuppressLint("ValidFragment")
    public MeetingInfoFragment(Meeting meeting) {
        this.meeting = meeting;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentMeetingLayout = (LinearLayout) inflater.inflate(R.layout.fragment_current_meeting, container, false);
        populateLayout(meeting);
        return currentMeetingLayout;
    }

    private void populateLayout(Meeting meeting) {
        if (meeting != null) {
            TextView title = (TextView) currentMeetingLayout.findViewById(R.id.meeting_title);
            TextView date = (TextView) currentMeetingLayout.findViewById(R.id.meeting_date);
            TextView time = (TextView) currentMeetingLayout.findViewById(R.id.meeting_time);
            TextView message = (TextView) currentMeetingLayout.findViewById(R.id.meeting_message);

            title.setText("Title: " + meeting.getTitle());
            date.setText("Date: " + meeting.getDate());
            time.setText("Time: " + meeting.getStartTime() + " - " + meeting.getEndTime());
            message.setText("Message: " + meeting.getMessage());

            getFragmentManager().beginTransaction().replace(R.id.location_frame, new CustomMapFragment()).commit();
            FrameLayout layout = (FrameLayout) currentMeetingLayout.findViewById(R.id.location_frame);
            layout.setBackgroundResource(R.drawable.view_border);
        } else {
            TextView title = (TextView) currentMeetingLayout.findViewById(R.id.meeting_title);
            title.setText("No meetings found.");
        }
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

}
