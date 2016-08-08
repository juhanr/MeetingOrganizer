package ee.juhan.meetingorganizer.models;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import ee.juhan.meetingorganizer.models.googleplaces.Place;
import ee.juhan.meetingorganizer.models.server.MapLocation;
import ee.juhan.meetingorganizer.util.LocationUtil;

public class MarkerData {

	public static final BitmapDescriptor PREFERRED_LOCATION_MARKER_ICON =
			BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);

	private MarkerOptions markerOptions;
	private Type type;
	private int valueId;
	private MapLocation mapLocation;

	public MarkerData(MarkerOptions markerOptions, Type type, int valueId, Context context) {
		this.markerOptions = markerOptions;
		this.type = type;
		this.valueId = valueId;
		this.mapLocation = new MapLocation(markerOptions.getPosition().latitude,
				markerOptions.getPosition().longitude);
		this.mapLocation
				.setAddress(LocationUtil.getAddressFromLatLng(mapLocation.getLatLng(), context));
	}

	public MarkerData(MarkerOptions markerOptions, Type type, Context context) {
		this.markerOptions = markerOptions;
		this.type = type;
		this.mapLocation = new MapLocation(markerOptions.getPosition().latitude,
				markerOptions.getPosition().longitude);
		this.mapLocation
				.setAddress(LocationUtil.getAddressFromLatLng(mapLocation.getLatLng(), context));
	}

	public MarkerData(MarkerData.Type markerType, MapLocation mapLocation, Context context) {
		this.type = markerType;
		if (mapLocation.getPlaceName() == null && markerType == Type.CONFIRMED_LOCATION_MARKER) {
			mapLocation.setPlaceName("Meeting location");
		}
		if (mapLocation.getAddress() == null) {
			mapLocation.setAddress(
					LocationUtil.getAddressFromLatLng(mapLocation.getLatLng(), context));
		}
		String snippetText = String.format("%s\n%s", mapLocation.getAddressFormatted(),
				mapLocation.getPlacesTypesFormatted()).trim();
		this.markerOptions = new MarkerOptions().draggable(false).title(mapLocation.getPlaceName())
				.snippet(snippetText).position(mapLocation.getLatLng());
		this.mapLocation = mapLocation;
	}

	public MarkerData(Place place, Context context) {
		this.mapLocation = place.getMapLocation();
		if (mapLocation.getAddress() == null) {
			mapLocation.setAddress(
					LocationUtil.getAddressFromLatLng(mapLocation.getLatLng(), context));
		}
		String snippetText = String.format("%s\n%s", mapLocation.getAddressFormatted(),
				mapLocation.getPlacesTypesFormatted()).trim();
		this.markerOptions = new MarkerOptions().draggable(false).title(mapLocation.getPlaceName())
				.snippet(snippetText).position(mapLocation.getLatLng());
		this.type = Type.RECOMMENDED_PLACE_MARKER;
	}

	public MarkerOptions getMarkerOptions() {
		return markerOptions;
	}

	public void setMarkerOptions(MarkerOptions markerOptions) {
		this.markerOptions = markerOptions;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getValueId() {
		return valueId;
	}

	public void setValueId(int valueId) {
		this.valueId = valueId;
	}

	public MapLocation getMapLocation() {
		return mapLocation;
	}

	public void setMapLocation(MapLocation mapLocation) {
		this.mapLocation = mapLocation;
	}

	public void addSnippetLine(String line) {
		markerOptions.snippet(String.format("%s\n%s", markerOptions.getSnippet(), line).trim());
	}

	public enum Type {
		TEMPORARY_MARKER, PARTICIPANT_MARKER, RECOMMENDED_PLACE_MARKER,
		CONFIRMED_LOCATION_MARKER, PREFERRED_LOCATION_MARKER
	}
}