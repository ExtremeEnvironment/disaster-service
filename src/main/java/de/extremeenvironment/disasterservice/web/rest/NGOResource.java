package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.Ngo;
import de.extremeenvironment.disasterservice.repository.NGORepository;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * REST controller for managing Ngo.
 */
@RestController
@RequestMapping("/api")
public class NGOResource {

    private final Logger log = LoggerFactory.getLogger(NGOResource.class);

    @Inject
    private NGORepository nGORepository;

    /**
     * POST  /n-gos : Create a new nGO.
     *
     * @param nGO the nGO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new nGO, or with status 400 (Bad Request) if the nGO has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/n-gos",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Ngo> createNGO(@RequestBody Ngo nGO) throws URISyntaxException {
        log.debug("REST request to save Ngo : {}", nGO);
        if (nGO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("nGO", "idexists", "A new nGO cannot already have an ID")).body(null);
        }
        Ngo result = nGORepository.save(nGO);
        return ResponseEntity.created(new URI("/api/n-gos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("nGO", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /n-gos : Updates an existing nGO.
     *
     * @param nGO the nGO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated nGO,
     * or with status 400 (Bad Request) if the nGO is not valid,
     * or with status 500 (Internal Server Error) if the nGO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/n-gos",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Ngo> updateNGO(@RequestBody Ngo nGO) throws URISyntaxException {
        log.debug("REST request to update Ngo : {}", nGO);
        if (nGO.getId() == null) {
            return createNGO(nGO);
        }
        Ngo result = nGORepository.save(nGO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("nGO", nGO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /n-gos : get all the nGOS.
     *
     * @param filter the filter of the request
     * @return the ResponseEntity with status 200 (OK) and the list of nGOS in body
     */
    @RequestMapping(value = "/n-gos",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Ngo> getAllNGOS(@RequestParam(required = false) String filter) {
        if ("area-is-null".equals(filter)) {
            log.debug("REST request to get all NGOs where area is null");
            return StreamSupport
                .stream(nGORepository.findAll().spliterator(), false)
                .filter(nGO -> nGO.getArea() == null)
                .collect(Collectors.toList());
        }
        log.debug("REST request to get all NGOS");
        List<Ngo> nGOS = nGORepository.findAll();
        return nGOS;
    }

    /**
     * GET  /n-gos/:id : get the "id" nGO.
     *
     * @param id the id of the nGO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the nGO, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/n-gos/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Ngo> getNGO(@PathVariable Long id) {
        log.debug("REST request to get Ngo : {}", id);
        Ngo nGO = nGORepository.findOne(id);
        return Optional.ofNullable(nGO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /n-gos/:id : delete the "id" nGO.
     *
     * @param id the id of the nGO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/n-gos/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteNGO(@PathVariable Long id) {
        log.debug("REST request to delete Ngo : {}", id);
        nGORepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("nGO", id.toString())).build();
    }

}
