package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionObjectRepository;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * REST controller for managing ActionObject.
 */
@RestController
@RequestMapping("/api")
public class ActionObjectResource {

    private final Logger log = LoggerFactory.getLogger(ActionObjectResource.class);

    @Inject
    private ActionObjectRepository actionObjectRepository;


    @Inject
    private ActionRepository actionRepository;

    @Autowired
    public ActionObjectResource(ActionRepository actionRepositoryRepository,
                                ActionObjectRepository actionObjectRepository) {
        this.actionRepository = actionRepositoryRepository;
        this.actionObjectRepository = actionObjectRepository;
    }

    /**
     * POST  /action-objects : Create a new actionObject.
     *
     * @param actionObject the actionObject to create
     * @return the ResponseEntity with status 201 (Created) and with body the new actionObject, or with status 400 (Bad Request) if the actionObject has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/action-objects",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ActionObject> createActionObject(@RequestBody ActionObject actionObject) throws URISyntaxException {
        log.debug("REST request to save ActionObject : {}", actionObject);
        if (actionObject.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("actionObject", "idexists", "A new actionObject cannot already have an ID")).body(null);
        }
        ActionObject result = actionObjectRepository.save(actionObject);
        return ResponseEntity.created(new URI("/api/action-objects/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("actionObject", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /action-objects : Updates an existing actionObject.
     *
     * @param actionObject the actionObject to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated actionObject,
     * or with status 400 (Bad Request) if the actionObject is not valid,
     * or with status 500 (Internal Server Error) if the actionObject couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/action-objects",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ActionObject> updateActionObject(@RequestBody ActionObject actionObject) throws URISyntaxException {
        log.debug("REST request to update ActionObject : {}", actionObject);
        if (actionObject.getId() == null) {
            return createActionObject(actionObject);
        }
        ActionObject result = actionObjectRepository.save(actionObject);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("actionObject", actionObject.getId().toString()))
            .body(result);
    }

    /**
     * GET  /action-objects : get all the actionObjects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of actionObjects in body
     */
    @RequestMapping(value = "/action-objects",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<ActionObject> getAllActionObjects() {
        log.debug("REST request to get all ActionObjects");
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        return actionObjects;
    }

    /**
     * GET  /action-objects/:id : get the "id" actionObject.
     *
     * @param id the id of the actionObject to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the actionObject, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/action-objects/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<ActionObject> getActionObject(@PathVariable Long id) {
        log.debug("REST request to get ActionObject : {}", id);
        ActionObject actionObject = actionObjectRepository.findOne(id);
        return Optional.ofNullable(actionObject)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /action-objects/:id : delete the "id" actionObject.
     *
     * @param id the id of the actionObject to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/action-objects/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteActionObject(@PathVariable Long id) {
        log.debug("REST request to delete ActionObject : {}", id);
        actionObjectRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("actionObject", id.toString())).build();
    }

    @RequestMapping(value = "/action-objects/topten/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<ActionObject> getTopTenSearch(@PathVariable Long id) {
        log.debug("REST request to get TopTen of Search of a Disaster : {}", id );


        HashMap<ActionObject,Integer> objectWithRate = new HashMap<ActionObject,Integer>();

        List<Action> actions = actionRepository.findByDisasterIdAndActionType(id, ActionType.SEEK);

        List<ActionObject> actionObjects = new ArrayList<ActionObject>();

        //alle ActionObjekte werden einer Actionobject Liste hinzugefügt
        for(int i = 0;i<actions.size();i++){
            actionObjects.addAll(actions.get(i).getActionObjects());
        }


        //ActionObjects werden mit ihrer Häufigkeit in einer Hashmap gespeichert
        for(int i = 0;i<actionObjects.size();i++){
            ActionObject actionObject = actionObjects.get(i);
            if(objectWithRate.keySet().contains(actionObject)){
                int a = objectWithRate.get(actionObject);
                objectWithRate.put(actionObject,a+1);
            }else{
                objectWithRate.put(actionObject,1);
            }
        }

        //Liste enthält keine mehrfache Elemente mehr
        actionObjects.clear();
        actionObjects.addAll(objectWithRate.keySet());


        Comparator<ActionObject> c = new Comparator<ActionObject>() {
            @Override
            public int compare(ActionObject o1, ActionObject o2) {
                if(objectWithRate.get(o1)>objectWithRate.get(o2)){
                    return -1;
                }
                if(objectWithRate.get(o1)==objectWithRate.get(o2)){
                    return 0;
                }
                return 1;
            }
        };

        //Liste wird nach Häufigkeit sortiert

        actionObjects.sort(c);

        //erste 10 Elemente werden zurück gegeben
        if(actionObjects.size()==0){
            return actionObjects;
        }
        if(actionObjects.size()>9) {
            actionObjects.subList(0, 9);
        }else{
            actionObjects.subList(0, actionObjects.size()-1);
        }
        return actionObjects;

    }

}
