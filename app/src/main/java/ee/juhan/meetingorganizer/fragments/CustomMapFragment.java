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

    public CustomMapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        activity = (MainActivity) getActivity();
        initializeMap();
        return v;
    }

    private void initializeMap() {
        map = this.getMap();
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    defaultCameraLatLng, defaultCameraZoom));
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnCameraChangeListener(this);
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    ChooseLocationFragment.location = latLng;
                    if (locationMarker != null)
                        locationMarker.remove();
                    locationMarker = map.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Meeting point")
                            .snippet("Meeting point"));
                }
            });
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
