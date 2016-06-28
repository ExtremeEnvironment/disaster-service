package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.Ngo;
import de.extremeenvironment.disasterservice.repository.NGORepository;
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
    private NGORepository ngoRepository;

    /**
     * POST  /ngos : Create a new ngo.
     *
     * @param ngo the ngo to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ngo, or with status 400 (Bad Request) if the ngo has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/ngos",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Ngo> createNgo(@RequestBody Ngo ngo) throws URISyntaxException {
        log.debug("REST request to save Ngo : {}", ngo);
        if (ngo.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ngo", "idexists", "A new ngo cannot already have an ID")).body(null);
        }
        Ngo result = ngoRepository.save(ngo);
        return ResponseEntity.created(new URI("/api/ngos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("ngo", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ngos : Updates an existing ngo.
     *
     * @param ngo the ngo to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ngo,
     * or with status 400 (Bad Request) if the ngo is not valid,
     * or with status 500 (Internal Server Error) if the ngo couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/ngos",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Ngo> updateNgo(@RequestBody Ngo ngo) throws URISyntaxException {
        log.debug("REST request to update Ngo : {}", ngo);
        if (ngo.getId() == null) {
            return createNgo(ngo);
        }
        Ngo result = ngoRepository.save(ngo);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("ngo", ngo.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ngos : get all the ngos.
     *
     * @param filter the filter of the request
     * @return the ResponseEntity with status 200 (OK) and the list of ngos in body
     */
    @RequestMapping(value = "/ngos",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Ngo> getAllNgos(@RequestParam(required = false) String filter) {
        if ("area-is-null".equals(filter)) {
            log.debug("REST request to get all Ngos where area is null");
            return StreamSupport
                .stream(ngoRepository.findAll().spliterator(), false)
                .filter(ngo -> ngo.getArea() == null)
                .collect(Collectors.toList());
        }
        log.debug("REST request to get all Ngos");
        List<Ngo> ngos = ngoRepository.findAll();
        return ngos;
    }

    /**
     * GET  /ngos/:id : get the "id" ngo.
     *
     * @param id the id of the ngo to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ngo, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/ngos/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Ngo> getNgo(@PathVariable Long id) {
        log.debug("REST request to get Ngo : {}", id);
        Ngo ngo = ngoRepository.findOne(id);
        return Optional.ofNullable(ngo)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /ngos/:id : delete the "id" ngo.
     *
     * @param id the id of the ngo to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/ngos/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteNgo(@PathVariable Long id) {
        log.debug("REST request to delete Ngo : {}", id);
        ngoRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("ngo", id.toString())).build();
    }

}
