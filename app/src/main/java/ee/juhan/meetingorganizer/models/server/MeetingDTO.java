package ee.juhan.meetingorganizer.models.server;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MeetingDTO {

    private int leaderId;
    private String title;
    private String description;
    private Date startDateTime;
    private Date endDateTime;
    private double locationLatitude;
    private double locationLongitude;
    private LocationType locationType;
    private Set<ParticipantDTO> participants = new HashSet<>();

    public MeetingDTO() {

    }

    public MeetingDTO(int leaderId, String title, String description, Date startDateTime, Date endDateTime,
                      double locationLatitude, double locationLongitude, LocationType locationType) {
        this.leaderId = leaderId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.locationType = locationType;
    }

    public MeetingDTO(int leaderId, String title, String description, Date startDateTime,
                      Date endDateTime, LocationType locationType) {
        this.leaderId = leaderId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.locationType = locationType;
    }

    public MeetingDTO(String title, String description, double locationLatitude,
                      double locationLongitude, LocationType locationType,
                      Set<ParticipantDTO> participants) {
        this.title = title;
        this.description = description;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.locationType = locationType;
        this.participants = participants;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startTime) {
        this.startDateTime = startTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endTime) {
        this.endDateTime = endTime;
    }

    public double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public Set<ParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<ParticipantDTO> participants) {
        this.participants = participants;
    }

    public boolean addParticipant(ParticipantDTO participant) {
        return participants.add(participant);
    }

}
