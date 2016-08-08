package ee.juhan.meetingorganizer.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import ee.juhan.meetingorganizer.interfaces.SnackbarActivity;
import ee.juhan.meetingorganizer.models.MarkerData;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.SendGpsLocationAnswer;
import ee.juhan.meetingorganizer.util.GsonUtil;
import ee.juhan.meetingorganizer.util.LocationUtil;

public class CustomMapFragment extends MapFragment
		implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapClickListener,
		OnMapReadyCallback {

	public static final String UPDATE_PARTICIPANT_MARKERS_ACTION = "update-participant-markers";
	private static final String TAG = CustomMapFragment.class.getSimpleName();
	private static final LatLng DEFAULT_CAMERA_POSITION = new LatLng(59.437046, 24.753742);
	private static final float DEFAULT_CAMERA_ZOOM = 10;
	private static int mapVisibility = View.VISIBLE;
	private static CustomMapFragment customMapFragment;
	private static Activity activity;
	private static GoogleMap map;
	private static boolean isMapInitialized;
	private Marker temporaryMarker;
	private Marker focusedMarker;
	private int maxLocationMarkers = 1;
	private boolean isClickableMap;
	private ViewGroup mapLayout;
	private ParticipantMarkerUpdateReceiver participantMarkerUpdateReceiver;
	private LatLng newCameraPosition;
	private BitmapDescriptor temporaryMarkerIcon;

	private List<MarkerData> markersToAdd = new ArrayList<>();
	private Map<Marker, MarkerData> addedMarkers = new HashMap<>();

	public static CustomMapFragment newInstance() {
		return new CustomMapFragment();
	}

	public static boolean isMapInitialized() {
		return customMapFragment != null && isMapInitialized;
	}

	public static CustomMapFragment getFragment() {
		return customMapFragment;
	}

	public static boolean canShowParticipantMarkers() {
		return activity instanceof ShowLocationActivity;
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
		checkAndroidVersion();
		return mapLayout;
	}

	@Override
	public final void onResume() {
		super.onResume();
		customMapFragment = this;
		participantMarkerUpdateReceiver = new ParticipantMarkerUpdateReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CustomMapFragment.UPDATE_PARTICIPANT_MARKERS_ACTION);
		getActivity().registerReceiver(participantMarkerUpdateReceiver, intentFilter);
	}

	@Override
	public final void onPause() {
		customMapFragment = null;
		getActivity().unregisterReceiver(participantMarkerUpdateReceiver);
		super.onPause();
	}

	@Override
	public final void onDestroyView() {
		addedMarkers.clear();
		super.onDestroyView();
	}

	@Override
	public final boolean onMyLocationButtonClick() {
		LocationManager locationManager =
				(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			((SnackbarActivity) activity).showSnackbar("GPS is disabled!");
		}
		return false;
	}

	@Override
	public final void onMapClick(LatLng latLng) {
		if (!isClickableMap) {
			setFocus(null, false);
			return;
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
		if (map == null || ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED) {
			return;
		}
		isMapInitialized = true;
		map.setMyLocationEnabled(true);
		map.setOnMyLocationButtonClickListener(this);
		map.setOnMapClickListener(this);

		addMarkersToMap();
		if (newCameraPosition != null) {
			map.moveCamera(
					CameraUpdateFactory.newLatLngZoom(newCameraPosition, DEFAULT_CAMERA_ZOOM));
		} else if (temporaryMarker != null) {
			map.moveCamera(CameraUpdateFactory
					.newLatLngZoom(temporaryMarker.getPosition(), DEFAULT_CAMERA_ZOOM));
		} else {
			map.moveCamera(CameraUpdateFactory
					.newLatLngZoom(DEFAULT_CAMERA_POSITION, DEFAULT_CAMERA_ZOOM));
		}
		centerCameraToMarkers();

		map.setOnMarkerClickListener(marker -> {
			setFocus(marker, true);
			return false;
		});
		map.setPadding(0, 200, 0, 0);
		map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

			@Override
			public View getInfoWindow(Marker arg0) {
				return null;
			}

			@Override
			public View getInfoContents(Marker marker) {

				LinearLayout info = new LinearLayout(activity);
				info.setOrientation(LinearLayout.VERTICAL);

				TextView title = new TextView(activity);
				title.setTextColor(Color.BLACK);
				title.setGravity(Gravity.CENTER);
				title.setTypeface(null, Typeface.BOLD);
				title.setText(marker.getTitle());

				TextView snippet = new TextView(activity);
				snippet.setTextColor(Color.GRAY);
				snippet.setText(marker.getSnippet());

				if (marker.getTitle() != null && !marker.getTitle().equals("")) {
					info.addView(title);
				}
				if (marker.getSnippet() != null && !marker.getSnippet().equals("")) {
					info.addView(snippet);
				}

				return info;
			}
		});

		if (temporaryMarkerIcon == null) {
			temporaryMarkerIcon =
					BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
		}
	}

	public void setCameraPosition(LatLng latLng) {
		newCameraPosition = latLng;
		if (isMapInitialized()) {
			map.moveCamera(
					CameraUpdateFactory.newLatLngZoom(newCameraPosition, DEFAULT_CAMERA_ZOOM));
		}
	}

	private void addMarkersToMap() {
		for (MarkerData markerData : markersToAdd) {
			Marker marker = map.addMarker(markerData.getMarkerOptions());
			addedMarkers.put(marker, markerData);
		}
		if (activity instanceof ChooseLocationActivity) {
			((ChooseLocationActivity) activity).refreshConfirmMarkerFabState();
		}
		markersToAdd.clear();

	}

	private void setFocus(Marker marker, boolean focused) {
		if (focused) {
			focusedMarker = marker;
			if (activity instanceof ChooseLocationActivity &&
					addedMarkers.get(focusedMarker) != null &&
					addedMarkers.get(focusedMarker).getType() != MarkerData.Type.TEMPORARY_MARKER) {
				((ChooseLocationActivity) activity).showDeleteMarkerFab(true);
			} else if (addedMarkers.get(focusedMarker).getType() ==
					MarkerData.Type.TEMPORARY_MARKER) {
				((ChooseLocationActivity) activity).showDeleteMarkerFab(false);
			} else if (activity instanceof ShowLocationActivity) {
				MarkerData.Type focusedMarkerTpe = addedMarkers.get(focusedMarker).getType();
				if (focusedMarkerTpe == MarkerData.Type.PREFERRED_LOCATION_MARKER ||
						focusedMarkerTpe == MarkerData.Type.RECOMMENDED_PLACE_MARKER) {
					((ShowLocationActivity) activity).showConfirmFab(true);
				} else {
					((ShowLocationActivity) activity).showConfirmFab(false);
				}
			}
		} else {
			if (activity instanceof ChooseLocationActivity) {
				((ChooseLocationActivity) activity).showDeleteMarkerFab(false);
			} else if (activity instanceof ShowLocationActivity) {
				((ShowLocationActivity) activity).showConfirmFab(false);
			}
		}
	}

	public void addMarker(MarkerData markerData) {
		if (isMapInitialized()) {
			Marker marker = map.addMarker(markerData.getMarkerOptions());
			addedMarkers.put(marker, markerData);
		} else {
			markersToAdd.add(markerData);
		}
	}

	public void removeMarker(Marker marker) {
		marker.remove();
		addedMarkers.remove(marker);
	}

	public MarkerData getMarkerData(Marker marker) {
		return addedMarkers.get(marker);
	}

	public Marker confirmTemporaryMarker() {
		temporaryMarker.setAlpha(1);
		MarkerData markerData = addedMarkers.get(temporaryMarker);
		markerData.setType(MarkerData.Type.CONFIRMED_LOCATION_MARKER);
		Marker returnableMarker = temporaryMarker;
		temporaryMarker = null;
		return returnableMarker;
	}

	public Marker getFocusedMarker() {
		return focusedMarker;
	}

	public boolean isLocationMarkerAvailable() {
		int locationMarkersCount = 0;
		for (Marker marker : addedMarkers.keySet()) {
			MarkerData markerData = addedMarkers.get(marker);
			if (markerData.getType() == MarkerData.Type.CONFIRMED_LOCATION_MARKER ||
					markerData.getType() == MarkerData.Type.PREFERRED_LOCATION_MARKER) {
				locationMarkersCount++;
			}
		}
		return locationMarkersCount < maxLocationMarkers;
	}

	public void setMaxLocationMarkers(int max) {
		maxLocationMarkers = max;
	}

	public final Marker getTemporaryMarker() {
		return temporaryMarker;
	}

	private void setTemporaryMarker(LatLng latLng) {
		MarkerData markerData;
		if (temporaryMarker == null) {
			markerData = new MarkerData(
					new MarkerOptions().draggable(false).alpha(0.5f).position(latLng)
							.icon(temporaryMarkerIcon), MarkerData.Type.TEMPORARY_MARKER, activity);
			temporaryMarker = map.addMarker(markerData.getMarkerOptions());
			addedMarkers.put(temporaryMarker, markerData);
		} else {
			markerData = addedMarkers.get(temporaryMarker);
			markerData.getMapLocation().setLatLng(latLng);
			markerData.getMapLocation()
					.setAddress(LocationUtil.getAddressFromLatLng(latLng, activity));
		}
		temporaryMarker.setPosition(latLng);
		temporaryMarker.setTitle(markerData.getMapLocation().getPlaceName());
		temporaryMarker.setSnippet(markerData.getMapLocation().getAddressFormatted());
		temporaryMarker.showInfoWindow();
		temporaryMarker.setIcon(temporaryMarkerIcon);
		setFocus(temporaryMarker, true);
		if (activity instanceof ChooseLocationActivity) {
			((ChooseLocationActivity) activity).refreshConfirmMarkerFabState();
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

	public final void clearMap() {
		if (map != null) {
			map.clear();
			temporaryMarker = null;
			addedMarkers.clear();
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
				setTemporaryMarker(latLng);
			} else {
				((SnackbarActivity) activity).showSnackbar(getString(R.string.location_not_found));
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not get location from address string.");
			((SnackbarActivity) activity)
					.showSnackbar(getString(R.string.location_geocoder_service_error));
		}
	}

	public void addParticipantMarkers(List<Participant> participants, Context context) {
		for (Participant participant : participants) {
			if (participant.getSendGpsLocationAnswer() != SendGpsLocationAnswer.SEND ||
					participant.getMapLocation() == null) {
				continue;
			}
			markersToAdd.add(new MarkerData(participant.getMarkerOptions(),
					MarkerData.Type.PARTICIPANT_MARKER, participant.getId(), context));
		}
	}

	public void updateParticipantMarker(Participant participant) {
		if (customMapFragment == null || !customMapFragment.isVisible() || !isMapInitialized ||
				participant.getSendGpsLocationAnswer() != SendGpsLocationAnswer.SEND ||
				participant.getMapLocation() == null || !canShowParticipantMarkers()) {
			return;
		}
		Marker marker = null;
		for (Marker markerKey : addedMarkers.keySet()) {
			MarkerData markerData = addedMarkers.get(markerKey);
			if (markerData.getValueId() == participant.getId()) {
				marker = markerKey;
				break;
			}
		}
		if (marker != null) {
			marker.setPosition(participant.getMapLocation().getLatLng());
			marker.setSnippet("Last updated: " + participant.getLocationTimestampFormatted());

			// Refresh info window if it's open.
			if (marker.isInfoWindowShown()) {
				marker.showInfoWindow();
			}
		} else {
			marker = map.addMarker(participant.getMarkerOptions());
			addedMarkers.put(marker, new MarkerData(participant.getMarkerOptions(),
					MarkerData.Type.PARTICIPANT_MARKER, participant.getId(), activity));
		}
	}

	public void setTemporaryMarkerIcon(BitmapDescriptor temporaryMarkerIcon) {
		this.temporaryMarkerIcon = temporaryMarkerIcon;
	}

	public void centerCameraToMarkers() {
		if (!addedMarkers.isEmpty() && isMapInitialized()) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (Marker marker : addedMarkers.keySet()) {
				builder.include(marker.getPosition());
			}
			LatLngBounds bounds = builder.build();
			int width = getResources().getDisplayMetrics().widthPixels;
			int height = getResources().getDisplayMetrics().heightPixels;
			int padding = (int) (width * 0.30);
			CameraUpdate cameraUpdate =
					CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
			map.moveCamera(cameraUpdate);
		}
	}

	private class ParticipantMarkerUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Meeting newMeetingInfo = GsonUtil.getJsonObjectFromIntentExtras(intent, Meeting.class,
					Meeting.class.getSimpleName());
			for (Participant participant : newMeetingInfo.getParticipants()) {
				updateParticipantMarker(participant);
			}
		}
	}
}
