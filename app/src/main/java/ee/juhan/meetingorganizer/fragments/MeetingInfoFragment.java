package ee.juhan.meetingorganizer.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.CheckBox;
import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.activities.ShowLocationActivity;
import ee.juhan.meetingorganizer.adapters.GroupedListAdapter;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.MapCoordinate;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.MeetingStatus;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;
import ee.juhan.meetingorganizer.models.server.SendGpsLocationAnswer;
import ee.juhan.meetingorganizer.network.RestClient;
import ee.juhan.meetingorganizer.services.LocationService;
import ee.juhan.meetingorganizer.services.MeetingUpdaterService;
import ee.juhan.meetingorganizer.util.DateUtil;
import ee.juhan.meetingorganizer.util.GsonUtil;
import ee.juhan.meetingorganizer.util.LocationRecommenderUtil;
import ee.juhan.meetingorganizer.util.UIUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MeetingInfoFragment extends Fragment {

	public static final String UPDATE_MEETING_ACTION = "update-meeting";
	private static MeetingInfoFragment meetingInfoFragment;
	private Meeting meeting;
	private String title;
	private MainActivity activity;
	private ViewGroup meetingInfoLayout;
	private List<Participant> participantsList;
	private ParticipantsAdapter participantsAdapter;
	private MeetingInfoReceiver meetingInfoReceiver;

	public static MeetingInfoFragment newInstance(Meeting meeting) {
		MeetingInfoFragment fragment = new MeetingInfoFragment();
		Bundle args = new Bundle();
		GsonUtil.addJsonObjectToBundle(args, meeting, meeting.getClass().getSimpleName());
		fragment.setArguments(args);
		return fragment;
	}

	public static MeetingInfoFragment getFragment() {
		return meetingInfoFragment;
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setMeeting(GsonUtil.getJsonObjectFromBundle(savedInstanceState, Meeting.class,
					Meeting.class.getSimpleName()));
		}
		Bundle args = getArguments();
		if (args != null) {
			setMeeting(GsonUtil.getJsonObjectFromBundle(args, Meeting.class,
					Meeting.class.getSimpleName()));
		}
		activity = (MainActivity) getActivity();
		title = getString(R.string.title_meeting_info);
		participantsList = meeting.getParticipants();
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		meetingInfoLayout =
				(ViewGroup) inflater.inflate(R.layout.fragment_meeting_info, container, false);
		populateLayout();
		setButtonListeners();
		refreshParticipantsListView();
		return meetingInfoLayout;
	}

	@Override
	public final void onResume() {
		super.onResume();
		meetingInfoFragment = this;
		if (meeting.isOngoing()) {
			MeetingUpdaterService.startMeetingUpdaterTask();
		}
		meetingInfoReceiver = new MeetingInfoReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MeetingInfoFragment.UPDATE_MEETING_ACTION);
		getActivity().registerReceiver(meetingInfoReceiver, intentFilter);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState = GsonUtil.addJsonObjectToBundle(savedInstanceState, meeting,
				meeting.getClass().getSimpleName());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public final void onPause() {
		getActivity().unregisterReceiver(meetingInfoReceiver);
		meetingInfoFragment = null;
		super.onPause();
	}

	public Meeting getMeeting() {
		return this.meeting;
	}

	public void setMeeting(Meeting meeting) {
		this.meeting = meeting;
		MeetingUpdaterService.setUpdatedMeeting(meeting);
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
		setUpOngoingMeetingViews();

		if (meeting.getLocation() != null) {
			activity.showLocationFab(true);
		} else {
			activity.showLocationFab(false);
		}
	}

	private void setUpOngoingMeetingViews() {
		if (!meeting.isOngoing() || getParticipantObject() == null ||
				getParticipantObject().getParticipationAnswer() !=
						ParticipationAnswer.PARTICIPATING) {
			return;
		}
		CheckBox chkSendGpsLocation =
				(CheckBox) meetingInfoLayout.findViewById(R.id.chk_send_gps_location);
		chkSendGpsLocation.setVisibility(View.VISIBLE);
		chkSendGpsLocation.setOnCheckedChangeListener((view, isChecked) -> {
			Participant participant = getParticipantObject();
			if (isChecked) {
				LocationService.connect();
				participant.setSendGpsLocationAnswer(SendGpsLocationAnswer.SEND);
			} else {
				LocationService.disconnect();
				participant.setSendGpsLocationAnswer(SendGpsLocationAnswer.NO_SEND);
			}
			sendUpdateGpsLocationAnswer(participant.getSendGpsLocationAnswer(),
					participant.getId());
			refreshParticipantsListView();
		});
		if (getParticipantObject() != null &&
				getParticipantObject().getSendGpsLocationAnswer() == SendGpsLocationAnswer.SEND) {
			chkSendGpsLocation.setCheckedImmediately(true);
		}

		if (meeting.getLeaderId() == MainActivity.getAccountId() &&
				meeting.getStatus() == MeetingStatus.WAITING_LOCATION_CHOICE) {
			Button chooseLocationButton =
					(Button) meetingInfoLayout.findViewById(R.id.btn_choose_location);
			chooseLocationButton.setVisibility(View.VISIBLE);
			chooseLocationButton.setOnClickListener(view -> {
				showChooseLocationDialog();
			});
		}
	}

	private void setButtonListeners() {
		if (meeting.getLocation() != null) {
			FloatingActionButton showLocation =
					(FloatingActionButton) activity.findViewById(R.id.fab_location);
			showLocation.setOnClickListener(view -> startShowLocationActivity(null));
		}
	}

	private void startShowLocationActivity(Bundle args) {
		if (args == null) {
			args = new Bundle();
		}
		Intent intent = new Intent(activity, ShowLocationActivity.class);
		args = GsonUtil.addJsonObjectToBundle(args, meeting, meeting.getClass().getSimpleName());
		intent.putExtras(args);
		startActivity(intent);
	}

	private void setAnswerButtons() {
		final Participant participant = getParticipantObject();
		Button acceptInvitationButton =
				(Button) meetingInfoLayout.findViewById(R.id.btn_accept_invitation);
		Button denyInvitationButton =
				(Button) meetingInfoLayout.findViewById(R.id.btn_deny_invitation);
		if (participant != null &&
				participant.getParticipationAnswer() == ParticipationAnswer.NO_ANSWER &&
				meeting.getEndDateTime().after(new Date())) {
			acceptInvitationButton.setVisibility(View.VISIBLE);
			denyInvitationButton.setVisibility(View.VISIBLE);
			acceptInvitationButton.setOnClickListener(view -> {
				participant.setParticipationAnswer(ParticipationAnswer.PARTICIPATING);
				showSendGpsLocationDialog(participant);
			});
			denyInvitationButton.setOnClickListener(view -> {
				showConfirmationDialog(participant);
			});
		}
	}

	private void showSendGpsLocationDialog(final Participant participant) {
		SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.DialogTheme) {
			@Override
			public void onPositiveActionClicked(DialogFragment fragment) {
				participant.setSendGpsLocationAnswer(SendGpsLocationAnswer.SEND);
				sendUpdateParticipationAnswerRequest(participant);
				super.onPositiveActionClicked(fragment);
			}

			@Override
			public void onNegativeActionClicked(DialogFragment fragment) {
				participant.setSendGpsLocationAnswer(SendGpsLocationAnswer.NO_SEND);
				sendUpdateParticipationAnswerRequest(participant);
				super.onNegativeActionClicked(fragment);
			}
		};
		builder.message(
				"Send your GPS location to other participants so they can track your location.")
				.title("Send GPS location?").positiveAction("AGREE").negativeAction("DISAGREE");
		DialogFragment fragment = DialogFragment.newInstance(builder);
		fragment.show(activity.getSupportFragmentManager(), null);
	}

	private void showConfirmationDialog(final Participant participant) {
		SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.DialogTheme) {
			@Override
			public void onPositiveActionClicked(DialogFragment fragment) {
				participant.setParticipationAnswer(ParticipationAnswer.NOT_PARTICIPATING);
				sendUpdateParticipationAnswerRequest(participant);
				super.onPositiveActionClicked(fragment);
			}

			@Override
			public void onNegativeActionClicked(DialogFragment fragment) {
				super.onNegativeActionClicked(fragment);
			}
		};
		builder.message("You are about to discard the invitation to the meeting.")
				.title("Discard invitation?").positiveAction("OK").negativeAction("CANCEL");
		DialogFragment fragment = DialogFragment.newInstance(builder);
		fragment.show(activity.getSupportFragmentManager(), null);
	}

	private void showChooseLocationDialog() {
		SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.DialogTheme) {
			@Override
			public void onPositiveActionClicked(DialogFragment fragment) {
				showRecommendedLocation();
				super.onPositiveActionClicked(fragment);
			}

			@Override
			public void onNegativeActionClicked(DialogFragment fragment) {
				super.onNegativeActionClicked(fragment);
			}
		};

		int participantsWhoAnswered = 0;
		for (Participant participant : meeting.getParticipants()) {
			if (participant.getSendGpsLocationAnswer() == SendGpsLocationAnswer.SEND) {
				participantsWhoAnswered++;
			}
		}

		builder.message("The recommended locations depend on the locations of all participants. " +
				"Currently " + participantsWhoAnswered + " participants out of " +
				meeting.getParticipants().size() +
				" have sent their GPS location. Continue anyway?").title("Choose meeting location?")
				.positiveAction("OK").negativeAction("Cancel");
		DialogFragment fragment = DialogFragment.newInstance(builder);
		fragment.show(activity.getSupportFragmentManager(), null);
	}

	private void showRecommendedLocation() {
		activity.showProgress(true);
		MapCoordinate recommendedLocation = LocationRecommenderUtil.getCenterCoordinate(meeting);
		if (meeting.getLocationType() == LocationType.GENERATED_FROM_PREFERRED_LOCATIONS) {
			List<MapCoordinate> preferredLocationsSorted = LocationRecommenderUtil
					.getUserPreferredSortedByRecommendation(meeting, recommendedLocation);
			meeting.setUserPreferredLocations(preferredLocationsSorted);
			Bundle args = new Bundle();
			args.putBoolean(ShowLocationActivity.CHOOSE_LOCATION_ARG, true);
			startShowLocationActivity(args);
		}
		activity.showProgress(false);
	}

	private void sendUpdateParticipationAnswerRequest(Participant participant) {
		activity.showProgress(true);
		RestClient.get().updateParticipationAnswerRequest(participant.getParticipationAnswer(),
				participant.getId(), new Callback<ResponseBody>() {
					@Override
					public void success(ResponseBody responseBody, Response response) {
						activity.showProgress(false);
						populateLayout();
					}

					@Override
					public void failure(RetrofitError error) {
						activity.showProgress(false);
						UIUtil.showToastMessage(activity, getString(R.string.error_server_fail));
					}
				});
	}

	private void sendUpdateGpsLocationAnswer(SendGpsLocationAnswer sendGpsLocationAnswer,
			int participantId) {
		RestClient.get().updateSendGpsLocationAnswer(sendGpsLocationAnswer, participantId,
				new Callback<ResponseBody>() {
					@Override
					public void success(ResponseBody responseBody, Response response) {

					}

					@Override
					public void failure(RetrofitError error) {
						UIUtil.showToastMessage(activity, getString(R.string.error_server_fail));
					}
				});
	}

	private Participant getParticipantObject() {
		int accountId = MainActivity.getAccountId();
		for (Participant participant : meeting.getParticipants()) {
			if (participant.getAccountId() == accountId) {
				return participant;
			}
		}
		return null;
	}

	private void refreshParticipantsListView() {
		participantsList = meeting.getParticipants();
		ListView participantsListView =
				(ListView) meetingInfoLayout.findViewById(R.id.list_view_participants);
		participantsListView.setOnItemClickListener((parent, view, position, id) -> {

			GroupedListAdapter.GroupedListItem item = participantsAdapter.getItem(position);
			if (!item.isGroupItem()) {
				Participant participant = (Participant) item.getObject();
				activity.changeFragmentToParticipantInfo(participant);
			}
		});
		participantsAdapter = new ParticipantsAdapter(getActivity(), getGroupedListItems());
		participantsListView.setAdapter(participantsAdapter);
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
			super(context, R.layout.list_item_participant, listItems);
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
				if (participant.getSendGpsLocationAnswer() != SendGpsLocationAnswer.SEND) {
					super.addIcon(R.drawable.ic_remove_marker_black_18dp, R.color.dark_red);
				} else {
					super.addIcon(R.drawable.ic_marker_black_18dp, R.color.green);
				}
			}
		}

	}

	private class MeetingInfoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Meeting newMeetingInfo = GsonUtil.getJsonObjectFromIntentExtras(intent, Meeting.class,
					Meeting.class.getSimpleName());
			if (getMeeting().getId() == newMeetingInfo.getId()) {
				setMeeting(newMeetingInfo);
				populateLayout();
				refreshParticipantsListView();
			}
		}
	}

}
