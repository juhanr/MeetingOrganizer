package ee.juhan.meetingorganizer.core.communications.loaders;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ee.juhan.meetingorganizer.core.communications.Constants;
import ee.juhan.meetingorganizer.models.server.ContactDTO;

public abstract class CheckContactsLoader extends GenericPostLoader<List<ContactDTO>> {

    public CheckContactsLoader(int accountId, List<ContactDTO> post, String sid) {
        super(new TypeToken<ArrayList<ContactDTO>>() {
        }.getType(), post.toArray(), Constants.CHECK_CONTACTS, "sid=" + sid);
        addParameter("accountId", String.valueOf(accountId));
    }
}