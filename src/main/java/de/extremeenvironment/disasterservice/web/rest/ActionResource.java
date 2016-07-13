package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.client.Conversation;
import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.client.UserService;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.service.ActionService;
import de.extremeenvironment.disasterservice.service.DisasterService;
import de.extremeenvironment.disasterservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for managing Action.
 */
@RestController
@RequestMapping("/api")
public class ActionResource {

    private final Logger log = LoggerFactory.getLogger(ActionResource.class);

    private ActionRepository actionRepository;

    private ActionService actionService;

    private DisasterRepository disasterRepository;

    private MessageClient messageClient;

    private DisasterService disasterService;

    private UserService userService;

    @Inject
    public ActionResource(ActionRepository actionRepository, DisasterRepository disasterRepository,
                          MessageClient messageClient, DisasterService disasterService, UserService userService,
                          ActionService actionService) {

        this.actionRepository = actionRepository;
        this.disasterRepository = disasterRepository;
        this.messageClient = messageClient;
        this.disasterService = disasterService;
        this.userService = userService;
        this.actionService = actionService;
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
    public ResponseEntity<Action> createAction(@Valid @RequestBody Action action, Principal principal) throws URISyntaxException {

        log.debug("REST request to save Action : {}", action);
        if (action.getId() != null) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert("action", "idexists", "A new action cannot already have an ID")).body(null);
        }
        if ((action.getDisaster() == null) && (action.getActionType() != ActionType.OFFER)) {
            if (disasterService.getDisasterForAction(action) == null) {

                Disaster disaster = new Disaster();
                disaster.setLat(action.getLat());
                disaster.setLon(action.getLon());
                action.setDisaster(disaster);
                disasterRepository.saveAndFlush(disaster);


            } else {
                action.setDisaster(disasterService.getDisasterForAction(action));
            }
        }


        OAuth2Request request = ((OAuth2Authentication) principal).getOAuth2Request();
/*
        if (request.getScope().contains("web-app")) {
            throw new IllegalArgumentException("no valid user can be present. preventing NullPointerException");
        }*/
        User user = userService.findOrCreateByName(principal.getName());
        action.setUser(user);

        action = matchActions(action);
        //Action result = actionRepository.saveAndFlush(action);
        Action result = actionService.save(action);
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
    public ResponseEntity<Action> updateAction(@Valid @RequestBody Action action, Principal principal) throws URISyntaxException {

        log.debug("REST request to update Action : {}", action);
        if (action.getId() == null) {
            return createAction(action, principal);
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

//        rejectMatch(actionRepository.getOne(id), true);

        actionRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("action", id.toString())).build();
    }


    /**
     * GET /actions/:userId/:actionType : get all actions created by "userId" with the specific "actoinType"
     *
     * @param userId     the userId of the user from which the actions shall be returned
     * @param actionType the @see{ActionType} of the Actions which shall be returned
     * @return the ResponseEntity with status 200 (OK) and the list of actions in body
     */
    @RequestMapping(value = "/action/{userId}/{actionType}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getActionByActionType(@PathVariable Long userId, @PathVariable ActionType actionType) {
        return actionRepository.findByActionType(userId, actionType);

    }


    /**
     * PUT /actions/:id/likes : increments the "id" like-counter by one
     *
     * @param id the id of the action
     * @return ResponseEntity with status 200 (OK) and the action in the body or with 400 (Bad Request)
     * @throws URISyntaxException
     */
    @RequestMapping(value = "/actions/{id}/likes",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Action> updateLikes(@PathVariable Long id) throws URISyntaxException {

        if (!actionRepository.findActionById(id).isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }

        return actionRepository.findActionById(id)
            .map(action -> {
                log.debug("REST request to update Action : {}", action);
                action.setLikeCounter(action.getLikeCounter() + 1);
                actionRepository.save(action);

                return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert("action", action.getId().toString()))
                    .body(action);
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * GET /actions/:disasterId/knowledge : lists all actions of type knowledge from a "disasterId"
     *
     * @param id the disasterId
     * @return ResponseEntity with status 200 (OK) and the list of actions or with 404 (Not Found)
     */
    @RequestMapping(value = "/actions/{id}/knowledge",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getActionKnowledgeByCatastrophe(@Valid @PathVariable("id") Long id) {
        return disasterRepository.findById(id)
            .map(disaster -> actionRepository.findActionByActionType(ActionType.KNOWLEDGE).stream()
                .filter(a -> a.getDisaster().getId().equals(disaster.getId()))
                .collect(Collectors.toList()))
            .orElseGet(() -> null);
    }


    /**
     * GET /actions/:disasterId/topTenKnowledge : lists the ten knowledges with the most likes of "disasterId"
     *
     * @param id the disasterId of the wanted request
     * @return ResponseEntity with status 200 (OK) and the list of actions or with 404 (Not Found)
     */
    @RequestMapping(value = "/actions/{id}/topTenKnowledge",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getTopTenKnowledge(@PathVariable Long id) {
        return disasterRepository.findById(id)
            .map(disaster -> {
                List<Action> actions = actionRepository.findActionByActionType(ActionType.KNOWLEDGE);

                List<Action> result = actions.stream()
                    .filter(action -> action.getDisaster().getId().equals(disaster.getId()))
                    .collect(Collectors.toList());

                if (result.size() <= 10) {
                    return result;
                } else {
                    Collections.sort(result, (o1, o2) -> {
                        if (o1.getLikeCounter() > o2.getLikeCounter()) {
                            return 1;
                        } else if (o1.getLikeCounter().equals(o2.getLikeCounter())) {
                            return 0;
                        } else {
                            return -1;
                        }
                    });
                }
                return result.subList(0, 9);
            })
            .orElseGet(() -> null);
    }

    /**
     * PUT /actions/:id/rejectMatch the "id" of the action for which the current match shall be released
     *
     * @param id the id of the action
     * @return the ResponseEntity with status 200 (OK) and the Action or with 400 (Bad Request)
     * @throws URISyntaxException
     */
    @RequestMapping(value = "/actions/{id}/rejectMatch",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Action> rejectMatchFromAction(@PathVariable Long id) throws URISyntaxException {

        if (!actionRepository.findActionById(id).isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }

        Action action = actionRepository.getOne(id);

        System.out.println(action);
        System.out.println(action.getMatch());

        if (action.getMatch() == null) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert("action", "notMatched", "You cannot reject a match of an action which has no match.")).body(null);
        }

        log.debug("REST request to reject match : {}", action);

        action = rejectMatch(action);
        Action result = actionRepository.save(action);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("action", action.getId().toString()))
            .body(result);
    }




    /**
     * checks whether a match is available for a specific action
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

            Float matchDist = disasterService.getDistance(a.getLat(), a.getLon(), act.getLat(), act.getLon(), (a.getActionType().equals(ActionType.OFFER) ? a.getCreatedDate() : act.getCreatedDate()));

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
                new Conversation(true, bestMatch.getDescription() + " Conversation")
            );
            messageClient.addMember(new User(bestMatch.getUser().getUserId()), savedConversation.getId());
            messageClient.addMember(new User(a.getUser().getUserId()), savedConversation.getId());
        }

//        System.out.println("### match " + bestMatch + "###");

        return a;

    }


    /**
     * removes a match from actions
     * commented lines for later removal of already rejected actions
     *
     * @param a the action the match shall be removed from
     */
    public Action rejectMatch(Action a) {
//        if (a.getMatch() == null) {
//            return ;
//        }

        a.getMatch().addRejectedMatch(a);
        Action otherAction = a.getMatch();
        a.getMatch().setMatch(null);
        actionRepository.save(a.getMatch());

        a.addRejectedMatch(a.getMatch());
        a.setMatch(null);
//        actionRepository.save(a);

        matchActions(otherAction);

        matchActions(a);

        return a;
    }

}
