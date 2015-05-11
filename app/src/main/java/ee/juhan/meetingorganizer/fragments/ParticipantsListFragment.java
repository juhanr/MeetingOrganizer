package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.ParticipantsAdapter;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;

public class ParticipantsListFragment extends Fragment {

    private final List<ParticipantDTO> participantsList;
    private String title;
    private MainActivity activity;
    private ViewGroup participantsListLayout;
    private ParticipantsAdapter adapter;

    public ParticipantsListFragment() {
        this.participantsList = null;
    }

    @SuppressLint("ValidFragment")
    public ParticipantsListFragment(List<ParticipantDTO> participantsList) {
        this.participantsList = participantsList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        title = getString(R.string.title_participants);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        if (participantsList == null || participantsList.size() == 0) {
            participantsListLayout = (ViewGroup) inflater.inflate(R.layout.fragment_no_data, container, false);
            TextView infoText = (TextView) participantsListLayout.findViewById(R.id.info_text);
            infoText.setText(getString(R.string.textview_no_participants));
        } else {
            participantsListLayout = (ViewGroup) inflater.inflate(R.layout.layout_listview, container, false);
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
                ParticipantDTO participant = (ParticipantDTO) adapter.getItem(position);
                activity.changeFragment(new ParticipantInfoFragment(participant));
            }
        });
        adapter = new ParticipantsAdapter(getActivity(), participantsList);
        listview.setAdapter(adapter);
    }

}