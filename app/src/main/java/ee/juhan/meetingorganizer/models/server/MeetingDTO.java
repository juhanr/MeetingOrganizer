package ee.juhan.meetingorganizer.models.server;

import java.util.ArrayList;
import java.util.Date;

public class MeetingDTO {

    private int id;
    private int leaderId;
    private String title;
    private String description;
    private Date startDateTime;
    private Date endDateTime;
    private MapCoordinate location;
    private LocationType locationType;
    private ArrayList<ParticipantDTO> participants = new ArrayList<>();

    public MeetingDTO() {

    }

    public MeetingDTO(int id, int leaderId, String title, String description,
                      Date startDateTime, Date endDateTime, MapCoordinate location,
                      LocationType locationType) {
        this.id = id;
        this.leaderId = leaderId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
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

    public MeetingDTO(String title, String description, MapCoordinate location,
                      LocationType locationType, ArrayList<ParticipantDTO> participants) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.locationType = locationType;
        this.participants = participants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public MapCoordinate getLocation() {
        return location;
    }

    public void setLocation(MapCoordinate location) {
        this.location = location;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public ArrayList<ParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<ParticipantDTO> participants) {
        this.participants = participants;
    }

    public boolean addParticipant(ParticipantDTO participant) {
        return participants.add(participant);
    }

}
