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
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;
import ee.juhan.meetingorganizer.models.server.SendGpsLocationAnswer;
import ee.juhan.meetingorganizer.network.LocationService;
import ee.juhan.meetingorganizer.network.MeetingUpdaterService;
import ee.juhan.meetingorganizer.network.RestClient;
import ee.juhan.meetingorganizer.util.DateUtil;
import ee.juhan.meetingorganizer.util.UIUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MeetingInfoFragment extends Fragment {

	private static MeetingInfoFragment meetingInfoFragment;
	private static Meeting meeting;
	private String title;
	private MainActivity activity;
	private ViewGroup meetingInfoLayout;
	private List<Participant> participantsList;
	private ParticipantsAdapter participantsAdapter;

	public static MeetingInfoFragment newInstance(Meeting meeting) {
		MeetingInfoFragment fragment = new MeetingInfoFragment();
		MeetingInfoFragment.setMeeting(meeting);
		return fragment;
	}

	public static Meeting getMeeting() {
		return MeetingInfoFragment.meeting;
	}

	public static void setMeeting(Meeting meeting) {
		MeetingInfoFragment.meeting = meeting;
	}

	public static MeetingInfoFragment getFragment() {
		return meetingInfoFragment;
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
		meetingInfoFragment = this;
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
			if (meeting.isOngoing()) {
				MeetingUpdaterService.startMeetingUpdaterTask();
			}
		}
		return meetingInfoLayout;
	}

	@Override
	public void onDestroyView() {
		activity.showLocationFab(false);
		meetingInfoFragment = null;
		super.onDestroyView();
	}

	public void refreshLayoutIfVisible() {
		if (this.isVisible()) {
			populateLayout();
			refreshParticipantsListView();
		}
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
	}

	private void setButtonListeners() {
		if (meeting.getLocation() != null) {
			activity.showLocationFab(true);
			FloatingActionButton showLocation =
					(FloatingActionButton) activity.findViewById(R.id.fab_location);
			showLocation.setOnClickListener(view -> {
				Intent intent = new Intent(activity, ShowLocationActivity.class);
				intent.putExtra(ShowLocationActivity.MARKER_LATITUDE,
						meeting.getLocation().getLatitude());
				intent.putExtra(ShowLocationActivity.MARKER_LONGITUDE,
						meeting.getLocation().getLongitude());
				startActivity(intent);
			});
		}
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
			acceptInvitationButton.setOnClickListener(view -> {
				participant.setParticipationAnswer(ParticipationAnswer.PARTICIPATING);
				showSendGpsLocationDialog(participant);
			});
			denyInvitationButton.setOnClickListener(view -> {
				showConfirmationDialog(participant);
			});
		} else {
			acceptInvitationButton.setVisibility(View.GONE);
			denyInvitationButton.setVisibility(View.GONE);
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
				if (participant.getLocation() == null ||
						participant.getSendGpsLocationAnswer() != SendGpsLocationAnswer.SEND) {
					super.addIcon(R.drawable.ic_remove_marker_black_18dp, R.color.dark_red);
				} else {
					super.addIcon(R.drawable.ic_marker_black_18dp, R.color.green);
				}
			}
		}

	}

}
