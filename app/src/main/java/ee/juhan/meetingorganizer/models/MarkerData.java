package ee.juhan.meetingorganizer.models;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import ee.juhan.meetingorganizer.fragments.CustomMapFragment;
import ee.juhan.meetingorganizer.models.server.LocationType;
import ee.juhan.meetingorganizer.models.server.MapCoordinate;
import ee.juhan.meetingorganizer.models.server.Meeting;

public class MarkerData {

	public static final BitmapDescriptor PREFERRED_LOCATION_MARKER_ICON =
			BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);

	private MarkerOptions markerOptions;
	private Type type;
	private int valueId;

	public MarkerData(MarkerOptions markerOptions, Type type, int valueId) {
		this.markerOptions = markerOptions;
		this.type = type;
		this.valueId = valueId;
	}

	public MarkerData(MarkerOptions markerOptions, Type type) {
		this.markerOptions = markerOptions;
		this.type = type;
	}

	public MarkerData(Meeting meeting) {
		if (meeting.getLocationType() == LocationType.SPECIFIC_LOCATION) {
			this.markerOptions = new MarkerOptions().draggable(false)
					.title(CustomMapFragment.getAddressFromLatLng(meeting.getLocation().toLatLng()))
					.position(meeting.getLocation().toLatLng());
			this.type = Type.CONFIRMED_LOCATION_MARKER;
		}
	}

	public MarkerData(Meeting meeting, MapCoordinate mapCoordinate) {
		if (meeting.getLocationType() == LocationType.GENERATED_FROM_PREFERRED_LOCATIONS) {
			this.markerOptions = new MarkerOptions().draggable(false)
					.title(CustomMapFragment.getAddressFromLatLng(mapCoordinate.toLatLng()))
					.position(mapCoordinate.toLatLng());
			this.type = Type.PREFERRED_LOCATION_MARKER;
		}
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

	public enum Type {
		TEMPORARY_MARKER, PARTICIPANT_MARKER, RECOMMENDED_LOCATION_MARKER,
		CONFIRMED_LOCATION_MARKER, PREFERRED_LOCATION_MARKER
	}
}