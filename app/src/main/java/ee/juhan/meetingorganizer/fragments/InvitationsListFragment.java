package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
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
    private OnFragmentInteractionListener mListener;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
