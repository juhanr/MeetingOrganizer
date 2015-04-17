package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.MeetingsAdapter;
import ee.juhan.meetingorganizer.models.Meeting;

public class InvitationsListFragment extends Fragment {

    private MainActivity activity;
    private final String title = "Invitations list";
    private LinearLayout invitationsListLayout;
    private MeetingsAdapter adapter;
    private final List<Meeting> invitationsList;

    public InvitationsListFragment() {
        this.invitationsList = null;
    }

    @SuppressLint("ValidFragment")
    public InvitationsListFragment(List<Meeting> invitationsList) {
        this.invitationsList = invitationsList;
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
        if (invitationsList == null || invitationsList.size() == 0) {
            invitationsListLayout = (LinearLayout) inflater.inflate(R.layout.fragment_no_data, container, false);
            TextView infoText = (TextView) invitationsListLayout.findViewById(R.id.info_text);
            infoText.setText("No invitations found.");
        } else {
            invitationsListLayout = (LinearLayout) inflater.inflate(R.layout.layout_listview, container, false);
            refreshListView();
        }
        return invitationsListLayout;
    }

    public void refreshListView() {
        ListView listview = (ListView) invitationsListLayout.findViewById(R.id.listView);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Meeting meeting = (Meeting) adapter.getItem(position);
                ((MainActivity) getActivity()).changeFragment(new MeetingInfoFragment(meeting));
            }
        });

        adapter = new MeetingsAdapter(getActivity(), invitationsList);
        listview.setAdapter(adapter);
    }

}