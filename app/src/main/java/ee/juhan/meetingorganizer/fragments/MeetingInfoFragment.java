package ee.juhan.meetingorganizer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.reflect.TypeToken;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.CheckBox;
import com.squareup.okhttp.ResponseBody;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.activities.ShowLocationActivity;
import ee.juhan.meetingorganizer.adapters.GroupedListAdapter;
import ee.juhan.meetingorganizer.models.googleplaces.Place;
import ee.juhan.meetingorganizer.models.googleplaces.PlaceSearchResult;
import ee.juhan.meetingorganizer.models.server.LocationChoice;
import ee.juhan.meetingorganizer.models.server.MapLocation;
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
import ee.juhan.meetingorganizer.util.LocationUtil;
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 1:
				if (resultCode == Activity.RESULT_OK) {
					meeting = GsonUtil.getJsonObjectFromIntentExtras(data, Meeting.class,
							Meeting.class.getSimpleName());
					sendUpdateMeetingRequest();
					populateLayout();
				}
				break;
		}
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
		ImageView locationImage = (ImageView) meetingInfoLayout.findViewById(R.id.img_location);
		TextView locationName = (TextView) meetingInfoLayout.findViewById(R.id.txt_location_name);

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

		if (meeting.getMapLocation() != null) {
			locationImage.setVisibility(View.VISIBLE);
			locationName.setVisibility(View.VISIBLE);
			String meetingPlaceName = meeting.getMapLocation().getPlaceName();
			String meetingAddress = meeting.getMapLocation().getAddress();
			if (meetingPlaceName != null && meetingAddress != null &&
					!meetingPlaceName.equals("Meeting location")) {
				locationName.setText(String.format("%s (%s)", meetingPlaceName, meetingAddress));
			} else if (meetingAddress != null) {
				locationName.setText(meetingAddress);
			}
		} else {
			locationImage.setVisibility(View.GONE);
			locationName.setVisibility(View.GONE);
		}

		setAnswerButtons();
		setUpOngoingMeetingViews();

		if (meeting.getMapLocation() != null) {
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

		Button chooseLocationButton =
				(Button) meetingInfoLayout.findViewById(R.id.btn_choose_location);
		if (meeting.getLeaderId() == MainActivity.getAccountId() &&
				meeting.getStatus() == MeetingStatus.WAITING_LOCATION_CHOICE) {
			chooseLocationButton.setVisibility(View.VISIBLE);
			chooseLocationButton.setOnClickListener(view -> showChooseLocationWarningDialog());
		} else {
			chooseLocationButton.setVisibility(View.GONE);
		}
	}

	private void setButtonListeners() {
		FloatingActionButton showLocation =
				(FloatingActionButton) activity.findViewById(R.id.fab_location);
		showLocation.setOnClickListener(view -> startShowLocationActivity(null));
	}

	private void startShowLocationActivity(Bundle args) {
		if (args == null) {
			args = new Bundle();
		}
		Intent intent = new Intent(activity, ShowLocationActivity.class);
		args = GsonUtil.addJsonObjectToBundle(args, meeting, meeting.getClass().getSimpleName());
		intent.putExtras(args);
		startActivityForResult(intent, 1);
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

	private void showChooseLocationWarningDialog() {
		int participantsWhoAnswered = 0;
		for (Participant participant : meeting.getParticipants()) {
			if (participant.getSendGpsLocationAnswer() == SendGpsLocationAnswer.SEND) {
				participantsWhoAnswered++;
			}
		}
		if (participantsWhoAnswered == 0) {
			activity.showSnackbar(
					"At least 1 participant's position is needed to choose a location.");
			return;
		}

		String message = "The recommended locations depend on the positions of all participants. ";
		if (participantsWhoAnswered == meeting.getParticipants().size()) {
			message += "Continue?";
		} else {
			message += String.format(
					"Currently %s participants out of %s have sent their GPS position. Continue anyway?",
					participantsWhoAnswered, meeting.getParticipants().size());
		}

		SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.DialogTheme) {
			@Override
			public void onPositiveActionClicked(DialogFragment fragment) {
				super.onPositiveActionClicked(fragment);
				if (meeting.getLocationChoice() ==
						LocationChoice.RECOMMENDED_FROM_PREFERRED_LOCATIONS) {
					showRecommendedLocation();
				} else if (meeting.getLocationChoice() ==
						LocationChoice.RECOMMENDED_BY_PLACE_TYPE) {
					showChoosePlaceTypeDialog();
				}
			}

			@Override
			public void onNegativeActionClicked(DialogFragment fragment) {
				super.onNegativeActionClicked(fragment);
			}
		};

		builder.message(message).title("Choose meeting location?").positiveAction("OK")
				.negativeAction("Cancel");
		DialogFragment fragment = DialogFragment.newInstance(builder);
		fragment.show(activity.getSupportFragmentManager(), null);
	}

	private void showRecommendedLocation() {
		activity.showProgress(true);
		AsyncTask<Meeting, Void, String> recommendedLocationFinderTask =
				new AsyncTask<Meeting, Void, String>() {

					@Override
					protected String doInBackground(Meeting... meetings) {
						Meeting tempMeeting = meetings[0];
						LatLng recommendedLocation = LocationUtil.getCenterCoordinate(tempMeeting);
						if (tempMeeting.getLocationChoice() ==
								LocationChoice.RECOMMENDED_FROM_PREFERRED_LOCATIONS) {
							List<MapLocation> preferredLocationsSorted = LocationUtil
									.getUserPreferredSortedByRecommendation(tempMeeting,
											recommendedLocation);
							tempMeeting.setUserPreferredLocations(preferredLocationsSorted);
							Bundle args = new Bundle();
							args.putBoolean(ShowLocationActivity.CHOOSE_LOCATION_ARG, true);
							try {
								Thread.sleep(500);
								startShowLocationActivity(args);
								Thread.sleep(50);
							} catch (InterruptedException e) {
								Thread.interrupted();
							}
						}
						return null;
					}

					@Override
					protected void onPostExecute(String result) {
						activity.showProgress(false);
					}
				};
		recommendedLocationFinderTask.execute(meeting);
	}

	private void showChoosePlaceTypeDialog() {
		String[] placesTypes =
				new String[]{"bar", "cafe", "restaurant", "park", "movie_theater", "night_club"};
		String[] placesTypesTranslations =
				getResources().getStringArray(R.array.location_parameters_array);
		List<String> chosenPlacesTypes = new ArrayList<>();
		SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.DialogTheme) {
			@Override
			public void onPositiveActionClicked(DialogFragment fragment) {
				super.onPositiveActionClicked(fragment);
				int[] selectedIndexes = getSelectedIndexes();
				if (selectedIndexes.length > 0) {
					for (int i : selectedIndexes) {
						chosenPlacesTypes.add(placesTypes[i]);
					}
					showChoosePlace(chosenPlacesTypes);
				}
			}

			@Override
			public void onNegativeActionClicked(DialogFragment fragment) {
				super.onNegativeActionClicked(fragment);
			}
		};
		builder.multiChoiceItems(placesTypesTranslations).title("Choose suitable place types")
				.positiveAction("OK").negativeAction("Cancel");
		DialogFragment fragment = DialogFragment.newInstance(builder);
		fragment.show(activity.getSupportFragmentManager(), null);
	}

	private void showChoosePlace(List<String> chosenPlacesTypes) {
		activity.showProgress(true);
		AsyncTask<Meeting, Void, Boolean> recommendedLocationFinderTask =
				new AsyncTask<Meeting, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(Meeting... meetings) {
						Meeting tempMeeting = meetings[0];
						LatLng recommendedLocation = LocationUtil.getCenterCoordinate(tempMeeting);

						Set<Place> nearbyPlacesSet = new HashSet<>();
						for (String placeType : chosenPlacesTypes) {
							PlaceSearchResult placeSearchResult =
									RestClient.getGooglePlacesService()
											.getNearbyPlaces(recommendedLocation.latitude + "," +
													recommendedLocation.longitude, 5000, placeType);
							if (placeSearchResult.getResults() != null) {
								nearbyPlacesSet.addAll(placeSearchResult.getResults());
							}
						}
						if (nearbyPlacesSet.isEmpty()) {
							return false;
						}
						List<Place> nearbyPlacesList = new ArrayList<>();
						for (Place place : nearbyPlacesSet) {
							place.setDistanceFromRecommendedLocation(LocationUtil
									.getDistance(recommendedLocation,
											place.getMapLocation().getLatLng()));
							nearbyPlacesList.add(place);
						}
						Collections.sort(nearbyPlacesList,
								(place1, place2) -> place1.getDistanceFromRecommendedLocation()
										.compareTo(place2.getDistanceFromRecommendedLocation()));
						Bundle args = new Bundle();
						args.putBoolean(ShowLocationActivity.CHOOSE_LOCATION_ARG, true);
						Type placesListType = new TypeToken<ArrayList<Place>>() {}.getType();
						GsonUtil.addJsonCollectionToBundle(args, nearbyPlacesList, placesListType,
								"places");
						try {
							Thread.sleep(500);
							startShowLocationActivity(args);
							Thread.sleep(50);
						} catch (InterruptedException e) {
							Thread.interrupted();
						}
						return true;
					}

					@Override
					protected void onPostExecute(Boolean succeeded) {
						activity.showProgress(false);
						if (!succeeded) {
							activity.showSnackbar("Failed to find any suitable nearby places.");
						}
					}
				};
		recommendedLocationFinderTask.execute(meeting);
	}

	private void sendUpdateMeetingRequest() {
		RestClient.get().updateMeetingRequest(meeting, new Callback<ResponseBody>() {
			@Override
			public void success(ResponseBody responseBody, Response response) {

			}

			@Override
			public void failure(RetrofitError error) {
				activity.showSnackbar(getString(R.string.error_server_fail));
			}
		});
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
						activity.showSnackbar(getString(R.string.error_server_fail));
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
						activity.showSnackbar(getString(R.string.error_server_fail));
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
