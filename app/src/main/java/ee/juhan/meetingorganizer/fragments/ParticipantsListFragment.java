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
import ee.juhan.meetingorganizer.adapters.ParticipantsAdapter;
import ee.juhan.meetingorganizer.models.Participant;

public class ParticipantsListFragment extends Fragment {

    private MainActivity activity;
    private final String title = "Participants";
    private LinearLayout participantsListLayout;
    private ParticipantsAdapter adapter;
    private final List<Participant> participantsList;

    public ParticipantsListFragment() {
        this.participantsList = null;
    }

    @SuppressLint("ValidFragment")
    public ParticipantsListFragment(List<Participant> participantsList) {
        this.participantsList = participantsList;
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
        if (participantsList == null || participantsList.size() == 0) {
            participantsListLayout = (LinearLayout) inflater.inflate(R.layout.fragment_no_data, container, false);
            TextView infoText = (TextView) participantsListLayout.findViewById(R.id.info_text);
            infoText.setText("No participants found.");
        } else {
            participantsListLayout = (LinearLayout) inflater.inflate(R.layout.layout_listview, container, false);
            refreshListView();
        }
        return participantsListLayout;
    }

    public void refreshListView() {
        ListView listview = (ListView) participantsListLayout.findViewById(R.id.listView);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Participant participant = (Participant) adapter.getItem(position);
            }
        });

        adapter = new ParticipantsAdapter(getActivity(), participantsList);
        listview.setAdapter(adapter);
    }

}