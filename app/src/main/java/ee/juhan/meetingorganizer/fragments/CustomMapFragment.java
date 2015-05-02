package ee.juhan.meetingorganizer.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import ee.juhan.meetingorganizer.MainActivity;
import ee.juhan.meetingorganizer.R;

public class CustomMapFragment extends MapFragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnCameraChangeListener {

    private static int mapVisibility;
    private MainActivity activity;
    private GoogleMap map;
    private LatLng defaultCameraLatLng = new LatLng(59.437046, 24.753742);
    private float defaultCameraZoom = 10;
    private Marker locationMarker;
    private LatLng location;
    private boolean isClickableMap;
    private ViewGroup mapLayout;

    public CustomMapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mapLayout = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        checkAndroidVersion();
        initializeMap();
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
        if (map != null) {
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnCameraChangeListener(this);
            if (isClickableMap) {
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        NewMeetingFragment.newMeetingModel.setLocationLatitude(latLng.latitude);
                        NewMeetingFragment.newMeetingModel.setLocationLongitude(latLng.longitude);
                        if (locationMarker != null)
                            locationMarker.remove();
                        setLocationMarker(latLng);
                    }
                });
            }
            if (location == null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        defaultCameraLatLng, defaultCameraZoom));
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        location, defaultCameraZoom));
                setLocationMarker(location);
            }
        }
    }

    private void setLocationMarker(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(activity.getBaseContext());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            setLocationMarker(latLng, addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
            setLocationMarker(latLng, "");
        }
    }

    private void setLocationMarker(LatLng latLng, String address) {
        if (locationMarker != null)
            locationMarker.remove();
        locationMarker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.textview_meeting_location))
                .snippet(address));
        locationMarker.showInfoWindow();
    }


    public void setLocation(LatLng latLng) {
        location = latLng;
    }

    public void setIsClickableMap(boolean isClickableMap) {
        this.isClickableMap = isClickableMap;
    }

    public void setMapVisibility(int visibility) {
        CustomMapFragment.mapVisibility = visibility;
        if (mapLayout != null)
            mapLayout.setVisibility(visibility);
    }

    public void clearMap() {
        if (map != null) {
            map.clear();
            location = null;
        }
    }

    public void searchMap(String locationName) {
        try {
            Geocoder geocoder = new Geocoder(activity.getBaseContext());
            List<Address> addresses = geocoder.getFromLocationName(locationName, 3);
            if (addresses.size() > 0) {
                LatLng latLng = new LatLng(addresses.get(0).getLatitude(),
                        addresses.get(0).getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        latLng, defaultCameraZoom));
                setLocationMarker(latLng, addresses.get(0).getAddressLine(0));
            } else {
                activity.showToastMessage(getString(R.string.toast_location_not_found));
            }
        } catch (IOException e) {
            e.printStackTrace();
            activity.showToastMessage(getString(R.string.toast_geocoder_service_error));
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

}
