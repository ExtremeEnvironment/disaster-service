package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.client.Conversation;
import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.client.UserHolder;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import de.extremeenvironment.disasterservice.service.ActionService;
import de.extremeenvironment.disasterservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * REST controller for managing Action.
 */
@RestController
@RequestMapping("/api")
public class ActionResource {

    private final Logger log = LoggerFactory.getLogger(ActionResource.class);

    @Inject
    private ActionRepository actionRepository;
    @Inject
    private  DisasterRepository disasterRepository;

    private MessageClient messageClient;

    @Autowired
    public ActionResource(ActionRepository actionRepositoryRepository,
                          DisasterRepository disasterRepository, MessageClient messageClient) {
        this.actionRepository = actionRepositoryRepository;
        this.disasterRepository = disasterRepository;
        this.messageClient=messageClient;
    }

    /**
     * POST  /actions : Create a new action.
     *
     * @param action the action to create
     * @return the ResponseEntity with status 201 (Created) and with body the new action, or with status 400 (Bad Request) if the action has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/actions",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Action> createAction(@Valid @RequestBody Action action) throws URISyntaxException {
        log.debug("REST request to save Action : {}", action);
        if (action.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("action", "idexists", "A new action cannot already have an ID")).body(null);
        }
        if((action.getDisaster() == null) && (action.getActionType()!= ActionType.OFFER)) {
            if (getDisasterForAction(action) == null) {

                Disaster disaster = new Disaster();
                disaster.setLat(action.getLat());
                disaster.setLon(action.getLon());
                action.setDisaster(disaster);
                disasterRepository.saveAndFlush(disaster);


            } else {
                action.setDisaster(getDisasterForAction(action));
            }
        }

       action = matchActions(action);

        Action result = actionRepository.saveAndFlush(action);
        return ResponseEntity.created(new URI("/api/actions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("action", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /actions : Updates an existing action.
     *
     * @param action the action to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated action,
     * or with status 400 (Bad Request) if the action is not valid,
     * or with status 500 (Internal Server Error) if the action couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/actions",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Action> updateAction(@Valid @RequestBody Action action) throws URISyntaxException {
        log.debug("REST request to update Action : {}", action);
        if (action.getId() == null) {
            return createAction(action);
        }

        action = matchActions(action);


        Action result = actionRepository.save(action);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("action", action.getId().toString()))
            .body(result);
    }

    /**
     * GET  /actions : get all the actions.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of actions in body
     */
    @RequestMapping(value = "/actions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getAllActions() {
        log.debug("REST request to get all Actions");
        List<Action> actions = actionRepository.findAllWithEagerRelationships();
        return actions;
    }

    /**
     * GET  /actions/:id : get the "id" action.
     *
     * @param id the id of the action to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the action, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/actions/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Action> getAction(@PathVariable Long id) {
        log.debug("REST request to get Action : {}", id);
        Action action = actionRepository.findOneWithEagerRelationships(id);
        return Optional.ofNullable(action)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /actions/:id : delete the "id" action.
     *
     * @param id the id of the action to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/actions/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteAction(@PathVariable Long id) {
        log.debug("REST request to delete Action : {}", id);

        rejectMatch(actionRepository.getOne(id), true);

        actionRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("action", id.toString())).build();
    }


    @RequestMapping(value = "/action/{userId}/{actionType}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getActionByActionType(@PathVariable Long userId, @PathVariable ActionType actionType) {
        return actionRepository.findByActionType(userId,actionType);

    }

    /**
     * @param action
     * @return the nearest disaster of an action location, in a radius of 15000km
     */
    public Disaster getDisasterForAction(Action action) {
        float distance = 15000;
        Disaster disasterReturn = null;
        float lon = action.getLon();
        float lat = action.getLat();

        List<Disaster> disasterList = disasterRepository.findAll();

        for (int i = 0; i < disasterList.size(); i++) {
            Disaster disaster = disasterList.get(i);
            Float disasterLon = disaster.getLon();
            Float disasterLat = disaster.getLat();
            float distanceBetween = getDistance(lat, lon, disasterLat, disasterLon);
            if (distanceBetween < 15000) {
                if (distanceBetween < distance) {
                    distance = distanceBetween;
                    disasterReturn = disaster;
                }
            }

        }
        return disasterReturn;
    }



    /**
     * checks wether a match is available for a specific action
     * commented lines for later removal of already rejected actions
     *
     * @param a the action for which a match shall be found
     */
    public Action matchActions(Action a) {
//        System.out.println("\n\n### matching begin ###");

        if (a.getMatch() != null || a.getActionType() == ActionType.KNOWLEDGE) {
            return a;
        }

        Set<Action> possibleMatches = new HashSet<>(actionRepository.findAll());

        possibleMatches.remove(a);
        possibleMatches.removeAll(a.getRejectedMatches());


        Action bestMatch = null;
        Float bestMatchDist = Float.MAX_VALUE;

//        System.out.println("### Matching prior For ###");

        for (Action act : possibleMatches) {
            if (act.getMatch() != null) {
                continue;
            }

            HashSet actionObjectIntersect = new HashSet<>(a.getActionObjects());
            actionObjectIntersect.retainAll(act.getActionObjects());

            Float matchDist = getDistance(a.getLat(), a.getLon(), act.getLat(), act.getLon(), (a.getActionType().equals(ActionType.OFFER) ? a.getCreatedDate() : act.getCreatedDate()));

//            System.out.println("### " + act.getId() + " " + matchDist + " ###");

            if (!actionObjectIntersect.isEmpty() && matchDist <= 100_000.0 && matchDist < bestMatchDist && a.getActionType() != act.getActionType()) { //check if a is in act's rejectedMatches shouldnt be necessary
                bestMatchDist = matchDist;
                bestMatch = act;
            }
        }

        a.setMatch(bestMatch);
        actionRepository.save(a);

        if (bestMatch != null) {
            bestMatch.setMatch(a);
            actionRepository.save(bestMatch);


            Conversation savedConversation = messageClient.addConversation(
                new Conversation(true, bestMatch.getDisaster().getTitle() + " Conversation")
            );
            messageClient.addMember(new UserHolder(bestMatch.getUser().getUserId()), savedConversation.getId());
            messageClient.addMember(new UserHolder(a.getUser().getUserId()), savedConversation.getId());
        }

//        System.out.println("### match " + bestMatch + "###");

        return a;

    }

    @RequestMapping(value = "/actions/{id}/likes",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Action> updateLikes(@PathVariable Long id) throws URISyntaxException {

        if (!actionRepository.findActionById(id).isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }

        Action action = actionRepository.findActionById(id).get();

        log.debug("REST request to update Action : {}", action);
        action.setLikeCounter(action.getLikeCounter() + 1);
        actionRepository.save(action);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("action", action.getId().toString()))
            .body(action);
    }

    @RequestMapping(value = "/actions/{id}/topTenKnowledge",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getTopTenKnowledge(@PathVariable Long id) {


        Disaster disaster;
        if ((disaster = disasterRepository.findById(id).get()) == null) {
            return null;
        }

        List<Action> actions = actionRepository.findActionByActionType(ActionType.KNOWLEDGE);

        List<Action> result = new ArrayList<>();

        for (Action action : actions) {
            if (action.getDisaster().getId() == disaster.getId()) {
                result.add(action);
            }
        }


        if (result.size() <= 10) {
            return result;
        } else {
            Collections.sort(result, new Comparator<Action>() {
                @Override
                public int compare(Action o1, Action o2) {
                    if (o1.getLikeCounter() > o2.getLikeCounter()) {
                        return 1;
                    } else if (o1.getLikeCounter() == o2.getLikeCounter()) {
                        return 0;
                    } else {
                        return -1;
                    }

                }
            });
        }
        return result.subList(0, 9);
    }

    @RequestMapping(value = "/actions/{id}/knowledge",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getActionKnowledgeByCatastrophe(@Valid @PathVariable("id") Long id) {

        Disaster disaster;
        if ((disaster = disasterRepository.findById(id).get()) == null) {
            return null;
        } else {

            List<Action> actions = actionRepository.findActionByActionType(ActionType.KNOWLEDGE);
            List<Action> result = new ArrayList<>();
            for (Action a : actions) {
                if (a.getDisaster().getId() == disaster.getId()) {
                    result.add(a);
                }

            }
            return result;

        }


    }


    /**
     * removes a match from actions
     * commented lines for later removal of already rejected actions
     *
     * @param a the action the match shall be removed from
     */
    public void rejectMatch(Action a, boolean priorToDeletion) {
        if (a.getMatch() == null) {
            return;
        }

        a.getMatch().addRejectedMatch(a);
        Action otherAction = a.getMatch();
        a.getMatch().setMatch(null);
        actionRepository.save(a.getMatch());

        a.addRejectedMatch(a.getMatch());
        a.setMatch(null);
        actionRepository.save(a);

        matchActions(otherAction);

        if (priorToDeletion) {
            return;
        }

        matchActions(a);
    }

    public static Float getDistance(float lat1, float lon1, float lat2, float lon2, ZonedDateTime seekDate) {
        Duration d = Duration.between(seekDate, ZonedDateTime.now());
        long waitingDuration = d.getSeconds();

        final float BONUS = 1 / (60 * 60 * 24); // 1 km per day waited
        //TODO set bonus via web interface

        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist - (waitingDuration * BONUS);
    }


    public static Float getDistance(float lat1, float lon1, float lat2, float lon2) {
        return getDistance(lat1, lon1, lat2, lon2, ZonedDateTime.now());
    }


}
