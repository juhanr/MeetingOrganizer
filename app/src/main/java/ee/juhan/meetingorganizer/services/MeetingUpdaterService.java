package ee.juhan.meetingorganizer.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.fragments.MeetingInfoFragment;
import ee.juhan.meetingorganizer.fragments.MeetingsListFragment;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.network.RestClient;
import ee.juhan.meetingorganizer.util.GsonUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MeetingUpdaterService {

	private static final String TAG = MeetingUpdaterService.class.getSimpleName();
	private static final int UPDATE_INTERVAL = 5000;
	private static Handler meetingUpdateHandler = new Handler();
	private static boolean isRunning;
	private static boolean requestRunning;
	private static Context context;
	private static Meeting updatedMeeting;

	private static Runnable meetingUpdater = new Runnable() {
		@Override
		public void run() {
			try {
				getCurrentMeetingRequest();
			} finally {
				if (MeetingInfoFragment.getFragment() != null ||
						(CustomMapFragment.getFragment() != null &&
								CustomMapFragment.canShowParticipantMarkers())) {
					meetingUpdateHandler.postDelayed(meetingUpdater, UPDATE_INTERVAL);
				} else {
					stopRepeatingTask();
				}
			}
		}
	};

	public static void startMeetingUpdaterTask() {
		if (!isRunning) {
			Log.d(TAG, "Started service.");
			meetingUpdater.run();
			isRunning = true;
		}
	}

	public static void setContext(Context context) {
		MeetingUpdaterService.context = context;
	}

	public static void setUpdatedMeeting(Meeting updatedMeeting) {
		MeetingUpdaterService.updatedMeeting = updatedMeeting;
	}

	private static void stopRepeatingTask() {
		if (isRunning) {
			Log.d(TAG, "Stopped service.");
			meetingUpdateHandler.removeCallbacks(meetingUpdater);
			isRunning = false;
		}
	}

	private static void getCurrentMeetingRequest() {
		if (updatedMeeting == null || requestRunning) {
			return;
		}
		requestRunning = true;
		RestClient.get().getMeetingRequest(updatedMeeting.getId(), new Callback<Meeting>() {
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
		if (context != null) {
			Intent intent = new Intent();
			intent.setAction(MeetingInfoFragment.UPDATE_MEETING_ACTION);
			intent = GsonUtil.addJsonObjectToIntentExtras(intent, newMeetingInfo,
					newMeetingInfo.getClass().getSimpleName());
			context.sendBroadcast(intent);
		}

		if (CustomMapFragment.getFragment() != null) {
			Intent intent = new Intent();
			intent.setAction(CustomMapFragment.UPDATE_PARTICIPANT_MARKERS_ACTION);
			intent = GsonUtil.addJsonObjectToIntentExtras(intent, newMeetingInfo,
					newMeetingInfo.getClass().getSimpleName());
			CustomMapFragment.getFragment().getActivity().sendBroadcast(intent);
		}
	}
}
