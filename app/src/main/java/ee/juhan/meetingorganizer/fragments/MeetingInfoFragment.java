package ee.juhan.meetingorganizer.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Date;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.fragments.listeners.MyLocationListener;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
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
	private MeetingDTO meeting;
	private CustomMapFragment customMapFragment = new CustomMapFragment();

	public MeetingInfoFragment() {
	}

	public static MeetingInfoFragment newInstance(MeetingDTO meeting) {
		MeetingInfoFragment fragment = new MeetingInfoFragment();
		fragment.setMeeting(meeting);
		return fragment;
	}

	private void populateLayout() {
		TextView title = (TextView) meetingInfoLayout.findViewById(R.id.meeting_title);
		TextView description = (TextView) meetingInfoLayout.findViewById(R.id.meeting_description);
		TextView date = (TextView) meetingInfoLayout.findViewById(R.id.meeting_date);
		TextView time = (TextView) meetingInfoLayout.findViewById(R.id.meeting_time);
		title.setText(
				String.format("%s: %s", getString(R.string.textview_title), meeting.getTitle()));
		if (meeting.getDescription().trim().isEmpty()) {
			description
					.setText(String.format("%s: None", getString(R.string.textview_description)));
		} else {
			description.setText(String.format("%s: %s", getString(R.string.textview_description),
					meeting.getDescription()));
		}
		date.setText(String.format("%s: %s", getString(R.string.textview_date),
				DateUtil.formatDate(meeting.getStartDateTime())));
		time.setText(String.format("%s: %s - %s", getString(R.string.textview_time),
				DateUtil.formatTime(meeting.getStartDateTime()),
				DateUtil.formatTime(meeting.getEndDateTime())));

		setUpMapFragment();
		setAnswerButtons();
	}

	private void setUpMapFragment() {
		customMapFragment = new CustomMapFragment();
		getFragmentManager().beginTransaction().
				replace(R.id.location_frame, customMapFragment).commit();
		FrameLayout layout = (FrameLayout) meetingInfoLayout.findViewById(R.id.location_frame);
		layout.setBackgroundResource(R.drawable.view_border);
		//		if (meeting.getLocation() != null) {
		//			customMapFragment.setLocationMarker(new LatLng(meeting.getLocation().getLatitude(),
		//							meeting.getLocation().getLongitude()));
		//		}
	}

	private void setButtonListeners() {
		Button showParticipants = (Button) meetingInfoLayout.findViewById(R.id.show_participants);
		showParticipants.setOnClickListener(view -> ((MainActivity) getActivity())
				.changeFragmentToParticipantsList(meeting.getParticipants()));
	}

	private void setAnswerButtons() {
		final ParticipantDTO participantObject = getParticipantObject();
		assert participantObject != null;
		if (participantObject.getParticipationAnswer() == ParticipationAnswer.NOT_ANSWERED &&
				meeting.getEndDateTime().after(new Date())) {
			Button acceptInvitation =
					(Button) meetingInfoLayout.findViewById(R.id.accept_invitation);
			Button denyInvitation = (Button) meetingInfoLayout.findViewById(R.id.deny_invitation);
			acceptInvitation.setOnClickListener(view -> {
				if (meeting.getLocationType() == LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS &&
						MyLocationListener.getMyLocation() != null ||
						meeting.getLocationType() == LocationType.SPECIFIC_LOCATION) {
					participantObject.setLocation(MyLocationListener.getMyLocation());
					participantObject.setParticipationAnswer(ParticipationAnswer.PARTICIPATING);
					sendUpdateParticipantRequest(participantObject);
				} else {
					UIUtil.showToastMessage(activity,
							getString(R.string.toast_please_get_your_location));
				}
			});
			denyInvitation.setOnClickListener(view -> {
				if (meeting.getLocationType() == LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS &&
						MyLocationListener.getMyLocation() != null ||
						meeting.getLocationType() == LocationType.SPECIFIC_LOCATION) {
					participantObject.setLocation(MyLocationListener.getMyLocation());
					participantObject.setParticipationAnswer(ParticipationAnswer.NOT_PARTICIPATING);
					sendUpdateParticipantRequest(participantObject);
				} else {
					UIUtil.showToastMessage(activity,
							getString(R.string.toast_please_get_your_location));
				}
			});
		} else {
			ViewGroup answerButtons =
					(ViewGroup) meetingInfoLayout.findViewById(R.id.invitation_answer_buttons);
			answerButtons.setVisibility(View.GONE);
		}
	}

	private void sendUpdateParticipantRequest(ParticipantDTO participantDTO) {
		activity.showProgress(true);
		RestClient.get().updateParticipantRequest(participantDTO, meeting.getId(),
				new Callback<MeetingDTO>() {
					@Override
					public void success(MeetingDTO meetingDTO, Response response) {
						activity.showProgress(false);
						meeting = meetingDTO;
						meeting.toUTCTimeZone();
						populateLayout();
					}

					@Override
					public void failure(RetrofitError error) {
						activity.showProgress(false);
						UIUtil.showToastMessage(activity, getString(R.string.toast_server_fail));
					}
				});
	}

	private ParticipantDTO getParticipantObject() {
		int accountId = activity.getUserId();
		for (ParticipantDTO participant : meeting.getParticipants()) {
			if (participant.getAccountId() == accountId) {
				return participant;
			}
		}
		return null;
	}

	@Override
	public final Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
		final Animator anim = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			anim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					customMapFragment.setMapVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationStart(Animator animation) {
					customMapFragment.setMapVisibility(View.INVISIBLE);
				}
			});
		}
		return anim;
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		title = getString(R.string.title_meeting_info);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		if (meeting == null) {
			meetingInfoLayout =
					(ViewGroup) inflater.inflate(R.layout.fragment_no_data, container, false);
			TextView infoText = (TextView) meetingInfoLayout.findViewById(R.id.info_text);
			infoText.setText(getString(R.string.textview_no_info));
		} else {
			meetingInfoLayout =
					(ViewGroup) inflater.inflate(R.layout.fragment_meeting_info, container, false);
			populateLayout();
			setButtonListeners();
		}
		return meetingInfoLayout;
	}

	public void setMeeting(MeetingDTO meeting) {
		this.meeting = meeting;
	}
}
