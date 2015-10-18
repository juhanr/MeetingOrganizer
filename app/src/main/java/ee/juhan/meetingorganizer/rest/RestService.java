package ee.juhan.meetingorganizer.rest;

import java.util.List;

import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ContactDTO;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ParticipantDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface RestService {

	String MEETING = "/meeting";
	String NEW_MEETING = MEETING + "/new";
	String UPDATE_PARTICIPANT = "/update-participant";

	String REGISTER = "/register";
	String LOGIN = "/login";

	String ACCOUNT = "/account";
	String CHECK_CONTACTS = "/check-contacts";

	String ACCOUNT_ID = "accountId";
	String MEETING_ID = "meetingId";
	String MEETINGS_TYPE = "meetingsType";

	@POST(LOGIN)
	void loginRequest(@Body AccountDTO accountDTO, Callback<ServerResponse> callback);

	@POST(REGISTER)
	void registrationRequest(@Body AccountDTO accountDTO, Callback<ServerResponse> callback);

	@POST(ACCOUNT + "/{" + ACCOUNT_ID + "}" + CHECK_CONTACTS)
	void checkContactsRequest(@Body List<ContactDTO> contactList, @Path(ACCOUNT_ID) int accountId,
			Callback<List<ContactDTO>> callback);

	@POST(NEW_MEETING)
	void newMeetingRequest(@Body MeetingDTO meetingDTO, Callback<MeetingDTO> callback);

	@GET(MEETING + "/{" + MEETINGS_TYPE + "}" + ACCOUNT + "/{" + ACCOUNT_ID + "}")
	void getMeetingsRequest(@Path(MEETINGS_TYPE) String listType, @Path(ACCOUNT_ID) int accountId,
			Callback<List<MeetingDTO>> callback);

	@POST(MEETING + "/{" + MEETING_ID + "}" + UPDATE_PARTICIPANT)
	void updateParticipantRequest(@Body ParticipantDTO participantDTO,
			@Path(MEETING_ID) int meetingId, Callback<MeetingDTO> callback);

}