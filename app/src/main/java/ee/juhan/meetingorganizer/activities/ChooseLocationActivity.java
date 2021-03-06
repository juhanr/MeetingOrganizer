package ee.juhan.meetingorganizer.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.rey.material.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.CheckBoxAdapter;
import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.interfaces.SnackbarActivity;
import ee.juhan.meetingorganizer.models.MarkerData;
import ee.juhan.meetingorganizer.models.server.LocationChoice;
import ee.juhan.meetingorganizer.models.server.MapLocation;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.util.UIUtil;

import static ee.juhan.meetingorganizer.models.server.LocationChoice.NOT_SET;
import static ee.juhan.meetingorganizer.models.server.LocationChoice.RECOMMENDED_BY_PLACE_TYPE;
import static ee.juhan.meetingorganizer.models.server.LocationChoice.RECOMMENDED_FROM_PREFERRED_LOCATIONS;
import static ee.juhan.meetingorganizer.models.server.LocationChoice.SPECIFIC_LOCATION;

public class ChooseLocationActivity extends AppCompatActivity implements SnackbarActivity {

	private ViewGroup chooseLocationLayout;
	private List<String> filtersList;
	private CustomMapFragment customMapFragment = new CustomMapFragment();
	private TextView locationTypeInfo;
	private FloatingActionButton confirmFab;
	private FloatingActionButton confirmMarkerFab;
	private FloatingActionButton deleteMarkerFab;
	private View bottomSheet;
	private boolean isMapFragmentShown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapsInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_choose_location);
		chooseLocationLayout = (ViewGroup) findViewById(R.id.layout_choose_location);
		locationTypeInfo = (TextView) findViewById(R.id.location_type_info);
		confirmFab = (FloatingActionButton) findViewById(R.id.fab_confirm);
		confirmMarkerFab = (FloatingActionButton) findViewById(R.id.fab_confirm_marker);
		deleteMarkerFab = (FloatingActionButton) findViewById(R.id.fab_delete_marker);
		bottomSheet = findViewById(R.id.bottom_sheet);
		filtersList =
				Arrays.asList(getResources().getStringArray(R.array.location_parameters_array));
		if (NewMeetingActivity.getNewMeetingModel().getLocationChoice() == NOT_SET) {
			NewMeetingActivity.getNewMeetingModel()
					.setLocationChoice(LocationChoice.SPECIFIC_LOCATION);
		}
		setUpActionBar();
		setUpMapLayout();
		setButtonListeners();
		setLocationSpinner();
	}

	@Override
	protected void onPause() {
		// Check if we should change the meeting type to NOT_SET.
		Meeting newMeetingModel = NewMeetingActivity.getNewMeetingModel();
		if (newMeetingModel.getLocationChoice() == LocationChoice.SPECIFIC_LOCATION &&
				newMeetingModel.getMapLocation() == null || newMeetingModel.getLocationChoice() ==
				LocationChoice.RECOMMENDED_FROM_PREFERRED_LOCATIONS &&
				newMeetingModel.getUserPreferredLocations().isEmpty()) {
			NewMeetingActivity.getNewMeetingModel().setLocationChoice(LocationChoice.NOT_SET);
		}
		super.onPause();
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
		UIUtil.showSnackBar(chooseLocationLayout, message);
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

	private void setLocationSpinner() {
		Spinner spinner = (Spinner) chooseLocationLayout.findViewById(R.id.location_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.location_options_array, R.layout.row_spn);
		adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerListener());

		// Get the text view of the spinner and set width to MATCH_PARENT.
		TextView textView = (TextView) spinner.getChildAt(1);
		textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));

		// Set the spinner selection.
		switch (NewMeetingActivity.getNewMeetingModel().getLocationChoice()) {
			case SPECIFIC_LOCATION:
				spinner.setSelection(0);
				break;
			case RECOMMENDED_FROM_PREFERRED_LOCATIONS:
				spinner.setSelection(1);
				break;
			case RECOMMENDED_BY_PLACE_TYPE:
				spinner.setSelection(2);
				break;
			default:
				spinner.setSelection(0);
		}
		refreshConfirmMarkerFabState();
	}

	private void setButtonListeners() {
		if (confirmFab != null) {
			confirmFab.setOnClickListener(view -> finish());
		}

		if (confirmMarkerFab != null) {
			confirmMarkerFab.setOnClickListener(view -> {
				Marker locationMarker = customMapFragment.confirmTemporaryMarker();
				MarkerData locationMarkerData = customMapFragment.getMarkerData(locationMarker);
				refreshConfirmMarkerFabState();

				if (NewMeetingActivity.getNewMeetingModel().getLocationChoice() ==
						LocationChoice.SPECIFIC_LOCATION) {
					MapLocation mapLocation = locationMarkerData.getMapLocation();
					mapLocation.setPlaceName("Meeting location");
					NewMeetingActivity.getNewMeetingModel().setMapLocation(mapLocation);
				} else if (NewMeetingActivity.getNewMeetingModel().getLocationChoice() ==
						LocationChoice.RECOMMENDED_FROM_PREFERRED_LOCATIONS) {
					NewMeetingActivity.getNewMeetingModel().addUserPreferredLocation(
							locationMarkerData.getMapLocation());
				}
			});
		}

		if (deleteMarkerFab != null) {
			deleteMarkerFab.setOnClickListener(view -> {
				Marker focusedMarker = customMapFragment.getFocusedMarker();
				if (focusedMarker != null) {
					customMapFragment.removeMarker(focusedMarker);
					MapLocation newMeetingLocation =
							NewMeetingActivity.getNewMeetingModel().getMapLocation();
					List<MapLocation> newMeetingPredefinedLocations =
							NewMeetingActivity.getNewMeetingModel().getUserPreferredLocations();
					MapLocation focusedMarkerMapLocation =
							new MapLocation(focusedMarker.getPosition());
					if (newMeetingLocation != null && newMeetingLocation
							.equals(new MapLocation(focusedMarker.getPosition()))) {
						NewMeetingActivity.getNewMeetingModel().setMapLocation(null);
					} else if (newMeetingPredefinedLocations.contains(focusedMarkerMapLocation)) {
						newMeetingPredefinedLocations.remove(focusedMarkerMapLocation);
					}
				}
				showDeleteMarkerFab(false);
			});
		}
		showDeleteMarkerFab(false);

		final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		bottomSheetBehavior.setPeekHeight(450);
		bottomSheet.post(() -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {

			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				TextView slideUpInfo = (TextView) findViewById(R.id.slide_up_info);
				if (slideUpInfo != null) {
					slideUpInfo.setAlpha(1 - slideOffset);
				}
				TextView extraInfo = (TextView) findViewById(R.id.extra_info);
				if (extraInfo != null) {
					extraInfo.setAlpha(0 + slideOffset);
				}

			}
		});
	}

	public void showDeleteMarkerFab(boolean show) {
		if (show) {
			confirmMarkerFab.hide();
			deleteMarkerFab.show();
		} else {
			deleteMarkerFab.hide();
		}
	}

	public void refreshConfirmMarkerFabState() {
		if (canConfirmMarker()) {
			deleteMarkerFab.hide();
			confirmMarkerFab.show();
		} else {
			confirmMarkerFab.hide();
		}
	}

	public boolean canConfirmMarker() {
		return CustomMapFragment.isMapInitialized() &&
				customMapFragment.getTemporaryMarker() != null &&
				customMapFragment.isLocationMarkerAvailable() &&
				NewMeetingActivity.getNewMeetingModel().getLocationChoice() !=
						LocationChoice.RECOMMENDED_BY_PLACE_TYPE;
	}

	private void setUpMapLayout() {
		customMapFragment = CustomMapFragment.newInstance();
		customMapFragment.setIsClickableMap(true);
		getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment)
				.commit();
		FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
		layout.requestFocus();
		isMapFragmentShown = true;

		Meeting newMeeting = NewMeetingActivity.getNewMeetingModel();
		List<MapLocation> userPreferredLocations = newMeeting.getUserPreferredLocations();
		if (newMeeting.getLocationChoice() == SPECIFIC_LOCATION &&
				newMeeting.getMapLocation() != null) {
			customMapFragment.addMarker(new MarkerData(MarkerData.Type.CONFIRMED_LOCATION_MARKER,
					newMeeting.getMapLocation(), this));
		} else if (newMeeting.getLocationChoice() == RECOMMENDED_FROM_PREFERRED_LOCATIONS &&
				userPreferredLocations.size() > 0) {
			for (MapLocation location : userPreferredLocations) {
				customMapFragment.addMarker(
						new MarkerData(MarkerData.Type.PREFERRED_LOCATION_MARKER, location, this));
			}
		}
	}

	private void showListView(boolean show) {
		ListView listView = (ListView) findViewById(R.id.list_view_location_parameters);
		FrameLayout fragmentFrame =
				(FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
		if (show) {
			listView.setVisibility(View.VISIBLE);
			fragmentFrame.setVisibility(View.GONE);
			listView.setItemsCanFocus(true);
			listView.setAdapter(new LocationParametersAdapter(getBaseContext(), filtersList));
		} else {
			listView.setVisibility(View.GONE);
			fragmentFrame.setVisibility(View.VISIBLE);
		}
	}

	private class LocationParametersAdapter extends CheckBoxAdapter<String> {

		public LocationParametersAdapter(Context context, List<String> objects) {
			super(context, objects);
		}

		@Override
		protected void setUpCheckBox() {
			super.setCheckBoxText(getCurrentItem());
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			String item = buttonView.getText().toString();
			if (isChecked) {
				super.getCheckedItems().add(item);
			} else {
				super.getCheckedItems().remove(item);
			}
		}
	}

	private class SpinnerListener implements Spinner.OnItemSelectedListener {

		@Override
		public void onItemSelected(Spinner parent, View view, int pos, long id) {
			customMapFragment.setTemporaryMarkerIcon(
					BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			switch (pos) {
				case 0:
					showListView(false);
					NewMeetingActivity.getNewMeetingModel().setLocationChoice(SPECIFIC_LOCATION);
					NewMeetingActivity.getNewMeetingModel().getUserPreferredLocations().clear();
					locationTypeInfo.setText(
							getResources().getString(R.string.location_specific_location_info));
					locationTypeInfo.setTextColor(getResources().getColor(R.color.text_hint));
					setUpMapLayout();
					customMapFragment.setMaxLocationMarkers(1);
					break;
				case 1:
					if (NewMeetingActivity.getNewMeetingModel().isQuickMeeting()) {
						showListView(false);
						NewMeetingActivity.getNewMeetingModel()
								.setLocationChoice(RECOMMENDED_FROM_PREFERRED_LOCATIONS);
						NewMeetingActivity.getNewMeetingModel().setMapLocation(null);
						locationTypeInfo.setText(getResources()
								.getString(R.string.location_recommended_from_locations_info));
						locationTypeInfo.setTextColor(getResources().getColor(R.color.text_hint));
						setUpMapLayout();
						customMapFragment.setMaxLocationMarkers(5);
						customMapFragment.setTemporaryMarkerIcon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
					} else {
						locationTypeInfo
								.setText("This option is available only with a Quick Meeting.");
						locationTypeInfo.setTextColor(getResources().getColor(R.color.dark_red));
					}
					break;
				case 2:
					if (NewMeetingActivity.getNewMeetingModel().isQuickMeeting()) {
						showListView(false);
						NewMeetingActivity.getNewMeetingModel()
								.setLocationChoice(RECOMMENDED_BY_PLACE_TYPE);
						NewMeetingActivity.getNewMeetingModel().setMapLocation(null);
						locationTypeInfo.setText(getResources()
								.getString(R.string.location_recommended_by_place_type_info));
						locationTypeInfo.setTextColor(getResources().getColor(R.color.text_hint));
					} else {
						locationTypeInfo
								.setText("This option is available only with a Quick Meeting.");
						locationTypeInfo.setTextColor(getResources().getColor(R.color.dark_red));
					}
					break;
				default:
					break;
			}
			refreshConfirmMarkerFabState();
		}

	}
}
