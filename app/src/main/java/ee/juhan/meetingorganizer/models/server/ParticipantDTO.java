package ee.juhan.meetingorganizer.models.server;

public class ParticipantDTO {

    private int id;
    private int accountId;
    private String name;
    private String email;
    private String phoneNumber;
    private ParticipationAnswer participationAnswer = ParticipationAnswer.NOT_ANSWERED;
    private MapCoordinate location;

    public ParticipantDTO() {
    }

    public ParticipantDTO(int id, int accountId, ParticipationAnswer participationAnswer,
                          MapCoordinate location) {
        this.accountId = accountId;
        this.participationAnswer = participationAnswer;
        this.location = location;
    }

    public ParticipantDTO(int accountId, ParticipationAnswer participationAnswer) {
        this.accountId = accountId;
        this.participationAnswer = participationAnswer;
    }

    public ParticipantDTO(int accountId, String name, String email, String phoneNumber) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public ParticipantDTO(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ParticipationAnswer getParticipationAnswer() {
        return participationAnswer;
    }

    public void setParticipationAnswer(ParticipationAnswer participationAnswer) {
        this.participationAnswer = participationAnswer;
    }

    public MapCoordinate getLocation() {
        return location;
    }

    public void setLocation(MapCoordinate location) {
        this.location = location;
    }

}