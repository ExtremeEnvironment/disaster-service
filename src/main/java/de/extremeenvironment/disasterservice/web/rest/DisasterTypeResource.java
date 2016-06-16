package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.DisasterType;
import de.extremeenvironment.disasterservice.repository.DisasterTypeRepository;
import de.extremeenvironment.disasterservice.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * REST controller for managing DisasterType.
 */
@RestController
@RequestMapping("/api")
public class DisasterTypeResource {

    private final Logger log = LoggerFactory.getLogger(DisasterTypeResource.class);

    @Inject
    private DisasterTypeRepository disasterTypeRepository;

    /**
     * POST  /disaster-types : Create a new disasterType.
     *
     * @param disasterType the disasterType to create
     * @return the ResponseEntity with status 201 (Created) and with body the new disasterType, or with status 400 (Bad Request) if the disasterType has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/disaster-types",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<DisasterType> createDisasterType(@RequestBody DisasterType disasterType) throws URISyntaxException {
        log.debug("REST request to save DisasterType : {}", disasterType);
        if (disasterType.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("disasterType", "idexists", "A new disasterType cannot already have an ID")).body(null);
        }
        DisasterType result = disasterTypeRepository.save(disasterType);
        return ResponseEntity.created(new URI("/api/disaster-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("disasterType", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /disaster-types : Updates an existing disasterType.
     *
     * @param disasterType the disasterType to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated disasterType,
     * or with status 400 (Bad Request) if the disasterType is not valid,
     * or with status 500 (Internal Server Error) if the disasterType couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/disaster-types",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<DisasterType> updateDisasterType(@RequestBody DisasterType disasterType) throws URISyntaxException {
        log.debug("REST request to update DisasterType : {}", disasterType);
        if (disasterType.getId() == null) {
            return createDisasterType(disasterType);
        }
        DisasterType result = disasterTypeRepository.save(disasterType);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("disasterType", disasterType.getId().toString()))
            .body(result);
    }

    /**
     * GET  /disaster-types : get all the disasterTypes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of disasterTypes in body
     */
    @RequestMapping(value = "/disaster-types",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<DisasterType> getAllDisasterTypes() {
        log.debug("REST request to get all DisasterTypes");
        List<DisasterType> disasterTypes = disasterTypeRepository.findAll();
        return disasterTypes;
    }

    /**
     * GET  /disaster-types/:id : get the "id" disasterType.
     *
     * @param id the id of the disasterType to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the disasterType, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/disaster-types/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<DisasterType> getDisasterType(@PathVariable Long id) {
        log.debug("REST request to get DisasterType : {}", id);
        DisasterType disasterType = disasterTypeRepository.findOne(id);
        return Optional.ofNullable(disasterType)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /disaster-types/:id : delete the "id" disasterType.
     *
     * @param id the id of the disasterType to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/disaster-types/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteDisasterType(@PathVariable Long id) {
        log.debug("REST request to delete DisasterType : {}", id);
        disasterTypeRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("disasterType", id.toString())).build();
    }

}
