package ee.juhan.meetingorganizer.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;

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
		title = getString(R.string.title_participant_info);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity.setTitle(title);
		participantInfoLayout =
				(ViewGroup) inflater.inflate(R.layout.fragment_participant_info, container, false);
		setupFABListeners();
		populateLayout(participant);
		return participantInfoLayout;
	}

	@Override
	public void onDestroyView() {
		activity.showEmailFAB(false);
		activity.showSmsFAB(false);
		activity.showCallFAB(false);
		super.onDestroyView();
	}

	private void populateLayout(ParticipantDTO participant) {
		TextView name = (TextView) participantInfoLayout.findViewById(R.id.participant_name);
		TextView phoneNumber =
				(TextView) participantInfoLayout.findViewById(R.id.participant_phone_number);
		TextView email = (TextView) participantInfoLayout.findViewById(R.id.participant_email);
		name.setText(participant.getName());
		phoneNumber.setText(participant.getPhoneNumber());
		if (participant.getEmail() == null || participant.getEmail().isEmpty()) {
			email.setVisibility(View.GONE);
			participantInfoLayout.findViewById(R.id.img_email).setVisibility(View.GONE);
			activity.showEmailFAB(false);
		} else {
			email.setText(participant.getEmail());
			activity.showEmailFAB(true);
		}
		activity.showSmsFAB(true);
		activity.showCallFAB(true);
	}

	private void setupFABListeners() {
		FloatingActionButton emailFAB =
				(FloatingActionButton) activity.findViewById(R.id.fab_email);
		FloatingActionButton smsFAB = (FloatingActionButton) activity.findViewById(R.id.fab_sms);
		FloatingActionButton callFAB = (FloatingActionButton) activity.findViewById(R.id.fab_call);

		emailFAB.setOnClickListener(view -> {
			Intent emailIntent =
					new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + participant.getEmail()));
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{participant.getEmail()});
			if (emailIntent.resolveActivity(activity.getPackageManager()) != null) {
				startActivity(Intent.createChooser(emailIntent, "Send e-mail..."));
			}
		});

		smsFAB.setOnClickListener(view -> {
			Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse("sms:" + participant.getPhoneNumber()));
			if (smsIntent.resolveActivity(activity.getPackageManager()) != null) {
				startActivity(Intent.createChooser(smsIntent, "Send SMS..."));
			}
		});

		callFAB.setOnClickListener(view -> {
			Intent callIntent = new Intent(Intent.ACTION_CALL,
					Uri.parse("tel:" + participant.getPhoneNumber()));
			if (callIntent.resolveActivity(activity.getPackageManager()) != null) {
				startActivity(callIntent);
			}
		});
	}

}
