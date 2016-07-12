package de.extremeenvironment.disasterservice.client;

import de.extremeenvironment.disasterservice.domain.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by Jonathan on 30.06.2016.
 */
@FeignClient("http://messageservice/api")
public interface MessageClient {


    @RequestMapping(method = RequestMethod.GET, value = "/conversation")
    List<Conversation> getConversations();

    @RequestMapping(method = RequestMethod.POST, value = "/conversations")
    Conversation addConversation(@RequestBody Conversation conversation);

    @RequestMapping(method = RequestMethod.POST, value = "/conversations/{conversationId}/member")
    User addMember(@RequestBody User user, @PathVariable("conversationId") Long conversationId);

}
