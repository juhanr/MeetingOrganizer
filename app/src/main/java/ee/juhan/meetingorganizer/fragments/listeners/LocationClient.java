package ee.juhan.meetingorganizer.fragments.listeners;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import ee.juhan.meetingorganizer.models.server.MapCoordinate;

public class LocationClient
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		LocationListener {

	private static final String TAG = LocationClient.class.getSimpleName();
	private static final int REQUEST_CHECK_SETTINGS = 1000;
	private static final long GPS_INTERVAL = 3000;
	private static final long GPS_FASTEST_INTERVAL = 1000;

	private static MapCoordinate myLocation;
	private Activity activity;
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
	private boolean requestLocationOnlyOnce;

	public LocationClient(Activity activity, boolean requestLocationOnlyOnce) {
		this.activity = activity;
		this.requestLocationOnlyOnce = requestLocationOnlyOnce;
		googleApiClient = new GoogleApiClient.Builder(activity).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

		locationRequest = new LocationRequest();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(GPS_INTERVAL);
		locationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);

		LocationSettingsRequest.Builder builder =
				new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
		builder.setAlwaysShow(true);
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
				.checkLocationSettings(googleApiClient, builder.build());
		result.setResultCallback(result1 -> {
			final Status status = result1.getStatus();
			switch (status.getStatusCode()) {
				case LocationSettingsStatusCodes.SUCCESS:
					break;
				case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
					try {
						status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
					} catch (IntentSender.SendIntentException e) {
						// Ignore the error.
					}
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
					break;
			}
		});
	}

	public static MapCoordinate getMyLocation() {
		return LocationClient.myLocation;
	}

	@Override
	public final void onLocationChanged(Location loc) {
		LocationClient.myLocation = new MapCoordinate(loc.getLatitude(), loc.getLongitude());
		Log.d(TAG, loc.getLatitude() + ", " + loc.getLongitude());
		if (requestLocationOnlyOnce) {
			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
		}
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		if (ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED && ActivityCompat
				.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
				PackageManager.PERMISSION_GRANTED) {
			return;
		}
		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
		if (lastLocation == null || requestLocationOnlyOnce) {
			LocationServices.FusedLocationApi
					.requestLocationUpdates(googleApiClient, locationRequest, this);
		} else {
			LocationClient.myLocation =
					new MapCoordinate(lastLocation.getLatitude(), lastLocation.getLongitude());
			Log.d(TAG, lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
		}
		Log.d(TAG, "Connection started");
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "Connection suspended");
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.d(TAG, "Connection failed");
	}

	public void connect() {
		googleApiClient.connect();
	}

	public void disconnect() {
		googleApiClient.disconnect();
	}
}