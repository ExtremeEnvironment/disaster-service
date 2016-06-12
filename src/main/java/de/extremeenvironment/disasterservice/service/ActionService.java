package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.DisasterTypeRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by over on 08.06.2016.
 */
@Service
public class ActionService {

    @Inject
    ActionRepository actionRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    DisasterRepository disasterRepository;

    @Inject
    DisasterTypeRepository disasterTypeRepository;

    public Action createAction(Float lat, Float lon, ActionType actionType, Long user, Set<ActionObject> actionObjects, Long disasterType) {
        Action action = new Action();
        action.setLat(lat);
        action.setLon(lon);
        action.setActionType(actionType);


        action.setUser(userRepository.findOneById(user).get());

        if(actionType == ActionType.OFFER || actionType== ActionType.SEEK) {
            action.setActionObjects(actionObjects);
        }



        actionRepository.save(action);

        return action;

    }

    public Action updateAction(Long actionId, Set<ActionObject> actionObjects) {
        Optional<Action> action= actionRepository.findActionById(actionId);
        action.get().setActionObjects(actionObjects);

        actionRepository.save(action.get());

        return action.get();

    }

    public List<Action> getAllActionObjectsByUserId(Long userId) {
        return actionRepository.findNotExpiredActionsByUser(userRepository.findOne(userId));
    }


}
