package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.Corner;
import de.extremeenvironment.disasterservice.repository.CornerRepository;
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
 * REST controller for managing Corner.
 */
@RestController
@RequestMapping("/api")
public class CornerResource {

    private final Logger log = LoggerFactory.getLogger(CornerResource.class);
        
    @Inject
    private CornerRepository cornerRepository;
    
    /**
     * POST  /corners : Create a new corner.
     *
     * @param corner the corner to create
     * @return the ResponseEntity with status 201 (Created) and with body the new corner, or with status 400 (Bad Request) if the corner has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/corners",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Corner> createCorner(@RequestBody Corner corner) throws URISyntaxException {
        log.debug("REST request to save Corner : {}", corner);
        if (corner.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("corner", "idexists", "A new corner cannot already have an ID")).body(null);
        }
        Corner result = cornerRepository.save(corner);
        return ResponseEntity.created(new URI("/api/corners/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("corner", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /corners : Updates an existing corner.
     *
     * @param corner the corner to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated corner,
     * or with status 400 (Bad Request) if the corner is not valid,
     * or with status 500 (Internal Server Error) if the corner couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/corners",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Corner> updateCorner(@RequestBody Corner corner) throws URISyntaxException {
        log.debug("REST request to update Corner : {}", corner);
        if (corner.getId() == null) {
            return createCorner(corner);
        }
        Corner result = cornerRepository.save(corner);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("corner", corner.getId().toString()))
            .body(result);
    }

    /**
     * GET  /corners : get all the corners.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of corners in body
     */
    @RequestMapping(value = "/corners",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Corner> getAllCorners() {
        log.debug("REST request to get all Corners");
        List<Corner> corners = cornerRepository.findAll();
        return corners;
    }

    /**
     * GET  /corners/:id : get the "id" corner.
     *
     * @param id the id of the corner to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the corner, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/corners/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Corner> getCorner(@PathVariable Long id) {
        log.debug("REST request to get Corner : {}", id);
        Corner corner = cornerRepository.findOne(id);
        return Optional.ofNullable(corner)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /corners/:id : delete the "id" corner.
     *
     * @param id the id of the corner to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/corners/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteCorner(@PathVariable Long id) {
        log.debug("REST request to delete Corner : {}", id);
        cornerRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("corner", id.toString())).build();
    }

}
