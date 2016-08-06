package ee.juhan.meetingorganizer.services;

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

import java.util.Date;

import ee.juhan.meetingorganizer.activities.MainActivity;
import ee.juhan.meetingorganizer.models.server.MapCoordinate;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.network.RestClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LocationService
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		LocationListener {

	private static final String TAG = LocationService.class.getSimpleName();
	private static final int REQUEST_CHECK_SETTINGS = 1000;
	private static final long GPS_INTERVAL = 10000;
	private static final long GPS_FASTEST_INTERVAL = 5000;

	private static MapCoordinate gpsLocation;
	private static Date gpsLocationTimestamp;
	private static boolean isRunning;
	private static GoogleApiClient googleApiClient;
	private Activity activity;
	private LocationRequest locationRequest;
	private boolean requestLocationOnlyOnce;

	public LocationService(Activity activity, boolean requestLocationOnlyOnce) {
		this.activity = activity;
		this.requestLocationOnlyOnce = requestLocationOnlyOnce;
		googleApiClient = new GoogleApiClient.Builder(activity).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

		locationRequest = new LocationRequest();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(GPS_INTERVAL);
		locationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);
	}

	public static MapCoordinate getGpsLocation() {
		return LocationService.gpsLocation;
	}

	public static Date getGpsLocationTimestamp() {
		return LocationService.gpsLocationTimestamp;
	}

	public static void connect() {
		if (!isRunning) {
			googleApiClient.connect();
		}
		isRunning = true;
	}

	public static void disconnect() {
		if (isRunning) {
			googleApiClient.disconnect();
		}
		isRunning = false;
	}

	@Override
	public final void onLocationChanged(Location loc) {
		LocationService.gpsLocation = new MapCoordinate(loc.getLatitude(), loc.getLongitude());
		LocationService.gpsLocationTimestamp = new Date();
		Log.d(TAG, loc.getLatitude() + ", " + loc.getLongitude());
		sendLocationRequest();
		if (requestLocationOnlyOnce) {
			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			isRunning = false;
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
		if (lastLocation == null || !requestLocationOnlyOnce) {
			checkGpsSettings();
			LocationServices.FusedLocationApi
					.requestLocationUpdates(googleApiClient, locationRequest, this);
		} else {
			LocationService.gpsLocation =
					new MapCoordinate(lastLocation.getLatitude(), lastLocation.getLongitude());
			LocationService.gpsLocationTimestamp = new Date();
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

	public void checkGpsSettings() {
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

	private void sendLocationRequest() {
		Participant dummyParticipant =
				new Participant(MainActivity.getAccountId(), LocationService.gpsLocation,
						LocationService.gpsLocationTimestamp);
		RestClient.get()
				.updateParticipantLocationRequest(dummyParticipant, new Callback<Boolean>() {
					@Override
					public void success(Boolean mustSendMoreLocationUpdates, Response response) {
						if (!mustSendMoreLocationUpdates) {
							LocationService.disconnect();
						}
					}

					@Override
					public void failure(RetrofitError error) {

					}
				});
	}
}