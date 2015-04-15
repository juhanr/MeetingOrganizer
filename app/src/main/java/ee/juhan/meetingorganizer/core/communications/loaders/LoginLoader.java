package ee.juhan.meetingorganizer.core.communications.loaders;

import ee.juhan.meetingorganizer.core.communications.Constants;
import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;

public abstract class LoginLoader extends GenericPostLoader<ServerResponse> {

    public LoginLoader(AccountDTO post) {
        super(ServerResponse.class, post, Constants.LOGIN);
    }
}
