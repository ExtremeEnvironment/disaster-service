package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.DisasterTypeRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by over on 08.06.2016.
 */
@Service
public class ActionService {

    private static final int MAX_TRIES = 5;
    ActionRepository actionRepository;

    UserRepository userRepository;

    DisasterRepository disasterRepository;

    DisasterTypeRepository disasterTypeRepository;

    MessageClient messageClient;

    @Inject
    public ActionService(ActionRepository actionRepository, UserRepository userRepository,
     DisasterRepository disasterRepository, DisasterTypeRepository disasterTypeRepository, MessageClient messageClient) {
        this.actionRepository = actionRepository;
        this.userRepository = userRepository;
        this.disasterRepository = disasterRepository;
        this.disasterTypeRepository = disasterTypeRepository;
        this.messageClient = messageClient;
    }

    public Action save(Action action) {
        Disaster disaster;
        Action result = actionRepository.save(action);

        int counter = 0;

        if (action.getDisaster() != null
            && action.getDisaster().getId() != null
            && (disaster = disasterRepository.findOne(action.getDisaster().getId())) != null) {
            while (counter++ < MAX_TRIES) {
                try {
                    messageClient.addMember(action.getUser(), disaster.getConversationId());
                } catch (Exception e) {
                    if (counter + 1 == MAX_TRIES) {
                        actionRepository.delete(action);
                        actionRepository.flush();
                        throw e;
                    }
                }
            }
        }

        return result;
    }

    public Action updateAction(Long actionId, Set<ActionObject> actionObjects) {
        Optional<Action> action = actionRepository.findActionById(actionId);
        action.get().setActionObjects(actionObjects);

        actionRepository.save(action.get());


        return action.get();

    }

    public List<Action> getAllActionObjectsByUserId(Long userId) {
        return actionRepository.findNotExpiredActionsByUser(userRepository.findOne(userId));
    }


    public List<Action> findAll() {
        return actionRepository.findAll();
    }


}
