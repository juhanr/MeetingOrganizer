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
import ee.juhan.meetingorganizer.core.communications.loaders.FutureMeetingsLoader;
import ee.juhan.meetingorganizer.core.communications.loaders.InvitationsLoader;
import ee.juhan.meetingorganizer.core.communications.loaders.OngoingMeetingsLoader;
import ee.juhan.meetingorganizer.core.communications.loaders.PastMeetingsLoader;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;

@SuppressLint("ValidFragment")
public class MeetingsListFragment extends Fragment {

    private final String title;
    private MainActivity activity;
    private LinearLayout meetingsListLayout;
    private MeetingsAdapter adapter;
    private List<MeetingDTO> meetingsList;

    public MeetingsListFragment(String title, MainActivity activity) {
        this.title = title;
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle(title);
        activity.setDrawerItem(activity.getDrawerItemPosition(title));
        if (meetingsList == null || meetingsList.size() == 0) {
            meetingsListLayout = (LinearLayout) inflater.inflate(R.layout.fragment_no_data, container, false);
            TextView infoText = (TextView) meetingsListLayout.findViewById(R.id.info_text);
            if (title.equals("Invitations"))
                infoText.setText("No invitations found.");
            else
                infoText.setText("No meetings found.");
        } else {
            meetingsListLayout = (LinearLayout) inflater.inflate(R.layout.layout_listview, container, false);
            refreshListView();
        }
        return meetingsListLayout;
    }

    public void loadMeetingsList() {
        if (title.equals("Ongoing meetings")) {
            loadOngoingMeetings();
        } else if (title.equals("Future meetings")) {
            loadFutureMeetings();
        } else if (title.equals("Past meetings")) {
            loadPastMeetings();
        } else if (title.equals("Invitations")) {
            loadInvitations();
        }
    }

    private void loadOngoingMeetings() {
        OngoingMeetingsLoader ongoingMeetingsLoader = new OngoingMeetingsLoader(
                activity.getUserId(), activity.getSID()) {

            @Override
            public void handleResponse(final List<MeetingDTO> response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.dismissLoadingFragment();
                        if (response != null) {
                            meetingsList = response;
                        } else {
                            activity.showToastMessage("Server response fail.");
                        }
                        refreshFragment();
                    }
                });
            }
        };
        activity.showLoadingFragment();
        ongoingMeetingsLoader.retrieveResponse();
    }

    private void loadFutureMeetings() {
        FutureMeetingsLoader futureMeetingsLoader = new FutureMeetingsLoader(
                activity.getUserId(), activity.getSID()) {

            @Override
            public void handleResponse(final List<MeetingDTO> response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.dismissLoadingFragment();
                        if (response != null) {
                            meetingsList = response;
                        } else {
                            activity.showToastMessage("Server response fail.");
                        }
                        refreshFragment();
                    }
                });
            }
        };
        activity.showLoadingFragment();
        futureMeetingsLoader.retrieveResponse();
    }

    private void loadPastMeetings() {
        PastMeetingsLoader pastMeetingsLoader = new PastMeetingsLoader(
                activity.getUserId(), activity.getSID()) {

            @Override
            public void handleResponse(final List<MeetingDTO> response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.dismissLoadingFragment();
                        if (response != null) {
                            meetingsList = response;
                        } else {
                            activity.showToastMessage("Server response fail.");
                        }
                        refreshFragment();
                    }
                });
            }
        };
        activity.showLoadingFragment();
        pastMeetingsLoader.retrieveResponse();
    }

    private void loadInvitations() {
        InvitationsLoader invitationsLoader = new InvitationsLoader(
                activity.getUserId(), activity.getSID()) {

            @Override
            public void handleResponse(final List<MeetingDTO> response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.dismissLoadingFragment();
                        if (response != null) {
                            meetingsList = response;
                        } else {
                            activity.showToastMessage("Server response fail.");
                        }
                        refreshFragment();
                    }
                });
            }
        };
        activity.showLoadingFragment();
        invitationsLoader.retrieveResponse();
    }

    private void refreshFragment() {
        activity.getFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }

    private void refreshListView() {
        ListView listview = (ListView) meetingsListLayout.findViewById(R.id.listView);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                MeetingDTO meeting = (MeetingDTO) adapter.getItem(position);
                ((MainActivity) getActivity()).changeFragment(new MeetingInfoFragment(meeting));
            }
        });

        adapter = new MeetingsAdapter(getActivity(), meetingsList);
        listview.setAdapter(adapter);
    }

}
