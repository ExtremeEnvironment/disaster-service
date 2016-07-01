package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
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

/**
 * REST controller for managing Disaster.
 */
@RestController
@RequestMapping("/api")
public class DisasterResource {

    private final Logger log = LoggerFactory.getLogger(DisasterResource.class);

    @Inject
    private ActionRepository actionRepository;
    @Inject
    private  DisasterRepository disasterRepository;

    @Autowired
    public DisasterResource(ActionRepository actionRepositoryRepository,
                          DisasterRepository disasterRepository) {
        this.actionRepository = actionRepositoryRepository;
        this.disasterRepository = disasterRepository;
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

        Disaster dis = getDisasterForDisaster(disaster);

        disaster.setIsExpired(false);

        if ((dis != null) && (dis.getDisasterType() == disaster.getDisasterType())&& (dis.isIsExpired()==false)) {
            Action action = new Action();
            action.setActionType(ActionType.KNOWLEDGE);
            action.setLat(disaster.getLat());
            action.setLon(disaster.getLon());
            actionRepository.saveAndFlush(action);

            return ResponseEntity.ok()
                .body(dis);

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
     * @param disaster
     * @return the nearest disaster of an action location, in a radius of 15000km
     */
    public Disaster getDisasterForDisaster(Disaster disaster) {
        float distance = 15000;
        Disaster disasterReturn = null;
        float lon = disaster.getLon();
        float lat = disaster.getLat();

        List<Disaster> disasterList = disasterRepository.findAll();

        for (int i = 0; i < disasterList.size(); i++) {
            Disaster disaster1 = disasterList.get(i);
            Float disasterLon = disaster1.getLon();
            Float disasterLat = disaster1.getLat();
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





