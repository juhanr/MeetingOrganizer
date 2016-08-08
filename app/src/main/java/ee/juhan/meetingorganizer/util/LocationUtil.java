package ee.juhan.meetingorganizer.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ee.juhan.meetingorganizer.models.server.LocationChoice;
import ee.juhan.meetingorganizer.models.server.MapLocation;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;

public final class LocationUtil {

	private LocationUtil() {}

	public static LatLng getCenterCoordinate(Meeting meeting) {
		ArrayList<Double> xCoordinates = new ArrayList<>();
		ArrayList<Double> yCoordinates = new ArrayList<>();
		ArrayList<Double> zCoordinates = new ArrayList<>();
		List<Participant> participants = meeting.getParticipants();

		// Convert lat/lon to Cartesian coordinates for each location.
		for (Participant participant : participants) {
			if (participant.getMapLocation() != null) {
				Double latitude = Math.toRadians(participant.getMapLocation().getLatitude());
				Double longitude = Math.toRadians(participant.getMapLocation().getLongitude());
				xCoordinates.add(Math.cos(latitude) * Math.cos(longitude));
				yCoordinates.add(Math.cos(latitude) * Math.sin(longitude));
				zCoordinates.add(Math.sin(latitude));
			}
		}

		// Compute average x, y and z coordinates.
		Double xSum = 0.0;
		Double ySum = 0.0;
		Double zSum = 0.0;

		for (int i = 0; i < xCoordinates.size(); i++) {
			xSum += xCoordinates.get(i);
			ySum += yCoordinates.get(i);
			zSum += zCoordinates.get(i);
		}

		Double xAverage = xSum / xCoordinates.size();
		Double yAverage = ySum / yCoordinates.size();
		Double zAverage = zSum / zCoordinates.size();

		// Convert average x, y, z coordinate to latitude and longitude.
		Double hyp = Math.sqrt(xAverage * xAverage + yAverage * yAverage);
		Double latitude = Math.toDegrees(Math.atan2(zAverage, hyp));
		Double longitude = Math.toDegrees(Math.atan2(yAverage, xAverage));
		return new LatLng(latitude, longitude);
	}

	public static List<MapLocation> getUserPreferredSortedByRecommendation(Meeting meeting,
			LatLng centerCoordinate) {
		if (meeting.getLocationChoice() != LocationChoice.RECOMMENDED_FROM_PREFERRED_LOCATIONS) {
			return null;
		}

		List<MapLocation> userPreferredSorted = new ArrayList<>();
		for (MapLocation mapLocation : meeting.getUserPreferredLocations()) {
			mapLocation.setDistanceFromRecommendedLocation(
					getDistance(mapLocation.getLatLng(), centerCoordinate));
			userPreferredSorted.add(mapLocation);
		}
		Collections.sort(userPreferredSorted,
				(coordinate1, coordinate2) -> coordinate1.getDistanceFromRecommendedLocation()
						.compareTo(coordinate2.getDistanceFromRecommendedLocation()));
		return userPreferredSorted;
	}

	//	private static MapLocation getNearestLocation(MapLocation coordinate,
	//			Set<MapLocation> locations) {
	//		MapLocation nearestLocation = null;
	//		Double smallestDistance = Double.POSITIVE_INFINITY;
	//		for (MapLocation location : locations) {
	//			Double distance = getDistance(coordinate, location);
	//			if (distance < smallestDistance) {
	//				smallestDistance = distance;
	//				nearestLocation = location;
	//			}
	//		}
	//		return nearestLocation;
	//	}

	public static Double getDistance(LatLng coordinate1, LatLng coordinate2) {
		final int earthRadius = 6371;
		Double dLat = Math.toRadians(coordinate2.latitude - coordinate1.latitude);
		Double dLon = Math.toRadians(coordinate2.longitude - coordinate1.longitude);
		Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(Math.toRadians(coordinate1.latitude)) *
						Math.cos(Math.toRadians(coordinate2.latitude)) * Math.sin(dLon / 2) *
						Math.sin(dLon / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return earthRadius * c;
	}

	public static String getAddressFromLatLng(LatLng latLng, Context context) {
		try {
			Geocoder geocoder = new Geocoder(context);
			List<Address> addresses =
					geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
			if (addresses.size() > 0) {
				return addresses.get(0).getAddressLine(0);
			}
		} catch (IOException e) {
			Log.e("Debug", "Could not get address string from location.");
		}
		return "Unknown address";
	}

}