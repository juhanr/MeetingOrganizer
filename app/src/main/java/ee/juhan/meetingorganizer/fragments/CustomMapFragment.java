package ee.juhan.meetingorganizer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ee.juhan.meetingorganizer.MainActivity;

public class CustomMapFragment extends MapFragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnCameraChangeListener {

    MainActivity activity;
    private GoogleMap map;
    private LatLng defaultCameraLatLng = new LatLng(59.437046, 24.753742);
    private float defaultCameraZoom = 10;
    private Marker locationMarker;
    private LatLng location;
    private boolean isClickableMap;

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
        View v = super.onCreateView(inflater, container, savedInstanceState);
        initializeMap();
        return v;
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
        locationMarker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Meeting location")
                .snippet("Meeting point"));
        locationMarker.showInfoWindow();
    }

    public void setLocation(LatLng latLng) {
        location = latLng;
    }

    public void setIsClickableMap(boolean isClickableMap) {
        this.isClickableMap = isClickableMap;
    }

    public void clearMap() {
        if (map != null) {
            map.clear();
            location = null;
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
