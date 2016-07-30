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
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.rey.material.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.adapters.CheckBoxAdapter;
import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.MapCoordinate;

import static ee.juhan.meetingorganizer.models.server.LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS;
import static ee.juhan.meetingorganizer.models.server.LocationType.SPECIFIC_LOCATION;

public class LocationActivity extends AppCompatActivity {

	public static final String SHOW_LOCATION_OPTIONS = "location-options";
	public static final String MARKER_LATITUDE = "marker-latitude";
	public static final String MARKER_LONGITUDE = "marker-longitude";
	private ViewGroup chooseLocationLayout;
	private List<String> filtersList;
	private CustomMapFragment customMapFragment = new CustomMapFragment();
	private TextView locationTypeInfo;
	private FloatingActionButton confirmFAB;
	private FloatingActionButton confirmMarkerFAB;
	private FloatingActionButton deleteMarkerFAB;
	private View bottomSheet;
	private boolean showLocationOptions;
	private LatLng markerLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getIntentExtras();
		setContentView(R.layout.activity_location);
		chooseLocationLayout = (ViewGroup) findViewById(R.id.activity_location);
		locationTypeInfo = (TextView) findViewById(R.id.location_type_info);
		confirmFAB = (FloatingActionButton) findViewById(R.id.fab_confirm);
		confirmMarkerFAB = (FloatingActionButton) findViewById(R.id.fab_confirm_marker);
		deleteMarkerFAB = (FloatingActionButton) findViewById(R.id.fab_delete_marker);
		bottomSheet = findViewById(R.id.bottom_sheet);
		filtersList =
				Arrays.asList(getResources().getStringArray(R.array.location_parameters_array));
		setUpActionBar();
		setUpMapLayout();

		if (showLocationOptions) {
			setButtonListeners();
			setLocationSpinner();
		} else {
			bottomSheet.setVisibility(View.GONE);
			confirmFAB.hide();
			confirmMarkerFAB.hide();
			deleteMarkerFAB.hide();
		}
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

	private void getIntentExtras() {
		showLocationOptions = getIntent().getBooleanExtra(SHOW_LOCATION_OPTIONS, true);
		double markerLatitude = getIntent().getDoubleExtra(MARKER_LATITUDE, 0);
		double markerLongitude = getIntent().getDoubleExtra(MARKER_LONGITUDE, 0);
		if (markerLatitude != 0 && markerLongitude != 0) {
			markerLocation = new LatLng(markerLatitude, markerLongitude);
		}

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
		switch (NewMeetingActivity.getNewMeetingModel().getLocationType()) {
			case SPECIFIC_LOCATION:
				spinner.setSelection(0);
				break;
			case GENERATED_FROM_PREDEFINED_LOCATIONS:
				spinner.setSelection(1);
				break;
			case GENERATED_FROM_PARAMETERS:
				spinner.setSelection(2);
				break;
			default:
				spinner.setSelection(0);
		}
		refreshConfirmMarkerFABState();
	}

	private void setButtonListeners() {
		if (confirmFAB != null) {
			confirmFAB.setOnClickListener(view -> finish());
		}

		if (confirmMarkerFAB != null) {
			confirmMarkerFAB.setOnClickListener(view -> {
				Marker locationMarker = customMapFragment.confirmTemporaryMarker();
				refreshConfirmMarkerFABState();

				if (NewMeetingActivity.getNewMeetingModel().getLocationType() ==
						LocationType.SPECIFIC_LOCATION) {
					NewMeetingActivity.getNewMeetingModel().setLocation(
							new MapCoordinate(locationMarker.getPosition().latitude,
									locationMarker.getPosition().longitude));
				} else if (NewMeetingActivity.getNewMeetingModel().getLocationType() ==
						LocationType.GENERATED_FROM_PREDEFINED_LOCATIONS) {
					NewMeetingActivity.getNewMeetingModel().addPredefinedLocation(
							new MapCoordinate(locationMarker.getPosition().latitude,
									locationMarker.getPosition().longitude));
				}
			});
		}

		if (deleteMarkerFAB != null) {
			deleteMarkerFAB.setOnClickListener(view -> {
				Marker focusedMarker = customMapFragment.getFocusedMarker();
				if (focusedMarker != null) {
					customMapFragment.removeLocationMarker(focusedMarker);
					MapCoordinate newMeetingLocation =
							NewMeetingActivity.getNewMeetingModel().getLocation();
					Set<MapCoordinate> newMeetingPredefinedLocations =
							NewMeetingActivity.getNewMeetingModel().getPredefinedLocations();
					MapCoordinate focusedMarkerMapCoordinate =
							new MapCoordinate(focusedMarker.getPosition());
					if (newMeetingLocation != null && newMeetingLocation
							.equals(new MapCoordinate(focusedMarker.getPosition()))) {
						NewMeetingActivity.getNewMeetingModel().setLocation(null);
					} else if (newMeetingPredefinedLocations.contains(focusedMarkerMapCoordinate)) {
						newMeetingPredefinedLocations.remove(focusedMarkerMapCoordinate);
					}
				}
				showDeleteMarkerFAB(false);
			});
		}
		showDeleteMarkerFAB(false);

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

			}
		});
	}

	public void showDeleteMarkerFAB(boolean show) {
		if (show && showLocationOptions) {
			confirmMarkerFAB.hide();
			deleteMarkerFAB.show();
		} else {
			deleteMarkerFAB.hide();
		}
	}

	public void refreshConfirmMarkerFABState() {
		if (canConfirmMarker()) {
			deleteMarkerFAB.hide();
			confirmMarkerFAB.show();
		} else {
			confirmMarkerFAB.hide();
		}
	}

	public boolean canConfirmMarker() {
		return customMapFragment.isMapInitialized() &&
				customMapFragment.getTemporaryMarker() != null &&
				customMapFragment.isLocationMarkerAvailable() &&
				NewMeetingActivity.getNewMeetingModel().getLocationType() !=
						LocationType.GENERATED_FROM_PARAMETERS;
	}

	private void setUpMapLayout() {
		customMapFragment = CustomMapFragment.newInstance();
		customMapFragment.setIsClickableMap(true);
		getFragmentManager().beginTransaction().replace(R.id.location_frame, customMapFragment)
				.commit();
		FrameLayout layout = (FrameLayout) chooseLocationLayout.findViewById(R.id.location_frame);
		layout.requestFocus();

		LocationType locationType = NewMeetingActivity.getNewMeetingModel().getLocationType();
		MapCoordinate newMeetingLocation = NewMeetingActivity.getNewMeetingModel().getLocation();
		Set<MapCoordinate> newMeetingPredefinedLocations =
				NewMeetingActivity.getNewMeetingModel().getPredefinedLocations();
		if (locationType == SPECIFIC_LOCATION && newMeetingLocation != null) {
			customMapFragment
					.setMarkerLocations(Collections.singletonList(newMeetingLocation.toLatLng()));
		} else if (locationType == GENERATED_FROM_PREDEFINED_LOCATIONS &&
				newMeetingPredefinedLocations.size() > 0) {
			List<LatLng> locationsList = new ArrayList<>();
			for (MapCoordinate location : newMeetingPredefinedLocations) {
				locationsList.add(location.toLatLng());
			}
			customMapFragment.setMarkerLocations(locationsList);
		}

		if (markerLocation != null) {
			customMapFragment.setMarkerLocations(Collections.singletonList(markerLocation));
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
			switch (pos) {
				case 0:
					NewMeetingActivity.getNewMeetingModel().setLocationType(SPECIFIC_LOCATION);
					locationTypeInfo.setText(
							getResources().getString(R.string.location_specific_location_info));
					setUpMapLayout();
					customMapFragment.setMaxLocationMarkers(1);
					break;
				case 1:
					NewMeetingActivity.getNewMeetingModel()
							.setLocationType(GENERATED_FROM_PREDEFINED_LOCATIONS);
					locationTypeInfo.setText(getResources()
							.getString(R.string.location_generated_from_locations_info));
					setUpMapLayout();
					customMapFragment.setMaxLocationMarkers(5);
					break;
				//				case 2:
				//					NewMeetingActivity.getNewMeetingModel()
				//							.setLocationType(GENERATED_FROM_PARAMETERS);
				//					locationTypeInfo.setText(getResources()
				//							.getString(R.string.info_location_generated_from_parameters));
				//					//						removeLocationViews();
				//					ListView listView = new ListView(getBaseContext());
				//					listView.setAdapter(
				//							new LocationParametersAdapter(getBaseContext(), filtersList));
				//					addLocationChild(listView);
				//					break;
				default:
					break;
			}
			refreshConfirmMarkerFABState();
		}

	}
}
