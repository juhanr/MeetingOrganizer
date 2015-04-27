package ee.juhan.meetingorganizer.rest;

import java.util.List;

import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ContactDTO;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface RestService {

    static final public String MEETING = "/meeting";
    static final public String NEW_MEETING = MEETING + "/new";

    static final public String REGISTER = "/register";
    static final public String LOGIN = "/login";

    static final public String ACCOUNT = "/account";
    static final public String CHECK_CONTACTS = "/check-contacts";

    @POST(LOGIN)
    void loginRequest(@Body AccountDTO accountDTO,
                      Callback<ServerResponse> callback);

    @POST(REGISTER)
    void registrationRequest(@Body AccountDTO accountDTO,
                             Callback<ServerResponse> callback);

    @POST(ACCOUNT + "/{id}" + CHECK_CONTACTS)
    void checkContactsRequest(@Body List<ContactDTO> contactList,
                              @Path("id") int accountId,
                              Callback<List<ContactDTO>> callback);

    @POST(NEW_MEETING)
    void newMeetingRequest(@Body MeetingDTO meetingDTO,
                           Callback<MeetingDTO> callback);

    @GET(MEETING + "/{meetingsType}/{id}")
    void getMeetingsRequest(@Path("meetingsType") String listType,
                            @Path("id") int accountId,
                            Callback<List<MeetingDTO>> callback);


}