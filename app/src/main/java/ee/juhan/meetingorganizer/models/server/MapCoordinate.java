package ee.juhan.meetingorganizer.models.server;

public class MapCoordinate {

    private Double latitude;
    private Double longitude;

    public MapCoordinate() {

    }

    public MapCoordinate(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

}
