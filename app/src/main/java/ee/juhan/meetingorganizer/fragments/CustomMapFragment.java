package ee.juhan.meetingorganizer.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.ChooseLocationActivity;
import ee.juhan.meetingorganizer.activities.ShowLocationActivity;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.SendGpsLocationAnswer;
import ee.juhan.meetingorganizer.util.UIUtil;

public class CustomMapFragment extends MapFragment
		implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapClickListener,
		OnMapReadyCallback {

	private static final String TAG = CustomMapFragment.class.getSimpleName();
	private static final LatLng DEFAULT_CAMERA_LOCATION = new LatLng(59.437046, 24.753742);
	private static final float DEFAULT_CAMERA_ZOOM = 10;
	private static int mapVisibility = View.VISIBLE;
	private static CustomMapFragment customMapFragment;
	private static Activity activity;
	private static GoogleMap map;
	private static boolean isMapInitialized;
	private static Map<Integer, Marker> participantMarkers = new HashMap<>();
	private Marker temporaryMarker;
	private Marker focusedMarker;
	private List<Marker> locationMarkers = new ArrayList<>();
	private int maxLocationMarkers = 1;
	private boolean isClickableMap;
	private ViewGroup mapLayout;
	private String temporaryMarkerAddress = "";
	private List<LatLng> markerLocationsToBeAdded = new ArrayList<>();
	private Map<Integer, MarkerOptions> participantMarkersToAdd = new HashMap<>();

	public static CustomMapFragment newInstance() {
		return new CustomMapFragment();
	}

	public static boolean isMapInitialized() {
		return customMapFragment != null && isMapInitialized;
	}

	public static boolean canShowParticipantMarkers() {
		return activity instanceof ShowLocationActivity;
	}

	public static void updateParticipantMarker(Participant participant) {
		if (customMapFragment == null || !customMapFragment.isVisible() || !isMapInitialized ||
				participant.getSendGpsLocationAnswer() != SendGpsLocationAnswer.SEND ||
				participant.getLocation() == null || !canShowParticipantMarkers()) {
			return;
		}
		Marker marker = participantMarkers.get(participant.getId());
		if (marker != null) {
			marker.setPosition(participant.getLocation().toLatLng());
			marker.setSnippet("Last updated: " + participant.getLocationTimestampFormatted());

			// Refresh info window if it's open.
			if (marker.isInfoWindowShown()) {
				marker.showInfoWindow();
			}
		} else {
			marker = map.addMarker(participant.getMarkerOptions());
			participantMarkers.put(participant.getId(), marker);
		}
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
		this.getMapAsync(this);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mapLayout = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		customMapFragment = this;
		checkAndroidVersion();
		return mapLayout;
	}

	@Override
	public final void onDestroyView() {
		customMapFragment = null;
		locationMarkers.clear();
		participantMarkers.clear();
		super.onDestroyView();
	}

	@Override
	public final boolean onMyLocationButtonClick() {
		LocationManager locationManager =
				(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			UIUtil.showToastMessage(activity, "GPS is disabled!");
		}
		return false;
	}

	@Override
	public final void onMapClick(LatLng latLng) {
		if (temporaryMarker != null) {
			temporaryMarker.remove();
		}
		setTemporaryMarker(latLng);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		CustomMapFragment.map = googleMap;
		initializeMap();
	}

	private void checkAndroidVersion() {
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			FrameLayout frameLayout = new FrameLayout(activity);
			frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			mapLayout.addView(frameLayout,
					new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.MATCH_PARENT));
			mapLayout.setVisibility(mapVisibility);
		}
	}

	private void initializeMap() {
		if (map == null) {
			return;
		}
		if (ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED) {
			return;
		}
		map.setMyLocationEnabled(true);
		map.setOnMyLocationButtonClickListener(this);
		if (isClickableMap) {
			map.setOnMapClickListener(this);
		}

		addLocationMarkers();
		addParticipantMarkers();

		if (temporaryMarker == null) {
			map.moveCamera(CameraUpdateFactory
					.newLatLngZoom(DEFAULT_CAMERA_LOCATION, DEFAULT_CAMERA_ZOOM));
		} else {
			map.moveCamera(CameraUpdateFactory
					.newLatLngZoom(temporaryMarker.getPosition(), DEFAULT_CAMERA_ZOOM));
		}

		map.setOnMarkerClickListener(marker -> {
			setFocus(marker, true);
			return false;
		});
		map.setPadding(0, 200, 0, 0);
		isMapInitialized = true;
	}

	private void addLocationMarkers() {
		for (LatLng latLng : markerLocationsToBeAdded) {
			setLocationMarker(latLng);
			if (activity instanceof ChooseLocationActivity) {
				((ChooseLocationActivity) activity).refreshConfirmMarkerFABState();
			}
		}
		markerLocationsToBeAdded = new ArrayList<>();
	}

	private void addParticipantMarkers() {
		for (Integer participantId : participantMarkersToAdd.keySet()) {
			Marker marker = map.addMarker(participantMarkersToAdd.get(participantId));
			participantMarkers.put(participantId, marker);
		}
		participantMarkersToAdd.clear();
	}

	private void setFocus(Marker marker, boolean focused) {
		if (focused) {
			focusedMarker = marker;
			if (activity instanceof ChooseLocationActivity) {
				((ChooseLocationActivity) activity).showDeleteMarkerFAB(true);
			}
		} else {
			if (activity instanceof ChooseLocationActivity) {
				((ChooseLocationActivity) activity).showDeleteMarkerFAB(false);
			}
		}
	}

	private void setLocationMarker(LatLng latLng) {
		setTemporaryMarker(latLng);
		confirmTemporaryMarker();
		if (activity instanceof ChooseLocationActivity) {
			((ChooseLocationActivity) activity).refreshConfirmMarkerFABState();
		}
	}

	public void setMarkerLocations(List<LatLng> locations) {
		markerLocationsToBeAdded = locations;
	}

	public void removeLocationMarker(Marker marker) {
		marker.remove();
		locationMarkers.remove(marker);
	}

	private void setTemporaryMarker(LatLng latLng, String address) {
		this.temporaryMarkerAddress = address;
		if (temporaryMarker != null) {
			temporaryMarker.remove();
		}
		setFocus(null, false);
		temporaryMarker =
				map.addMarker(new MarkerOptions().position(latLng).title(address).draggable(false));
		temporaryMarker.setAlpha(0.5f);
		temporaryMarker.showInfoWindow();
		if (activity instanceof ChooseLocationActivity) {
			((ChooseLocationActivity) activity).refreshConfirmMarkerFABState();
		}
	}

	public Marker confirmTemporaryMarker() {
		temporaryMarker.setAlpha(1);
		locationMarkers.add(temporaryMarker);
		temporaryMarker = null;
		return locationMarkers.get(locationMarkers.size() - 1);
	}

	public Marker getFocusedMarker() {
		return focusedMarker;
	}

	public boolean isLocationMarkerAvailable() {
		return locationMarkers.size() < maxLocationMarkers;
	}

	public void clearLocationMarkers() {
		locationMarkers.clear();
	}

	public void setMaxLocationMarkers(int max) {
		maxLocationMarkers = max;
	}

	public final Marker getTemporaryMarker() {
		return temporaryMarker;
	}

	private void setTemporaryMarker(LatLng latLng) {
		try {
			Geocoder geocoder = new Geocoder(activity);
			List<Address> addresses =
					geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
			if (addresses.size() > 0) {
				setTemporaryMarker(latLng, addresses.get(0).getAddressLine(0));
			} else {
				setTemporaryMarker(latLng, "Unknown address");
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not get location from address string.");
			setTemporaryMarker(latLng, "");
		}
	}

	public final void setIsClickableMap(boolean isClickableMap) {
		this.isClickableMap = isClickableMap;
	}

	public final void setMapVisibility(int visibility) {
		CustomMapFragment.mapVisibility = visibility;
		if (mapLayout != null) {
			mapLayout.setVisibility(visibility);
		}
	}

	public final String getTemporaryMarkerAddress() {
		return temporaryMarkerAddress;
	}

	public final void clearMap() {
		if (map != null) {
			map.clear();
			temporaryMarker = null;
			temporaryMarkerAddress = "";
		}
	}

	public final void searchMap(String locationName) {
		try {
			Geocoder geocoder = new Geocoder(activity.getBaseContext());
			List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
			if (addresses.size() > 0) {
				LatLng latLng =
						new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_CAMERA_ZOOM));
				setTemporaryMarker(latLng, addresses.get(0).getAddressLine(0));
			} else {
				UIUtil.showToastMessage(activity, getString(R.string.location_not_found));
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not get location from address string.");
			UIUtil.showToastMessage(activity, getString(R.string.location_geocoder_service_error));
		}
	}

	public void addParticipantMarkerOptions(int participantId, MarkerOptions markerOptions) {
		participantMarkersToAdd.put(participantId, markerOptions);
	}
}
