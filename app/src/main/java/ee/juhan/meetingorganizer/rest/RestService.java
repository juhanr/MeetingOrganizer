package ee.juhan.meetingorganizer.rest;

import java.util.List;

import ee.juhan.meetingorganizer.models.server.Account;
import ee.juhan.meetingorganizer.models.server.Contact;
import ee.juhan.meetingorganizer.models.server.Meeting;
import ee.juhan.meetingorganizer.models.server.Participant;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface RestService {

	String MEETING = "/meeting";
	String NEW_MEETING = MEETING + "/new";
	String UPDATE_PARTICIPATION_ANSWER_PATH = "/update-participation-answer";
	String UPDATE_PARTICIPANT_LOCATION_PATH = "/update-participant-location";
	String GENERATE_RECOMMENDED_LOCATIONS_PATH = "/generate-recommended-locations";

	String REGISTER = "/register";
	String LOGIN = "/login";

	String ACCOUNT = "/account";
	String CHECK_CONTACTS = "/check-contacts";

	String ACCOUNT_ID = "accountId";
	String MEETING_ID = "meetingId";
	String MEETINGS_TYPE = "meetingsType";

	@POST(LOGIN)
	void loginRequest(@Body Account account, Callback<ServerResponse> callback);

	@POST(REGISTER)
	void registrationRequest(@Body Account account, Callback<ServerResponse> callback);

	@POST(ACCOUNT + "/{" + ACCOUNT_ID + "}" + CHECK_CONTACTS)
	void checkContactsRequest(@Body List<Contact> contactList, @Path(ACCOUNT_ID) int accountId,
			Callback<List<Contact>> callback);

	@POST(NEW_MEETING)
	void newMeetingRequest(@Body Meeting meeting, Callback<Meeting> callback);

	@GET(MEETING + "/{" + MEETINGS_TYPE + "}" + ACCOUNT + "/{" + ACCOUNT_ID + "}")
	void getMeetingsRequest(@Path(MEETINGS_TYPE) String listType, @Path(ACCOUNT_ID) int accountId,
			Callback<List<Meeting>> callback);

	@POST(MEETING + "/{" + MEETING_ID + "}" + UPDATE_PARTICIPATION_ANSWER_PATH)
	void updateParticipationAnswerRequest(@Body Participant participant,
			@Path(MEETING_ID) int meetingId, Callback<Meeting> callback);

	@POST(MEETING + "/{" + MEETING_ID + "}" + UPDATE_PARTICIPANT_LOCATION_PATH)
	void updateParticipantLocationRequest(@Body Participant participant,
			@Path(MEETING_ID) int meetingId, Callback<Meeting> callback);

	@POST(MEETING + "/{" + MEETING_ID + "}" + GENERATE_RECOMMENDED_LOCATIONS_PATH)
	void generateRecommendedLocationsRequest(@Path(MEETING_ID) int meetingId,
			Callback<Meeting> callback);

}