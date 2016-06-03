package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.repository.ActionObjectRepository;
import de.extremeenvironment.disasterservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ActionObject.
 */
@RestController
@RequestMapping("/api")
public class ActionObjectResource {

    private final Logger log = LoggerFactory.getLogger(ActionObjectResource.class);
        
    @Inject
    private ActionObjectRepository actionObjectRepository;
    
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

}
