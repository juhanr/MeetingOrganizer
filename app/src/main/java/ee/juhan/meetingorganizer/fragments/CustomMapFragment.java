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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.activities.LocationActivity;
import ee.juhan.meetingorganizer.fragments.listeners.MyLocationListener;
import ee.juhan.meetingorganizer.util.UIUtil;

public class CustomMapFragment extends MapFragment
		implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapClickListener,
		OnMapReadyCallback {

	private static final LatLng DEFAULT_CAMERA_LOCATION = new LatLng(59.437046, 24.753742);
	private static final float DEFAULT_CAMERA_ZOOM = 10;
	private static final long GPS_MIN_TIME = 1000;
	private static final float GPS_MIN_DISTANCE = 10;
	private static final String TAG = "CustomMapFragment";
	private static int mapVisibility = View.VISIBLE;
	private Activity activity;
	private GoogleMap map;
	private Marker temporaryMarker;
	private Marker focusedMarker;
	private List<Marker> locationMarkers = new ArrayList<>();
	private int maxLocationMarkers = 1;
	private boolean isClickableMap;
	private ViewGroup mapLayout;
	private String temporaryMarkerAddress = "";

	private List<LatLng> markerLocationsToBeAdded = new ArrayList<>();
	private boolean isMapInitialized;

	public static CustomMapFragment newInstance() {
		return new CustomMapFragment();
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
		setUpLocationListener();
		return mapLayout;
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

		for (LatLng latLng : markerLocationsToBeAdded) {
			setLocationMarker(latLng);
			((LocationActivity) activity).refreshConfirmMarkerFABState();
		}
		markerLocationsToBeAdded = null;

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

		isMapInitialized = true;
	}

	private void setUpLocationListener() {
		LocationManager locationManager =
				(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		if (ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED) {
			return;
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME,
				GPS_MIN_DISTANCE, new MyLocationListener());
	}

	private void setFocus(Marker marker, boolean focused) {
		if (focused) {
			focusedMarker = marker;
			if (activity instanceof LocationActivity) {
				((LocationActivity) activity).showDeleteMarkerFAB(true);
			}
		} else {
			if (activity instanceof LocationActivity) {
				((LocationActivity) activity).showDeleteMarkerFAB(false);
			}
		}
	}

	private void setLocationMarker(LatLng latLng) {
		setTemporaryMarker(latLng);
		confirmTemporaryMarker();
		if (activity instanceof LocationActivity) {
			((LocationActivity) activity).refreshConfirmMarkerFABState();
		}
	}

	public void setMarkerLocations(List<LatLng> locations) {
		markerLocationsToBeAdded = locations;
	}

	public void removeLocationMarker(Marker marker) {
		marker.remove();
		locationMarkers.remove(marker);
	}

	private void setTemporaryMarker(LatLng latLng) {
		try {
			Geocoder geocoder = new Geocoder(activity);
			List<Address> addresses =
					geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
			setTemporaryMarker(latLng, addresses.get(0).getAddressLine(0));
		} catch (IOException e) {
			Log.e(TAG, "Could not get location from address string.");
			setTemporaryMarker(latLng, "");
		}
	}

	private void setTemporaryMarker(LatLng latLng, String address) {
		this.temporaryMarkerAddress = address;
		if (temporaryMarker != null) {
			temporaryMarker.remove();
		}
		setFocus(null, false);
		temporaryMarker =
				map.addMarker(new MarkerOptions().position(latLng).title(address).draggable(true));
		temporaryMarker.showInfoWindow();
		if (activity instanceof LocationActivity) {
			((LocationActivity) activity).refreshConfirmMarkerFABState();
		}
	}

	public Marker confirmTemporaryMarker() {
		temporaryMarker
				.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
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
				UIUtil.showToastMessage(activity, getString(R.string.toast_location_not_found));
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not get location from address string.");
			UIUtil.showToastMessage(activity, getString(R.string.toast_geocoder_service_error));
		}
	}

	public final boolean isMapInitialized() {
		return isMapInitialized;
	}

	@Override
	public final boolean onMyLocationButtonClick() {
		LocationManager locationManager =
				(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			UIUtil.showToastMessage(activity, "Waiting for location...");
		} else {
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
		this.map = googleMap;
		initializeMap();
	}
}
