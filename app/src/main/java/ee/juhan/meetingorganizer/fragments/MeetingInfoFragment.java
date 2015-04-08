package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

import ee.juhan.meetingorganizer.MainActivity;
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
        if (meeting == null) {
            currentMeetingLayout = (LinearLayout) inflater.inflate(R.layout.fragment_no_data, container, false);
            TextView infoText = (TextView) currentMeetingLayout.findViewById(R.id.info_text);
            infoText.setText("No ongoing meeting found.");
        } else {
            currentMeetingLayout = (LinearLayout) inflater.inflate(R.layout.fragment_meeting_info, container, false);
            populateLayout(meeting);
            setButtonListeners();
        }
        return currentMeetingLayout;
    }

    private void populateLayout(Meeting meeting) {
        TextView title = (TextView) currentMeetingLayout.findViewById(R.id.meeting_title);
        TextView date = (TextView) currentMeetingLayout.findViewById(R.id.meeting_date);
        TextView time = (TextView) currentMeetingLayout.findViewById(R.id.meeting_time);
        TextView message = (TextView) currentMeetingLayout.findViewById(R.id.meeting_message);

        title.setText("Title: " + meeting.getTitle());
        date.setText("Date: " + meeting.getDate());
        time.setText("Time: " + meeting.getStartTime() + " - " + meeting.getEndTime());
        message.setText("Message: " + meeting.getMessage());

        CustomMapFragment customMapFragment = new CustomMapFragment();
        customMapFragment.setLocation(meeting.getLocation());
        getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment).commit();
        FrameLayout layout = (FrameLayout) currentMeetingLayout.findViewById(R.id.location_frame);
        layout.setBackgroundResource(R.drawable.view_border);
    }

    private void setButtonListeners() {
        Button showParticipants = (Button) currentMeetingLayout
                .findViewById(R.id.show_participants);

        showParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).changeFragment(new ParticipantsListFragment(
                        Arrays.asList(meeting.getParticipants())), "Participants");
            }
        });

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
