package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.service.DisasterService;
import de.extremeenvironment.disasterservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing Disaster.
 */
@RestController
@RequestMapping("/api")
public class DisasterResource {

    private final Logger log = LoggerFactory.getLogger(DisasterResource.class);

    private ActionRepository actionRepository;

    private DisasterRepository disasterRepository;

    private DisasterService disasterService;

    @Inject
    public DisasterResource(ActionRepository actionRepository, DisasterRepository disasterRepository, DisasterService disasterService) {
        this.actionRepository = actionRepository;
        this.disasterRepository = disasterRepository;
        this.disasterService = disasterService;
    }

    /**
     * POST  /disasters : Create a new disaster.
     *
     * @param disaster the disaster to create
     * @return the ResponseEntity with status 201 (Created) and with body the new disaster, or with status 400 (Bad Request) if the disaster has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/disasters",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Disaster> createDisaster(@RequestBody Disaster disaster) throws URISyntaxException {
        log.debug("REST request to save Disaster : {}", disaster);
        if (disaster.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("disaster", "idexists", "A new disaster cannot already have an ID")).body(null);
        }

        Disaster dis = disasterService.getDisasterForDisaster(disaster);

        disaster.setIsExpired(false);

        if ((dis != null) && (dis.getDisasterType() == disaster.getDisasterType()) && (dis.isIsExpired() == false)) {
            Action action = new Action();
            action.setActionType(ActionType.KNOWLEDGE);
            action.setLat(disaster.getLat());
            action.setLon(disaster.getLon());
            actionRepository.saveAndFlush(action);

            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("disaster", "disasteralreadyexists", "A disaster already exists at this location")).body(null);

        } else {
            Disaster result = disasterRepository.save(disaster);

            return ResponseEntity.created(new URI("/api/disasters/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("disaster", result.getId().toString()))
                .body(result);

        }
    }

    /**
     * PUT  /disasters : Updates an existing disaster.
     *
     * @param disaster the disaster to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated disaster,
     * or with status 400 (Bad Request) if the disaster is not valid,
     * or with status 500 (Internal Server Error) if the disaster couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/disasters",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Disaster> updateDisaster(@RequestBody Disaster disaster) throws URISyntaxException {
        log.debug("REST request to update Disaster : {}", disaster);
        if (disaster.getId() == null) {
            return createDisaster(disaster);
        }
        Disaster result = disasterRepository.save(disaster);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("disaster", disaster.getId().toString()))
            .body(result);
    }

    /**
     * GET  /disasters : get all the disasters.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of disasters in body
     */
    @RequestMapping(value = "/disasters",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Disaster> getAllDisasters() {
        log.debug("REST request to get all Disasters");
        List<Disaster> disasters = disasterRepository.findAll();
        return disasters;
    }

    /**
     * GET  /disasters/:id : get the "id" disaster.
     *
     * @param id the id of the disaster to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the disaster, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/disasters/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Disaster> getDisaster(@PathVariable Long id) {
        log.debug("REST request to get Disaster : {}", id);
        Disaster disaster = disasterRepository.findOne(id);
        return Optional.ofNullable(disaster)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /disasters/:id : delete the "id" disaster.
     *
     * @param id the id of the disaster to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/disasters/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteDisaster(@PathVariable Long id) {
        log.debug("REST request to delete Disaster : {}", id);
        disasterRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("disaster", id.toString())).build();
    }


    /**
     * GET  /disasters/:id/heatmap : get the Knowledge or Seek-actions for "id" disaster
     *
     * @param id the id of the disaster to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the disaster, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/disasters/{id}/heatmap",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Action> getActionsInDisasterForHeatmap(@PathVariable Long id) {
        log.debug("REST request to get all Actions in a Disaster ");
        List<Action> actions = actionRepository.findByDisasterId(id)
            .stream().filter(a -> (a.getActionType() == ActionType.KNOWLEDGE || a.getActionType() == ActionType.SEEK))
            .collect(Collectors.toList());
        return actions;
    }





}





