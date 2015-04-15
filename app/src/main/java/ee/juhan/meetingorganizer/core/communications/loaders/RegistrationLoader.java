package ee.juhan.meetingorganizer.core.communications.loaders;

import ee.juhan.meetingorganizer.core.communications.Constants;
import ee.juhan.meetingorganizer.models.server.AccountDTO;
import ee.juhan.meetingorganizer.models.server.ServerResponse;

public abstract class RegistrationLoader extends GenericPostLoader<ServerResponse> {

    public RegistrationLoader(AccountDTO post) {
        super(ServerResponse.class, post, Constants.REGISTER);
    }

}
