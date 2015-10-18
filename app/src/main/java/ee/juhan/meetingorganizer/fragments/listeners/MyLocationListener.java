package ee.juhan.meetingorganizer.fragments.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import ee.juhan.meetingorganizer.models.server.MapCoordinate;

public class MyLocationListener implements LocationListener {

	private static MapCoordinate myLocation;

	public static MapCoordinate getMyLocation() {
		return MyLocationListener.myLocation;
	}

	@Override
	public final void onLocationChanged(Location loc) {
		MyLocationListener.myLocation = new MapCoordinate(loc.getLatitude(), loc.getLongitude());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

}