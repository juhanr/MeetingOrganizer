package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
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
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.rest.RestClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressLint("ValidFragment")
public class MeetingsListFragment extends Fragment {

    static final public String ONGOING_MEETINGS = "ongoing-meetings";
    static final public String FUTURE_MEETINGS = "future-meetings";
    static final public String PAST_MEETINGS = "past-meetings";
    static final public String INVITATIONS = "invitations";
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
            infoText.setText("No meetings found.");
        } else {
            meetingsListLayout = (LinearLayout) inflater.inflate(R.layout.layout_listview, container, false);
            refreshListView();
        }
        return meetingsListLayout;
    }

    public void getMeetingsRequest(String meetingsType) {
        activity.showLoadingFragment();
        RestClient.get().getMeetingsRequest(meetingsType, activity.getUserId(),
                new Callback<List<MeetingDTO>>() {
                    @Override
                    public void success(final List<MeetingDTO> serverResponse, Response response) {
                        activity.dismissLoadingFragment();
                        meetingsList = serverResponse;
                        refreshFragment();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.dismissLoadingFragment();
                        activity.showToastMessage("Server response fail.");
                        if (error.getMessage() != null)
                            Log.d("DEBUG", String.valueOf(error.getMessage()));
                    }
                });
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
