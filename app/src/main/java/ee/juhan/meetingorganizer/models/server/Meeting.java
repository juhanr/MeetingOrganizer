package ee.juhan.meetingorganizer.models.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.juhan.meetingorganizer.util.DateUtil;

public class Meeting {

	public Date startDateTime;
	private int id;
	private int leaderId;
	private String title;
	private String description;
	private Date endDateTime;
	private MapCoordinate location;
	private LocationType locationType = LocationType.NOT_SET;
	private String locationName;
	private List<Participant> participants = new ArrayList<>();
	private Set<MapCoordinate> userPreferredLocations = new HashSet<>();
	private List<MapCoordinate> recommendedLocations = new ArrayList<>();
	private MeetingStatus status = MeetingStatus.ACTIVE;

	// App-specific values, not used in server
	private boolean isQuickMeeting = true;
	private boolean isUTCTimeZone = false;

	public Meeting() {}

	public Meeting(int id, int leaderId, String title, String description, Date startDateTime,
			Date endDateTime, MapCoordinate location, LocationType locationType,
			String locationName, MeetingStatus status) {
		this.id = id;
		this.leaderId = leaderId;
		this.title = title;
		this.description = description;
		this.setStartDateTime(startDateTime);
		this.setEndDateTime(endDateTime);
		this.location = location;
		this.locationType = locationType;
		this.locationName = locationName;
		this.status = status;
	}

	public Meeting(int leaderId, String title, String description, Date startDateTime,
			Date endDateTime, LocationType locationType, MeetingStatus status) {
		this.leaderId = leaderId;
		this.title = title;
		this.description = description;
		this.setStartDateTime(startDateTime);
		this.setEndDateTime(endDateTime);
		this.locationType = locationType;
		this.status = status;
	}

	public Meeting(String title, String description, MapCoordinate location,
			LocationType locationType, String locationName, List<Participant> participants,
			MeetingStatus status) {
		this.title = title;
		this.description = description;
		this.location = location;
		this.locationType = locationType;
		this.locationName = locationName;
		this.participants = participants;
		this.status = status;
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

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
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

	public final Set<MapCoordinate> getUserPreferredLocations() {
		return userPreferredLocations;
	}

	public final void setUserPreferredLocations(Set<MapCoordinate> userPreferredLocations) {
		this.userPreferredLocations = userPreferredLocations;
	}

	public final void addUserPreferredLocation(MapCoordinate userPreferredLocation) {
		this.userPreferredLocations.add(userPreferredLocation);
	}

	public final void removeUserPreferredLocation(MapCoordinate userPreferredLocation) {
		this.userPreferredLocations.remove(userPreferredLocation);
	}

	public List<MapCoordinate> getRecommendedLocations() {
		return recommendedLocations;
	}

	public void setRecommendedLocations(List<MapCoordinate> recommendedLocations) {
		this.recommendedLocations = recommendedLocations;
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
