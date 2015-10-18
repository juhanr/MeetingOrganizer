package ee.juhan.meetingorganizer.fragments;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;
import ee.juhan.meetingorganizer.fragments.listeners.MyLocationListener;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.MapCoordinate;

public class CustomMapFragment extends MapFragment
		implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapClickListener {

	private static final LatLng DEFAULT_CAMERA_LOCATION = new LatLng(59.437046, 24.753742);
	private static final float DEFAULT_CAMERA_ZOOM = 10;
	private static final long GPS_MIN_TIME = 1000;
	private static final float GPS_MIN_DISTANCE = 10;
	private static final String TAG = "CustomMapFragment";
	private static int mapVisibility = View.VISIBLE;
	private MainActivity activity;
	private GoogleMap map;
	private Marker locationMarker;
	private LatLng location;
	private boolean isClickableMap;
	private ViewGroup mapLayout;
	private String markerAddress = "";

	public CustomMapFragment() {}

	public static CustomMapFragment newInstance() {
		return new CustomMapFragment();
	}

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mapLayout = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		checkAndroidVersion();
		initializeMap();
		setUpLocationListener();
		TouchableWrapper frameLayout = new TouchableWrapper(getActivity());
		frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		mapLayout.addView(frameLayout,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
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
		map = this.getMap();
		if (map == null) {
			return;
		}
		map.setMyLocationEnabled(true);
		map.setOnMyLocationButtonClickListener(this);
		if (isClickableMap) {
			map.setOnMapClickListener(this);
		}
		if (location == null) {
			map.moveCamera(CameraUpdateFactory
					.newLatLngZoom(DEFAULT_CAMERA_LOCATION, DEFAULT_CAMERA_ZOOM));
		} else {
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_CAMERA_ZOOM));
			setLocationMarker(location);
		}
	}

	private void setUpLocationListener() {
		LocationManager locationManager =
				(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME,
				GPS_MIN_DISTANCE, new MyLocationListener());
	}

	private void setLocationMarker(LatLng latLng) {
		try {
			Geocoder geocoder = new Geocoder(activity.getBaseContext());
			List<Address> addresses =
					geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
			setLocationMarker(latLng, addresses.get(0).getAddressLine(0));
		} catch (IOException e) {
			Log.e(TAG, "Could not get location from address string.");
			setLocationMarker(latLng, "");
		}
	}

	private void setLocationMarker(LatLng latLng, String address) {
		this.location = latLng;
		this.markerAddress = address;
		if (locationMarker != null) {
			locationMarker.remove();
		}
		locationMarker = map.addMarker(new MarkerOptions().position(latLng)
				.title(getString(R.string.textview_meeting_location)).snippet(address));
		locationMarker.showInfoWindow();
	}

	public final LatLng getLocation() {
		return location;
	}

	public final void setLocation(LatLng latLng) {
		location = latLng;
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

	public final String getMarkerAddress() {
		return markerAddress;
	}

	public final void clearMap() {
		if (map != null) {
			map.clear();
			location = null;
			markerAddress = "";
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
				setLocationMarker(latLng, addresses.get(0).getAddressLine(0));
			} else {
				activity.showToastMessage(getString(R.string.toast_location_not_found));
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not get location from address string.");
			activity.showToastMessage(getString(R.string.toast_geocoder_service_error));
		}
	}

	@Override
	public final boolean onMyLocationButtonClick() {
		LocationManager locationManager =
				(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			activity.showToastMessage("Waiting for location...");
		} else {
			activity.showToastMessage("GPS is disabled!");
		}
		return false;
	}

	@Override
	public final void onMapClick(LatLng latLng) {
		if (NewMeetingFragment.getNewMeetingModel().getLocationType() ==
				LocationType.SPECIFIC_LOCATION) {
			NewMeetingFragment.getNewMeetingModel()
					.setLocation(new MapCoordinate(latLng.latitude, latLng.longitude));
		}
		if (locationMarker != null) {
			locationMarker.remove();
		}
		setLocationMarker(latLng);
	}

	private class TouchableWrapper extends FrameLayout {

		public TouchableWrapper(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN ||
					event.getAction() == MotionEvent.ACTION_UP) {
				mapLayout.requestDisallowInterceptTouchEvent(true);
			}
			return super.dispatchTouchEvent(event);
		}
	}

}
