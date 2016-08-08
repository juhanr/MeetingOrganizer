package ee.juhan.meetingorganizer.models.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.juhan.meetingorganizer.util.DateUtil;

public class Meeting {

	public Date startDateTime;
	private int id;
	private int leaderId;
	private String title;
	private String description;
	private Date endDateTime;
	private MapLocation mapLocation;
	private LocationChoice locationChoice = LocationChoice.NOT_SET;
	private List<Participant> participants = new ArrayList<>();
	private List<MapLocation> userPreferredLocations = new ArrayList<>();
	private MeetingStatus status = MeetingStatus.ACTIVE;

	// App-specific values, not used in server
	private boolean isQuickMeeting = true;
	private boolean isUTCTimeZone = false;

	public Meeting() {}

	public Meeting(int id, int leaderId, String title, String description, Date startDateTime,
			Date endDateTime, MapLocation mapLocation, LocationChoice locationChoice,
			MeetingStatus status) {
		this.id = id;
		this.leaderId = leaderId;
		this.title = title;
		this.description = description;
		this.setStartDateTime(startDateTime);
		this.setEndDateTime(endDateTime);
		this.mapLocation = mapLocation;
		this.locationChoice = locationChoice;
		this.status = status;
	}

	public Meeting(int leaderId, String title, String description, Date startDateTime,
			Date endDateTime, LocationChoice locationChoice, MeetingStatus status) {
		this.leaderId = leaderId;
		this.title = title;
		this.description = description;
		this.setStartDateTime(startDateTime);
		this.setEndDateTime(endDateTime);
		this.locationChoice = locationChoice;
		this.status = status;
	}

	public Meeting(String title, String description, MapLocation mapLocation,
			LocationChoice locationChoice, List<Participant> participants, MeetingStatus status) {
		this.title = title;
		this.description = description;
		this.mapLocation = mapLocation;
		this.locationChoice = locationChoice;
		this.participants = participants;
		this.status = status;
	}

	@Override
	public String toString() {
		return "Meeting{" +
				"startDateTime=" + startDateTime +
				", id=" + id +
				", leaderId=" + leaderId +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", endDateTime=" + endDateTime +
				", mapLocation=" + mapLocation +
				", locationChoice=" + locationChoice +
				", participants=" + participants +
				", userPreferredLocations=" + userPreferredLocations +
				", status=" + status +
				", isQuickMeeting=" + isQuickMeeting +
				", isUTCTimeZone=" + isUTCTimeZone +
				'}';
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
		return isUTCTimeZone ? DateUtil.toLocalTimezone(startDateTime) : startDateTime;
	}

	public final void setStartDateTime(Date startTime) {
		this.startDateTime = DateUtil.toUTCTimezone(startTime);
		this.isUTCTimeZone = true;
	}

	public final Date getEndDateTime() {
		return isUTCTimeZone ? DateUtil.toLocalTimezone(endDateTime) : endDateTime;
	}

	public final void setEndDateTime(Date endTime) {
		this.endDateTime = DateUtil.toUTCTimezone(endTime);
		this.isUTCTimeZone = true;
	}

	public MapLocation getMapLocation() {
		return mapLocation;
	}

	public void setMapLocation(MapLocation mapLocation) {
		this.mapLocation = mapLocation;
	}

	public final LocationChoice getLocationChoice() {
		return locationChoice;
	}

	public final void setLocationChoice(LocationChoice locationChoice) {
		this.locationChoice = locationChoice;
	}

	public final List<Participant> getParticipants() {
		return participants;
	}

	public final void setParticipants(List<Participant> participants) {
		this.participants = participants;
	}

	public final boolean addParticipant(Participant participant) {
		return participants.add(participant);
	}

	public final List<MapLocation> getUserPreferredLocations() {
		return userPreferredLocations;
	}

	public final void setUserPreferredLocations(List<MapLocation> userPreferredLocations) {
		this.userPreferredLocations = userPreferredLocations;
	}

	public final void addUserPreferredLocation(MapLocation userPreferredLocation) {
		this.userPreferredLocations.add(userPreferredLocation);
	}

	public final void removeUserPreferredLocation(MapLocation userPreferredLocation) {
		this.userPreferredLocations.remove(userPreferredLocation);
	}

	public MeetingStatus getStatus() {
		return status;
	}

	public void setStatus(MeetingStatus status) {
		this.status = status;
	}

	public boolean isQuickMeeting() {
		return isQuickMeeting;
	}

	public void setQuickMeeting(boolean quickMeeting) {
		isQuickMeeting = quickMeeting;
	}

	public boolean isOngoing() {
		Date currentTime = new Date();
		return getStartDateTime().before(currentTime) && getEndDateTime().after(currentTime);
	}
}
