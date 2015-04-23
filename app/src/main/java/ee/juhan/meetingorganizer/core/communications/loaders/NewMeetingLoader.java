package ee.juhan.meetingorganizer.core.communications.loaders;

import ee.juhan.meetingorganizer.core.communications.Constants;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;
import ee.juhan.meetingorganizer.models.server.ServerResult;

public abstract class NewMeetingLoader extends GenericPostLoader<ServerResult> {

    public NewMeetingLoader(MeetingDTO post, String sid) {
        super(ServerResult.class, post, Constants.NEW_MEETING, "sid=" + sid);
    }
}