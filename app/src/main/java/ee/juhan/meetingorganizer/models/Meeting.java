package ee.juhan.meetingorganizer.models;

import com.google.android.gms.maps.model.LatLng;

public class Meeting {
    private String title;
    private Date date;
    private Time startTime;
    private Time endTime;
    private String message;
    private Participant[] participants;
    private double locationLatitude;
    private double locationLongitude;

    public Meeting(String title, Date date, Time startTime, Time endTime, String message,
                   Participant[] participants, double locationLatitude, double locationLongitude) {
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message;
        this.participants = participants;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Participant[] getParticipants() {
        return participants;
    }

    public void setParticipants(Participant[] participants) {
        this.participants = participants;
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

    public LatLng getLocation() {
        return new LatLng(locationLatitude, locationLongitude);
    }
}
