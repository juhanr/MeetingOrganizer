package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.util.DateParserUtil;

public class MeetingInfoFragment extends Fragment {

    private final String title = "Meeting info";
    private MainActivity activity;
    private LinearLayout meetingInfoLayout;
    private MeetingDTO meeting;

    public MeetingInfoFragment() {
        meeting = null;
    }

    @SuppressLint("ValidFragment")
    public MeetingInfoFragment(MeetingDTO meeting) {
        this.meeting = meeting;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        if (meeting == null) {
            meetingInfoLayout = (LinearLayout) inflater.inflate(R.layout.fragment_no_data, container, false);
            TextView infoText = (TextView) meetingInfoLayout.findViewById(R.id.info_text);
            infoText.setText("No data found.");
        } else {
            meetingInfoLayout = (LinearLayout) inflater.inflate(R.layout.fragment_meeting_info, container, false);
            populateLayout(meeting);
            setButtonListeners();
        }
        return meetingInfoLayout;
    }

    private void populateLayout(MeetingDTO meeting) {
        TextView title = (TextView) meetingInfoLayout.findViewById(R.id.meeting_title);
        TextView description = (TextView) meetingInfoLayout.findViewById(R.id.meeting_description);
        TextView date = (TextView) meetingInfoLayout.findViewById(R.id.meeting_date);
        TextView time = (TextView) meetingInfoLayout.findViewById(R.id.meeting_time);

        title.setText("Title: " + meeting.getTitle());
        if (!description.getText().equals(""))
            description.setText("Description: " + meeting.getDescription());
        else
            meetingInfoLayout.removeView(description);
        date.setText("Date: " + DateParserUtil.formatDate(meeting.getStartDateTime()));
        time.setText("Time: " + DateParserUtil.formatTime(meeting.getStartDateTime()) +
                " - " + DateParserUtil.formatTime(meeting.getEndDateTime()));

        CustomMapFragment customMapFragment = new CustomMapFragment();
        customMapFragment.setLocation(new LatLng(meeting.getLocationLatitude(),
                meeting.getLocationLongitude()));
        getFragmentManager().beginTransaction().
                replace(R.id.location_frame, customMapFragment).commit();
        FrameLayout layout = (FrameLayout) meetingInfoLayout.findViewById(R.id.location_frame);
        layout.setBackgroundResource(R.drawable.view_border);
    }

    private void setButtonListeners() {
        Button showParticipants = (Button) meetingInfoLayout
                .findViewById(R.id.show_participants);

        showParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).changeFragment(new ParticipantsListFragment(
                        new ArrayList<ParticipantDTO>(meeting.getParticipants())));
            }
        });
    }

}
