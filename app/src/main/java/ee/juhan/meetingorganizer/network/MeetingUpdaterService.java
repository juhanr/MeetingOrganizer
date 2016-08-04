package ee.juhan.meetingorganizer.network;

import android.os.Handler;
import android.util.Log;

import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MeetingUpdaterService {

	private static final String TAG = LocationService.class.getSimpleName();
	private static final int UPDATE_INTERVAL = 5000; // 5 seconds by default, can be changed later
	private static Handler meetingUpdateHandler = new Handler();
	private static boolean requestRunning = false;

	static Runnable meetingUpdater = new Runnable() {
		@Override
		public void run() {
			try {
				getCurrentMeetingRequest();
			} finally {
				if (MeetingInfoFragment.getFragment() != null ||
						(CustomMapFragment.isMapInitialized() &&
								CustomMapFragment.canShowParticipantMarkers())) {
					meetingUpdateHandler.postDelayed(meetingUpdater, UPDATE_INTERVAL);
				} else {
					stopRepeatingTask();
				}
			}
		}
	};

	public static void startMeetingUpdaterTask() {
		Log.d(TAG, "Started service.");
	}

	private static void stopRepeatingTask() {
		Log.d(TAG, "Stopped service.");
		meetingUpdateHandler.removeCallbacks(meetingUpdater);
	}

	private static void getCurrentMeetingRequest() {
		Meeting meeting = MeetingInfoFragment.getMeeting();
		if (meeting == null || requestRunning) {
			return;
		}
		requestRunning = true;
		RestClient.get().getMeetingRequest(meeting.getId(), new Callback<Meeting>() {
			@Override
			public void success(Meeting responseMeeting, Response response) {
				requestRunning = false;
				updateCurrentMeeting(responseMeeting);
			}

			@Override
			public void failure(RetrofitError error) {
				Log.d(TAG, "Request failed.");
				requestRunning = false;
			}
		});
	}

	private static void updateCurrentMeeting(Meeting newMeetingInfo) {
		MeetingsListFragment.updateSingleMeeting(newMeetingInfo);

		// Check if the result meeting from the server is the same as the one open in UI.
		if (newMeetingInfo.getId() != MeetingInfoFragment.getMeeting().getId() ||
				MeetingInfoFragment.getFragment() == null) {
			return;
		}

		MeetingInfoFragment.setMeeting(newMeetingInfo);
		MeetingInfoFragment.getFragment().refreshLayoutIfVisible();

		for (Participant participant : newMeetingInfo.getParticipants()) {
			CustomMapFragment.updateParticipantMarker(participant);
		}
	}
}
