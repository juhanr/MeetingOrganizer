package ee.juhan.meetingorganizer.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.LocationActivity;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.adapters.GroupedListAdapter;
import ee.juhan.meetingorganizer.fragments.listeners.LocationClient;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;
import ee.juhan.meetingorganizer.rest.RestClient;
import ee.juhan.meetingorganizer.util.DateUtil;
import ee.juhan.meetingorganizer.util.UIUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MeetingInfoFragment extends Fragment {

	private String title;
	private MainActivity activity;
	private ViewGroup meetingInfoLayout;
	private Meeting meeting;

	private List<Participant> participantsList;
	private ParticipantsAdapter adapter;

	public static MeetingInfoFragment newInstance(Meeting meeting) {
		MeetingInfoFragment fragment = new MeetingInfoFragment();
		fragment.setMeeting(meeting);
		return fragment;
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		title = getString(R.string.title_meeting_info);
		participantsList = meeting.getParticipants();
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		if (meeting == null) {
			meetingInfoLayout =
					(ViewGroup) inflater.inflate(R.layout.fragment_no_data, container, false);
			TextView infoText = (TextView) meetingInfoLayout.findViewById(R.id.info_text);
			infoText.setText(getString(R.string.meeting_no_info));
		} else {
			meetingInfoLayout =
					(ViewGroup) inflater.inflate(R.layout.fragment_meeting_info, container, false);
			populateLayout();
			setButtonListeners();
			refreshParticipantsListView();
		}
		return meetingInfoLayout;
	}

	@Override
	public void onDestroyView() {
		activity.showLocationFab(false);
		super.onDestroyView();
	}

	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
	}

	private void populateLayout() {
		TextView title = (TextView) meetingInfoLayout.findViewById(R.id.txt_meeting_title);
		TextView status = (TextView) meetingInfoLayout.findViewById(R.id.txt_meeting_status);
		TextView description =
				(TextView) meetingInfoLayout.findViewById(R.id.txt_meeting_description);
		TextView date = (TextView) meetingInfoLayout.findViewById(R.id.txt_meeting_date);
		TextView time = (TextView) meetingInfoLayout.findViewById(R.id.txt_meeting_time);

		title.setText(meeting.getTitle());

		switch (meeting.getStatus()) {
			case ACTIVE:
				status.setVisibility(View.GONE);
				break;
			case CANCELLED:
				status.setText(getString(R.string.meeting_cancelled));
				break;
			case WAITING_LOCATION_CHOICE:
				status.setText(getString(R.string.meeting_waiting_location_choice));
				break;
			case WAITING_PARTICIPANT_ANSWERS:
				status.setText(getString(R.string.meeting_waiting_participant_answers));
				break;
		}

		if (meeting.getDescription().trim().isEmpty()) {
			description.setVisibility(View.GONE);
		} else {
			description.setText(meeting.getDescription());
		}
		date.setText(DateUtil.formatDate(meeting.getStartDateTime()));
		time.setText(String.format("%s - %s", DateUtil.formatTime(meeting.getStartDateTime()),
				DateUtil.formatTime(meeting.getEndDateTime())));

		setAnswerButtons();

		if (meeting.isOngoing()) {
			setUpOngoingMeetingViews();
		}
	}

	private void setUpOngoingMeetingViews() {

	}

	private void setButtonListeners() {
		if (meeting.getLocation() != null) {
			activity.showLocationFab(true);
			FloatingActionButton showLocation =
					(FloatingActionButton) activity.findViewById(R.id.fab_location);
			showLocation.setOnClickListener(view -> {
				Intent intent = new Intent(activity, LocationActivity.class);
				intent.putExtra(LocationActivity.SHOW_LOCATION_OPTIONS, false);
				intent.putExtra(LocationActivity.MARKER_LATITUDE,
						meeting.getLocation().getLatitude());
				intent.putExtra(LocationActivity.MARKER_LONGITUDE,
						meeting.getLocation().getLongitude());
				startActivity(intent);
			});
		}
	}

	private void setAnswerButtons() {
		final Participant participantObject = getParticipantObject();
		if (participantObject != null &&
				participantObject.getParticipationAnswer() == ParticipationAnswer.NO_ANSWER &&
				meeting.getEndDateTime().after(new Date())) {
			Button acceptInvitation =
					(Button) meetingInfoLayout.findViewById(R.id.accept_invitation);
			Button denyInvitation = (Button) meetingInfoLayout.findViewById(R.id.deny_invitation);
			acceptInvitation.setOnClickListener(view -> {
				if (meeting.getLocationType() == LocationType.GENERATED_FROM_PREFERRED_LOCATIONS &&
						LocationClient.getMyLocation() != null ||
						meeting.getLocationType() == LocationType.SPECIFIC_LOCATION) {
					participantObject.setLocation(LocationClient.getMyLocation());
					participantObject.setParticipationAnswer(ParticipationAnswer.PARTICIPATING);
					sendUpdateParticipationAnswerRequest(participantObject);
				} else {
					UIUtil.showToastMessage(activity,
							getString(R.string.location_get_your_location));
				}
			});
			denyInvitation.setOnClickListener(view -> {
				if (meeting.getLocationType() == LocationType.GENERATED_FROM_PREFERRED_LOCATIONS &&
						LocationClient.getMyLocation() != null ||
						meeting.getLocationType() == LocationType.SPECIFIC_LOCATION) {
					participantObject.setLocation(LocationClient.getMyLocation());
					participantObject.setParticipationAnswer(ParticipationAnswer.NOT_PARTICIPATING);
					sendUpdateParticipationAnswerRequest(participantObject);
				} else {
					UIUtil.showToastMessage(activity,
							getString(R.string.location_get_your_location));
				}
			});
		} else {
			ViewGroup answerButtons =
					(ViewGroup) meetingInfoLayout.findViewById(R.id.invitation_answer_buttons);
			answerButtons.setVisibility(View.GONE);
		}
	}

	private void sendUpdateParticipationAnswerRequest(Participant participant) {
		activity.showProgress(true);
		RestClient.get().updateParticipationAnswerRequest(participant, meeting.getId(),
				new Callback<Meeting>() {
					@Override
					public void success(Meeting meeting, Response response) {
						activity.showProgress(false);
						MeetingInfoFragment.this.meeting = meeting;
						populateLayout();
					}

					@Override
					public void failure(RetrofitError error) {
						activity.showProgress(false);
						UIUtil.showToastMessage(activity, getString(R.string.error_server_fail));
					}
				});
	}

	private Participant getParticipantObject() {
		int accountId = activity.getAccountId();
		for (Participant participant : meeting.getParticipants()) {
			if (participant.getAccountId() == accountId) {
				return participant;
			}
		}
		return null;
	}

	private void refreshParticipantsListView() {
		ListView listview = (ListView) meetingInfoLayout.findViewById(R.id.list_view_participants);
		listview.setOnItemClickListener((parent, view, position, id) -> {

			GroupedListAdapter.GroupedListItem item = adapter.getItem(position);
			if (!item.isGroupItem()) {
				Participant participant = (Participant) item.getObject();
				activity.changeFragmentToParticipantInfo(participant);
			}
		});
		adapter = new ParticipantsAdapter(getActivity(), getGroupedListItems());
		listview.setAdapter(adapter);
	}

	private List<GroupedListAdapter.GroupedListItem> getGroupedListItems() {
		List<GroupedListAdapter.GroupedListItem> attending = new ArrayList<>();
		attending.add(new GroupedListAdapter.GroupedListItem("Going"));
		List<GroupedListAdapter.GroupedListItem> notAttending = new ArrayList<>();
		notAttending.add(new GroupedListAdapter.GroupedListItem("Not going"));
		List<GroupedListAdapter.GroupedListItem> invited = new ArrayList<>();
		invited.add(new GroupedListAdapter.GroupedListItem("Invited"));
		for (Participant participant : participantsList) {
			switch (participant.getParticipationAnswer()) {
				case PARTICIPATING:
					attending.add(new GroupedListAdapter.GroupedListItem<>(participant));
					break;
				case NOT_PARTICIPATING:
					notAttending.add(new GroupedListAdapter.GroupedListItem<>(participant));
					break;
				case NO_ANSWER:
					invited.add(new GroupedListAdapter.GroupedListItem<>(participant));
					break;
			}
		}
		List<GroupedListAdapter.GroupedListItem> groupedListItems = new ArrayList<>();
		if (attending.size() > 1) {
			groupedListItems.addAll(attending);
		}
		if (notAttending.size() > 1) {
			groupedListItems.addAll(notAttending);
		}
		if (invited.size() > 1) {
			groupedListItems.addAll(invited);
		}
		return groupedListItems;
	}

	private class ParticipantsAdapter extends GroupedListAdapter {

		public ParticipantsAdapter(Context context, List<GroupedListItem> listItems) {
			super(context, R.layout.list_item_participants, listItems);
		}

		@Override
		protected void populateLayout() {
			Participant participant = (Participant) super.getCurrentItem().getObject();
			TextView participantNameView =
					(TextView) super.getLayout().findViewById(R.id.participant_name);
			if (participant.getName() == null) {
				participantNameView.setText(getString(R.string.msg_unknown));
			} else {
				participantNameView.setText(participant.getName());
			}
			if (participant.getAccountId() != 0) {
				super.addIcon(R.drawable.ic_account_box_black_18dp, R.color.dark_gray);
			}
			if (meeting.isOngoing()) {
				if (participant.getLocation() == null) {
					super.addIcon(R.drawable.ic_remove_marker_black_18dp, R.color.red);
				} else {
					super.addIcon(R.drawable.ic_marker_black_18dp, R.color.green);
				}
			}
		}

	}

}
