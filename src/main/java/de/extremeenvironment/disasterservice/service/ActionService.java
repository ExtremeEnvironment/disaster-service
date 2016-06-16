package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.DisasterTypeRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
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


     //   action.setUser(userRepository.findOneById(user).get());

        if (actionType == ActionType.OFFER || actionType == ActionType.SEEK) {
            action.setActionObjects(actionObjects);
        }



        actionRepository.save(action);

//        matchActions(action);

        return action;

    }

    public Action updateAction(Long actionId, Set<ActionObject> actionObjects) {
        Optional<Action> action = actionRepository.findActionById(actionId);
        action.get().setActionObjects(actionObjects);

        actionRepository.save(action.get());

//        matchActions(action.get());

        return action.get();

    }

    public List<Action> getAllActionObjectsByUserId(Long userId) {
        return actionRepository.findNotExpiredActionsByUser(userRepository.findOne(userId));
    }




    /**
     * checks wether a match is available for a specific action
     * commented lines for later removal of already rejected actions
     *
     * @param a the action for which a match shall be found
     */
    public void matchActions(Action a) {
        if (!a.getMatch().equals(null) || a.getActionType() == ActionType.KNOWLEDGE) {
            return;
        }

        Set<Action> possibleMatches = new HashSet<>(actionRepository.findAll());

        possibleMatches.remove(a);
//        possibleMatches.removeAll(a.getRejectedMatches());


        Action bestMatch = null;
        Float bestMatchDist = Float.MAX_VALUE;

        for (Action act : possibleMatches) {
            if (!act.getMatch().equals(null) || !a.getDisaster().equals(act.getDisaster())) {
                continue;
            }

            HashSet actionObjectIntersect = new HashSet<>(a.getActionObjects());
            actionObjectIntersect.retainAll(act.getActionObjects());

            Float matchDist = getDistance(a.getLat(), a.getLon(), act.getLat(), act.getLon());

            if (!actionObjectIntersect.isEmpty() && matchDist < bestMatchDist && a.getActionType() != act.getActionType()) { //check if a is in act's rejectedMatches shouldnt be necessary
                bestMatchDist = matchDist;
                bestMatch = act;
            }
        }

        a.setMatch(bestMatch);
        actionRepository.save(a);

        if (bestMatch != null) {
            bestMatch.setMatch(a);
            actionRepository.save(bestMatch);
        }


    }

    /**
     * removes a match from actions
     * commented lines for later removal of already rejected actions
     *
     * @param a the action the match shall be removed from
     */
    public void rejectMatch(Action a) {
//        a.getMatch().addRejectedMatch(a);
        a.getMatch().setMatch(null);
        actionRepository.save(a.getMatch());

//        a.addRejectedMatch(a.getMatch());
        a.setMatch(null);
        actionRepository.save(a);
    }


    public static Float getDistance(float lat1, float lon1, float lat2, float lon2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}
