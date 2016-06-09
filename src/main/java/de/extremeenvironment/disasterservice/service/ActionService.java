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

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

/**
 * Created by over on 08.06.2016.
 */
public class ActionService {

    @Inject
    ActionRepository actionRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    DisasterRepository disasterRepository;

    @Inject
    DisasterTypeRepository disasterTypeRepository;

    public Action createAction(Float lat, Float lon, ActionType actionType, Long user, Set<ActionObject> actionObjects, Long disterType) {
        Action action = new Action();
        action.setLat(lat);
        action.setLon(lon);
        action.setActionType(actionType);


        action.setUser(userRepository.findOneById(user).get());

        if(actionType == ActionType.OFFER || actionType== ActionType.SEEK) {
            action.setActionObjects(actionObjects);
        }

        action.setDisasterType(disasterTypeRepository.findById(disterType).get());

        actionRepository.save(action);

        return action;

    }

    public Action updateAction(Long actionId, Set<ActionObject> actionObjects) {
        Optional<Action> action= actionRepository.findActionById(actionId);
        action.get().setActionObjects(actionObjects);


        //save oder einfach so lassen?
         // actionRepository.save()

        return action.get();

    }
}
