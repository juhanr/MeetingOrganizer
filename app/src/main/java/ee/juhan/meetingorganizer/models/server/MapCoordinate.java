package ee.juhan.meetingorganizer.models.server;

import com.google.android.gms.maps.model.LatLng;

public class MapCoordinate {

	private Double latitude;
	private Double longitude;

	// App-specific values, not used in server
	private Double distanceFromRecommendedLocation;

	public MapCoordinate() {}

	public MapCoordinate(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public MapCoordinate(LatLng latLng) {
		this.latitude = latLng.latitude;
		this.longitude = latLng.longitude;
	}

	@Override
	public int hashCode() {
		int result = latitude.hashCode();
		result = 31 * result + longitude.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		MapCoordinate that = (MapCoordinate) o;
		return latitude.equals(that.latitude) && longitude.equals(that.longitude);
	}

	public final Double getLatitude() {
		return latitude;
	}

	public final void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public final Double getLongitude() {
		return longitude;
	}

	public final void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public final LatLng toLatLng() {
		return new LatLng(this.latitude, this.longitude);
	}

	public Double getDistanceFromRecommendedLocation() {
		return distanceFromRecommendedLocation;
	}

	public void setDistanceFromRecommendedLocation(Double distanceFromRecommendedLocation) {
		this.distanceFromRecommendedLocation = distanceFromRecommendedLocation;
	}
}
