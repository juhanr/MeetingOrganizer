package ee.juhan.meetingorganizer.models.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.juhan.meetingorganizer.util.DateUtil;

public class MeetingDTO {

	public Date startDateTime;
	private int id;
	private int leaderId;
	private String title;
	private String description;
	private Date endDateTime;
	private MapCoordinate location;
	private LocationType locationType = LocationType.SPECIFIC_LOCATION;
	private List<ParticipantDTO> participants = new ArrayList<>();
	private Set<MapCoordinate> predefinedLocations = new HashSet<>();

	public MeetingDTO() {

	}

	public MeetingDTO(int id, int leaderId, String title, String description, Date startDateTime,
			Date endDateTime, MapCoordinate location, LocationType locationType) {
		this.id = id;
		this.leaderId = leaderId;
		this.title = title;
		this.description = description;
		this.setStartDateTime(startDateTime);
		this.setEndDateTime(endDateTime);
		this.location = location;
		this.locationType = locationType;
	}

	public MeetingDTO(int leaderId, String title, String description, Date startDateTime,
			Date endDateTime, LocationType locationType) {
		this.leaderId = leaderId;
		this.title = title;
		this.description = description;
		this.setStartDateTime(startDateTime);
		this.setEndDateTime(endDateTime);
		this.locationType = locationType;
	}

	public MeetingDTO(String title, String description, MapCoordinate location,
			LocationType locationType, List<ParticipantDTO> participants) {
		this.title = title;
		this.description = description;
		this.location = location;
		this.locationType = locationType;
		this.participants = participants;
	}

	public final int getId() {
		return id;
	}

	public final void setId(int id) {
		this.id = id;
	}

	public final int getLeaderId() {
		return leaderId;
	}

	public final void setLeaderId(int leaderId) {
		this.leaderId = leaderId;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		this.title = title;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	public final Date getStartDateTime() {
		return DateUtil.toLocalTimezone(startDateTime);
	}

	public final void setStartDateTime(Date startTime) {
		this.startDateTime = DateUtil.toUTCTimezone(startTime);
	}

	public final Date getEndDateTime() {
		return DateUtil.toLocalTimezone(endDateTime);
	}

	public final void setEndDateTime(Date endTime) {
		this.endDateTime = DateUtil.toUTCTimezone(endTime);
	}

	public final MapCoordinate getLocation() {
		return location;
	}

	public final void setLocation(MapCoordinate location) {
		this.location = location;
	}

	public final LocationType getLocationType() {
		return locationType;
	}

	public final void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}

	public final List<ParticipantDTO> getParticipants() {
		return participants;
	}

	public final void setParticipants(List<ParticipantDTO> participants) {
		this.participants = participants;
	}

	public final boolean addParticipant(ParticipantDTO participant) {
		return participants.add(participant);
	}

	public final Set<MapCoordinate> getPredefinedLocations() {
		return predefinedLocations;
	}

	public final void setPredefinedLocations(Set<MapCoordinate> predefinedLocations) {
		this.predefinedLocations = predefinedLocations;
	}

	public final void addPredefinedLocation(MapCoordinate predefinedLocation) {
		this.predefinedLocations.add(predefinedLocation);
	}

	public final void removePredefinedLocation(MapCoordinate predefinedLocation) {
		this.predefinedLocations.remove(predefinedLocation);
	}

	public final void toUTCTimeZone() {
		if (startDateTime != null) {
			this.startDateTime = DateUtil.toUTCTimezone(this.startDateTime);
		}
		if (endDateTime != null) {
			this.endDateTime = DateUtil.toUTCTimezone(this.endDateTime);
		}
	}

	public final void toLocalTimeZone() {
		if (startDateTime != null) {
			this.startDateTime = DateUtil.toLocalTimezone(this.startDateTime);
		}
		if (endDateTime != null) {
			this.endDateTime = DateUtil.toLocalTimezone(this.endDateTime);
		}
	}

}
