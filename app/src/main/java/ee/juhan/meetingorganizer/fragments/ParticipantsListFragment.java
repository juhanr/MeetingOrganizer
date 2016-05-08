package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.adapters.GeneralAdapter;
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
		Log.d("DEBUG", String.valueOf(participantsList.size()));
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		title = getString(R.string.title_participants);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		if (participantsList == null || participantsList.size() == 0) {
			participantsListLayout =
					(ViewGroup) inflater.inflate(R.layout.fragment_no_data, container, false);
			TextView infoText = (TextView) participantsListLayout.findViewById(R.id.info_text);
			infoText.setText(getString(R.string.textview_no_participants));
		} else {
			participantsListLayout =
					(ViewGroup) inflater.inflate(R.layout.layout_listview, container, false);
			refreshListView();
		}
		return participantsListLayout;
	}

	private void refreshListView() {
		ListView listview = (ListView) participantsListLayout.findViewById(R.id.listView);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				ParticipantDTO participant = adapter.getItem(position);
				activity.changeFragmentToParticipantInfo(participant);
			}
		});
		adapter = new ParticipantsAdapter(getActivity(), participantsList);
		listview.setAdapter(adapter);
	}

	private class ParticipantsAdapter extends GeneralAdapter<ParticipantDTO> {

		public ParticipantsAdapter(Context context, List<ParticipantDTO> participantsList) {
			super(context, R.layout.list_item_participants, participantsList);
		}

		@Override
		protected void populateLayout() {
			ParticipantDTO participant = super.getCurrentItem();
			TextView participantNameView =
					(TextView) super.getLayout().findViewById(R.id.participant_name);
			if (participant.getName() == null) {
				participantNameView.setText("Unknown");
			} else {
				participantNameView.setText(participant.getName());
			}
			switch (participant.getParticipationAnswer()) {
				case PARTICIPATING:
					super.addIcon(R.drawable.ic_check_mark);
					break;
				case NOT_ANSWERED:
					super.addIcon(R.drawable.ic_question_mark);
					break;
			}
			if (participant.getAccountId() != 0) {
				super.addIcon(R.drawable.ic_account);
			}
		}

	}

}