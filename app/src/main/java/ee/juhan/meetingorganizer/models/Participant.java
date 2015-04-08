package ee.juhan.meetingorganizer.models;

public class Participant {
    private String name;
    private long phoneNumber;
    private boolean hasResponded;

    public Participant(String name, long phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public Participant(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean hasResponded() {
        return hasResponded;
    }

    public void setHasResponded(boolean hasResponded) {
        this.hasResponded = hasResponded;
    }
}
