package ee.juhan.meetingorganizer.models.server;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

import ee.juhan.meetingorganizer.util.DateUtil;

public class Participant {

	private int id;
	private int accountId;
	private String name;
	private String email;
	private String phoneNumber;
	private ParticipationAnswer participationAnswer = ParticipationAnswer.NO_ANSWER;
	private SendGpsLocationAnswer sendGpsLocationAnswer = SendGpsLocationAnswer.NO_ANSWER;
	private MapCoordinate location;
	private Date locationTimestamp;

	// App-specific values, not used in server
	private boolean isUTCTimeZone = false;

	public Participant() {}

	public Participant(int accountId, ParticipationAnswer participationAnswer,
			SendGpsLocationAnswer sendGpsLocationAnswer, MapCoordinate location,
			Date locationTimestamp) {
		this.accountId = accountId;
		this.participationAnswer = participationAnswer;
		this.sendGpsLocationAnswer = sendGpsLocationAnswer;
		this.location = location;
		this.setLocationTimestamp(locationTimestamp);
	}

	public Participant(int accountId, String name, String email, String phoneNumber) {
		this.accountId = accountId;
		this.name = name;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	public Participant(int accountId, MapCoordinate location, Date locationTimestamp) {
		this.accountId = accountId;
		this.location = location;
		this.setLocationTimestamp(locationTimestamp);
	}

	@Override
	public String toString() {
		return "Participant{" +
				"id=" + id +
				", accountId=" + accountId +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", participationAnswer=" + participationAnswer +
				", sendGpsLocationAnswer=" + sendGpsLocationAnswer +
				", location=" + location +
				", locationTimestamp=" + locationTimestamp +
				", isUTCTimeZone=" + isUTCTimeZone +
				'}';
	}

	public final int getId() {
		return id;
	}

	public final void setId(int id) {
		this.id = id;
	}

	public final int getAccountId() {
		return accountId;
	}

	public final void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getEmail() {
		return email;
	}

	public final void setEmail(String email) {
		this.email = email;
	}

	public final String getPhoneNumber() {
		return phoneNumber;
	}

	public final void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public final ParticipationAnswer getParticipationAnswer() {
		return participationAnswer;
	}

	public final void setParticipationAnswer(ParticipationAnswer participationAnswer) {
		this.participationAnswer = participationAnswer;
	}

	public SendGpsLocationAnswer getSendGpsLocationAnswer() {
		return sendGpsLocationAnswer;
	}

	public void setSendGpsLocationAnswer(SendGpsLocationAnswer sendGpsLocationAnswer) {
		this.sendGpsLocationAnswer = sendGpsLocationAnswer;
	}

	public final MapCoordinate getLocation() {
		return location;
	}

	public final void setLocation(MapCoordinate location) {
		this.location = location;
	}

	public Date getLocationTimestamp() {
		return isUTCTimeZone ? DateUtil.toLocalTimezone(locationTimestamp) : locationTimestamp;
	}

	public void setLocationTimestamp(Date locationTimestamp) {
		this.locationTimestamp = DateUtil.toUTCTimezone(locationTimestamp);
		this.isUTCTimeZone = true;
	}

	public String getLocationTimestampFormatted() {
		return DateUtil.isToday(getLocationTimestamp()) ?
				DateUtil.formatTime(getLocationTimestamp()) :
				DateUtil.formatDateTime(getLocationTimestamp());
	}

	public MarkerOptions getMarkerOptions() {
		return new MarkerOptions().position(location.toLatLng()).title(name)
				.snippet("Last updated: " + getLocationTimestampFormatted()).draggable(false)
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
	}
}