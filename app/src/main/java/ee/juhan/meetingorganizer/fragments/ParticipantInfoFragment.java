package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.models.server.ParticipationAnswer;

public class ParticipantInfoFragment extends Fragment {

	private String title;
	private MainActivity activity;
	private ViewGroup participantInfoLayout;
	private ParticipantDTO participant;

	public ParticipantInfoFragment() {}

	@SuppressLint("ValidFragment")
	public ParticipantInfoFragment(ParticipantDTO participant) {
		this.participant = participant;
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
		participantInfoLayout =
				(ViewGroup) inflater.inflate(R.layout.fragment_participant_info, container, false);
		populateLayout(participant);
		return participantInfoLayout;
	}

	private void populateLayout(ParticipantDTO participant) {
		TextView name = (TextView) participantInfoLayout.findViewById(R.id.participant_name);
		TextView phoneNumber =
				(TextView) participantInfoLayout.findViewById(R.id.participant_phone_number);
		TextView email = (TextView) participantInfoLayout.findViewById(R.id.participant_email);
		TextView answer = (TextView) participantInfoLayout.findViewById(R.id.participant_answer);
		name.setText(getString(R.string.textview_name) + ": " + participant.getName());
		phoneNumber.setText(
				getString(R.string.textview_phone_number) + ": " + participant.getPhoneNumber());
		email.setText(getString(R.string.textview_email) + ": " + participant.getEmail());
		if (participant.getParticipationAnswer() == ParticipationAnswer.PARTICIPATING) {
			answer.setText(getString(R.string.textview_participating) + ": " +
					getString(R.string.textview_yes));
		} else if (participant.getParticipationAnswer() == ParticipationAnswer.NOT_PARTICIPATING) {
			answer.setText(getString(R.string.textview_participating) + ": " +
					getString(R.string.textview_no));
		} else if (participant.getParticipationAnswer() == ParticipationAnswer.NOT_ANSWERED) {
			answer.setText(getString(R.string.textview_participating) + ": " +
					getString(R.string.textview_not_answered));
		}
	}

}
