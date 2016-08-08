package ee.juhan.meetingorganizer.models.googleplaces;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import ee.juhan.meetingorganizer.models.server.MapLocation;

public class Place {

	private String id;
	private String icon;
	private String vicinity;
	private String name;
	private String[] types;
	private Geometry geometry;
	private Double distanceFromRecommendedLocation;

	@SerializedName("place_id")
	private String placeId;

	@SerializedName("opening_hours")
	private OpeningHours openingHours;

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		Place place = (Place) o;

		return id.equals(place.id);

	}

	@Override
	public String toString() {
		return "Place{" +
				"id='" + id + '\'' +
				", placeId='" + placeId + '\'' +
				", icon='" + icon + '\'' +
				", vicinity='" + vicinity + '\'' +
				", name='" + name + '\'' +
				", types=" + Arrays.toString(types) +
				", geometry=" + geometry +
				", openingHours=" + openingHours +
				'}';
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getVicinity() {
		return vicinity;
	}

	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getTypes() {
		return types;
	}

	public void setTypes(String[] types) {
		this.types = types;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public OpeningHours getOpeningHours() {
		return openingHours;
	}

	public void setOpeningHours(OpeningHours openingHours) {
		this.openingHours = openingHours;
	}

	public Double getDistanceFromRecommendedLocation() {
		return distanceFromRecommendedLocation;
	}

	public void setDistanceFromRecommendedLocation(Double distanceFromRecommendedLocation) {
		this.distanceFromRecommendedLocation = distanceFromRecommendedLocation;
	}

	public MapLocation getMapLocation() {
		return new MapLocation(geometry.getLocation().getLat(), geometry.getLocation().getLng(),
				vicinity, name, types, distanceFromRecommendedLocation);
	}

	public class Geometry {

		private Place.Location location;

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}
	}

	public class Location {

		private Double lng;
		private Double lat;

		public Double getLng() {
			return lng;
		}

		public void setLng(Double lng) {
			this.lng = lng;
		}

		public Double getLat() {
			return lat;
		}

		public void setLat(Double lat) {
			this.lat = lat;
		}

	}

	public class OpeningHours {

		@SerializedName("open_now")
		private boolean openNow;

		@SerializedName("weekday_text")
		private String[] weekdayText;

		public boolean isOpenNow() {
			return openNow;
		}

		public void setOpenNow(boolean openNow) {
			this.openNow = openNow;
		}

		public String[] getWeekdayText() {
			return weekdayText;
		}

		public void setWeekdayText(String[] weekdayText) {
			this.weekdayText = weekdayText;
		}

	}
}
