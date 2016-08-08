package ee.juhan.meetingorganizer.models.server;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ee.juhan.meetingorganizer.util.StringUtil;

public class MapLocation {

	private double latitude;
	private double longitude;
	private String address;
	private String placeName;
	private String[] placeTypes;

	// App-specific values, not used in server
	private Double distanceFromRecommendedLocation;

	public MapLocation() {}

	public MapLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}


	public MapLocation(double latitude, double longitude, String address, String placeName,
			String[] placeTypes, Double distanceFromRecommendedLocation) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.placeName = placeName;
		this.placeTypes = placeTypes;
		this.distanceFromRecommendedLocation = distanceFromRecommendedLocation;
	}

	public MapLocation(LatLng latLng) {
		this.latitude = latLng.latitude;
		this.longitude = latLng.longitude;
	}

	@Override
	public String toString() {
		return "MapLocation{" +
				"latitude=" + latitude +
				", longitude=" + longitude +
				", address='" + address + '\'' +
				", placeName='" + placeName + '\'' +
				", placeTypes=" + Arrays.toString(placeTypes) +
				", distanceFromRecommendedLocation=" + distanceFromRecommendedLocation +
				'}';
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}

	public void setLatLng(LatLng latLng) {
		this.latitude = latLng.latitude;
		this.longitude = latLng.longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public String[] getPlaceTypes() {
		return placeTypes;
	}

	public void setPlaceTypes(String[] placeTypes) {
		this.placeTypes = placeTypes;
	}

	public Double getDistanceFromRecommendedLocation() {
		return distanceFromRecommendedLocation;
	}

	public void setDistanceFromRecommendedLocation(Double distanceFromRecommendedLocation) {
		this.distanceFromRecommendedLocation = distanceFromRecommendedLocation;
	}

	public String getAddressFormatted() {
		return address == null ? "" : "Address: " + address;
	}

	public String getPlacesTypesFormatted() {
		if (placeTypes == null) {
			return "";
		}
		List<String> types = new ArrayList<>();
		for (String placeType : placeTypes) {
			switch (placeType) {
				case "bar":
					types.add("Bar");
					break;
				case "cafe":
					types.add("Cafe");
					break;
				case "restaurant":
					types.add("Restaurant");
					break;
				case "park":
					types.add("Park");
					break;
				case "movie_theater":
					types.add("Movie theater");
					break;
				case "night_club":
					types.add("Night club");
					break;
				default:
					break;
			}
		}
		return types.isEmpty() ? "" : "Type: " + StringUtil.concatStrings(types, ", ");
	}
}
