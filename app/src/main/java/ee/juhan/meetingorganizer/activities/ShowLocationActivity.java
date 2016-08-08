package ee.juhan.meetingorganizer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.interfaces.SnackbarActivity;
import ee.juhan.meetingorganizer.models.MarkerData;
import ee.juhan.meetingorganizer.models.googleplaces.Place;
import ee.juhan.meetingorganizer.models.server.LocationChoice;
import ee.juhan.meetingorganizer.models.server.MapLocation;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.MeetingStatus;
import ee.juhan.meetingorganizer.services.MeetingUpdaterService;
import ee.juhan.meetingorganizer.util.GsonUtil;
import ee.juhan.meetingorganizer.util.UIUtil;

public class ShowLocationActivity extends AppCompatActivity implements SnackbarActivity {

	public static final String CHOOSE_LOCATION_ARG = "choose-location";
	public static final int MAX_PLACES_MARKERS = 10;
	private ViewGroup showLocationLayout;
	private CustomMapFragment customMapFragment;
	private Meeting currentMeeting;
	private boolean chooseLocation;
	private List<Place> nearbyPlacesList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapsInitializer.initialize(getApplicationContext());
		getIntentExtras();
		setContentView(R.layout.activity_show_location);
		showLocationLayout = (ViewGroup) findViewById(R.id.layout_show_location);
		setUpActionBar();
		setUpMapLayout();
		setUpButtonListeners();
		showConfirmFab(false);
	}

	@Override
	public final void onResume() {
		super.onResume();
		MeetingUpdaterService.startMeetingUpdaterTask();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}

	@Override
	public void showSnackbar(String message) {
		UIUtil.showSnackBar(showLocationLayout, message);
	}

	private void getIntentExtras() {
		currentMeeting = GsonUtil.getJsonObjectFromIntentExtras(getIntent(), Meeting.class,
				Meeting.class.getSimpleName());
		chooseLocation = getIntent().getBooleanExtra(CHOOSE_LOCATION_ARG, false);
		Type placesListType = new TypeToken<ArrayList<Place>>() {}.getType();
		nearbyPlacesList =
				GsonUtil.getJsonCollectionFromBundle(getIntent().getExtras(), placesListType,
						"places");
	}

	private void setUpActionBar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		assert actionBar != null;
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.layout_search_bar);
		actionBar.setDisplayShowTitleEnabled(false);
		EditText editSearch =
				(EditText) actionBar.getCustomView().findViewById(R.id.search_edittext);
		editSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				customMapFragment.searchMap(v.getText().toString());
				return true;
			}
			return false;
		});
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void setUpMapLayout() {
		customMapFragment = CustomMapFragment.newInstance();
		customMapFragment.setIsClickableMap(false);
		customMapFragment.addParticipantMarkers(currentMeeting.getParticipants(), this);
		getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment)
				.commit();

		if (currentMeeting.getMapLocation() != null) {
			customMapFragment.addMarker(new MarkerData(MarkerData.Type.CONFIRMED_LOCATION_MARKER,
					currentMeeting.getMapLocation(), this));
			customMapFragment.setCameraPosition(currentMeeting.getMapLocation().getLatLng());
		}

		if (chooseLocation) {
			if (currentMeeting.getLocationChoice() ==
					LocationChoice.RECOMMENDED_FROM_PREFERRED_LOCATIONS) {
				int i = 0;
				for (MapLocation location : currentMeeting.getUserPreferredLocations()) {
					i++;
					MarkerData markerData =
							new MarkerData(MarkerData.Type.PREFERRED_LOCATION_MARKER, location,
									this);
					markerData.getMarkerOptions().icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
					if (i == 1) {
						markerData.addSnippetLine("Recommended location");
						customMapFragment.setCameraPosition(location.getLatLng());
					} else {
						markerData.getMarkerOptions().alpha(0.5f);
					}
					customMapFragment.addMarker(markerData);
				}
			} else if (currentMeeting.getLocationChoice() ==
					LocationChoice.RECOMMENDED_BY_PLACE_TYPE && nearbyPlacesList != null) {
				int i = 0;
				for (Place place : nearbyPlacesList) {
					i++;
					MarkerData markerData = new MarkerData(place, this);
					MarkerOptions markerOptions = markerData.getMarkerOptions();
					markerOptions.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
					if (i == 1) {
						markerData.addSnippetLine("Recommended location");
						customMapFragment.setCameraPosition(place.getMapLocation().getLatLng());
					} else if (i == MAX_PLACES_MARKERS + 1) {
						break;
					} else {
						markerOptions.alpha(0.5f);
					}
					customMapFragment.addMarker(markerData);
				}
			}
		}

		customMapFragment.centerCameraToMarkers();
	}

	private void setUpButtonListeners() {
		FloatingActionButton confirmFab = (FloatingActionButton) findViewById(R.id.fab_confirm);
		confirmFab.setOnClickListener(view -> {
			Marker focusedMarker = customMapFragment.getFocusedMarker();
			MarkerData focusedMarkerData = customMapFragment.getMarkerData(focusedMarker);
			LatLng markerLocation = focusedMarker.getPosition();
			SimpleDialog.Builder builder = new SimpleDialog.Builder(R.style.DialogTheme) {
				@Override
				public void onPositiveActionClicked(DialogFragment fragment) {
					super.onPositiveActionClicked(fragment);
					MapLocation mapLocation =
							customMapFragment.getMarkerData(focusedMarker).getMapLocation();
					if (mapLocation.getPlaceName() == null) {
						mapLocation.setPlaceName("Meeting location");
					}
					currentMeeting.setMapLocation(mapLocation);
					currentMeeting.setStatus(MeetingStatus.ACTIVE);
					finishWithResult(currentMeeting);
				}

				@Override
				public void onNegativeActionClicked(DialogFragment fragment) {
					super.onNegativeActionClicked(fragment);
				}
			};

			String locationName = focusedMarkerData.getMapLocation().getPlaceName() != null ?
					focusedMarkerData.getMapLocation().getPlaceName() :
					focusedMarkerData.getMapLocation().getAddress();
			builder.message(String.format("You are about to choose '%s' as the meeting location.",
					locationName)).title("Confirm location?").positiveAction("OK")
					.negativeAction("Cancel");
			DialogFragment fragment = DialogFragment.newInstance(builder);
			fragment.show(getSupportFragmentManager(), null);
		});
	}

	private void finishWithResult(Meeting meeting) {
		Intent intent = GsonUtil.addJsonObjectToIntentExtras(new Intent(), meeting,
				meeting.getClass().getSimpleName());
		setResult(RESULT_OK, intent);
		finish();
	}

	public void showConfirmFab(boolean show) {
		FloatingActionButton confirmFab = (FloatingActionButton) findViewById(R.id.fab_confirm);
		if (show) {
			confirmFab.show();
		} else {
			confirmFab.hide();
		}
	}

	public Meeting getCurrentMeeting() {
		return currentMeeting;
	}
}
