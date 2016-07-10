package de.extremeenvironment.disasterservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by on 10.07.16.
 *
 * @author David Steiman
 */
@Component
public class TestMockMessageClient implements MessageClient {

    private Long conversationCounter = 1L;

    private Long userHolderCounter = 1L;



    @Override
    public Conversation addConversation(@RequestBody Conversation conversation) {
        return new Conversation(conversationCounter++, true, "test mocked conversation");
    }

    @Override
    public UserHolder addMember(@RequestBody UserHolder user, @PathVariable("conversationId") Long conversationId) {
        return new UserHolder(userHolderCounter++);
    }
}
