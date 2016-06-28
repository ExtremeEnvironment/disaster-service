package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.extremeenvironment.disasterservice.domain.Area;
import de.extremeenvironment.disasterservice.domain.Corner;
import de.extremeenvironment.disasterservice.repository.AreaRepository;
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
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

/**
 * REST controller for managing Area.
 */
@RestController
@RequestMapping("/api")
public class AreaResource {

    private final Logger log = LoggerFactory.getLogger(AreaResource.class);

    @Inject
    private AreaRepository areaRepository;

    @Inject
    private CornerRepository cornerRepository;

    /**
     * POST  /areas : Create a new area.
     *
     * @param area the area to create
     * @return the ResponseEntity with status 201 (Created) and with body the new area, or with status 400 (Bad Request) if the area has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/areas",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Area> createArea(@RequestBody Area area) throws URISyntaxException {
        log.debug("REST request to save Area : {}", area);
        if (area.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("area", "idexists", "A new area cannot already have an ID")).body(null);
        }

        if (!checkValidArea(area, area.getCorners())){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("area", "areaoverlaps", "This area overlaps with an already existing area")).body(null);
        }

        Area result = areaRepository.save(area);
        return ResponseEntity.created(new URI("/api/areas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("area", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /areas : Updates an existing area.
     *
     * @param area the area to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated area,
     * or with status 400 (Bad Request) if the area is not valid,
     * or with status 500 (Internal Server Error) if the area couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/areas",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Area> updateArea(@RequestBody Area area) throws URISyntaxException {
        log.debug("REST request to update Area : {}", area);
        if (area.getId() == null) {
            return createArea(area);
        }
        Area result = areaRepository.save(area);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("area", area.getId().toString()))
            .body(result);
    }

    /**
     * GET  /areas : get all the areas.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of areas in body
     */
    @RequestMapping(value = "/areas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Area> getAllAreas() {
        log.debug("REST request to get all Areas");
        List<Area> areas = areaRepository.findAll();
        return areas;
    }

    /**
     * GET  /areas/:id : get the "id" area.
     *
     * @param id the id of the area to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the area, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/areas/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Area> getArea(@PathVariable Long id) {
        log.debug("REST request to get Area : {}", id);
        Area area = areaRepository.findOne(id);
        return Optional.ofNullable(area)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /areas/:id : delete the "id" area.
     *
     * @param id the id of the area to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/areas/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        log.debug("REST request to delete Area : {}", id);
        areaRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("area", id.toString())).build();
    }


    private boolean checkValidArea(Area area, Set<Corner> corners) {

        Comparator<Corner> comp = new Comparator<Corner>() {
            @Override
            public int compare(Corner c1, Corner c2) {
                if (c1.getId() < c2.getId()) {
                    return 1;
                }

                return -1;
            }
        };

        List<Corner> cornerList = new ArrayList<>(corners);

        cornerList.sort(comp);


        HashMap<Area, java.awt.geom.Area> allAreas = new HashMap<>();

        java.awt.geom.Area thisArea = cornersToArea(cornerList);


        for (Area a : areaRepository.findAll()) {
            List<Corner> cList = cornerRepository.findByAreaId(a.getId());

            allAreas.put(a, cornersToArea(cList));
        }

        allAreas.remove(area);

        for (java.awt.geom.Area a : allAreas.values()) {
            java.awt.geom.Area aCopy = new java.awt.geom.Area(a);
            aCopy.intersect(thisArea);

            if (aCopy.isEmpty()) {
                return false;
            }
        }

        return true;
    }


    private long polygonArea(int[] lat, int[] lon) {
        long area = 0;         // Accumulates area in the loop
        int j = lat.length - 1;  // The last vertex is the 'previous' one to the first

        for (int i = 0; i < lat.length; i++) {
            area = area + (lat[j] + lat[i]) * (lon[j] - lon[i]);
            j = i;  //j is previous vertex to i
        }
        return area / 2;
    }


    private java.awt.geom.Area cornersToArea(List<Corner> cList) {
        int[] lat = new int[cList.size()];
        int[] lon = new int[cList.size()];

        for (int i = 0; i < cList.size(); i++) {
            lat[i] = (int) (cList.get(i).getLat() * 10_000_000);
            lon[i] = (int) (cList.get(i).getLon() * 10_000_000);
        }

        return new java.awt.geom.Area(new Polygon(lat, lon, cList.size()));
    }


}
