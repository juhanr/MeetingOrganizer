package ee.juhan.meetingorganizer.core.communications.loaders;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import ee.juhan.meetingorganizer.core.communications.Constants;
import ee.juhan.meetingorganizer.models.server.MeetingDTO;

public abstract class PastMeetingsLoader extends GenericLoader<List<MeetingDTO>> {

    public PastMeetingsLoader(int accountId, String sid) {
        super(new TypeToken<ArrayList<MeetingDTO>>() {
        }.getType(), Constants.PAST_MEETINGS, "sid=" + sid);
        addParameter("accountId", String.valueOf(accountId));
        addParameter("clientTimeZone", TimeZone.getDefault().getID());
    }
}