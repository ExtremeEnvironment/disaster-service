package de.extremeenvironment.disasterservice.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import de.extremeenvironment.disasterservice.domain.Area;
import de.extremeenvironment.disasterservice.domain.Corner;
import de.extremeenvironment.disasterservice.domain.Ngo;
import de.extremeenvironment.disasterservice.repository.AreaRepository;
import de.extremeenvironment.disasterservice.repository.CornerRepository;
import de.extremeenvironment.disasterservice.repository.NgoRepository;
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

    @Inject
    private NgoRepository ngoRepository;

    @Inject
    AreaResource(AreaRepository areaRepository,CornerRepository cornerRepository,NgoRepository ngoRepository){
        this.areaRepository = areaRepository;
        this.cornerRepository = cornerRepository;
        this.ngoRepository = ngoRepository;
    }
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

        if (!checkIsValidArea(area, area.getCorners())){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("area", "areaoverlaps", "This area overlaps with an already existing area")).body(null);
        }



        Set<Corner> cornerSet = new HashSet<>();
        ArrayList<Corner> corners = new ArrayList<>();
        corners.addAll(area.getCorners());

        for(Corner co : corners){
            Corner cor = new Corner();
            cor.setLat(co.getLat());
            cor.setLon(co.getLon());
            cornerSet.add(cor);
            cor.setArea(area);
        }

        Ngo ngo = new Ngo();
        ngo.setId(area.getNgo().getId());
        ngo.setArea(area);
        ngo.setName(area.getNgo().getName());

        Area result = areaRepository.saveAndFlush(area);
        corners.addAll(cornerSet);

        ngoRepository.save(ngo);
        for(Corner co :corners){
            cornerRepository.saveAndFlush(co);
        }


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


    /**
     * checks whether a specific area can be claimed, because it overlaps with another area by 5% at max
     *
     * @param area the to-be-tested area
     * @param corners the corners of the to-be-tested area
     * @return the validity
     */
    private boolean checkIsValidArea(Area area, Set<Corner> corners) {

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
//        cornerList.sort(comp);


        HashMap<Area, Polygon> allAreas = new HashMap<>();
        Polygon thisPoly = cornersToPolygon(cornerList);


//        log.debug("cornerRepository : ", cornerRepository);
//        log.debug("corners of area : ", cornerRepository.findByAreaId(area.getId()));
//        areaRepository.findAll().forEach(i -> i.getCorners().forEach(j -> System.out.println(j.toString())));


        areaRepository.findAll().forEach(a ->
            allAreas.put(a, cornersToPolygon(new ArrayList<>(a.getCorners())))
        );

        allAreas.remove(area);

        double thisPolyArea = thisPoly.getArea();

        for (Polygon p : allAreas.values()) {
            Geometry intersect = p.intersection(thisPoly);

            if (!intersect.isEmpty() && ((intersect.getArea() / p.getArea() > 0.05) || (intersect.getArea() / thisPolyArea > 0.05))) {
                return false;
            }

        }

        return true;
    }


    /**
     * converts a set of Corners into a polygon
     *
     * @param cList the Corners
     * @return the polygon
     */
    private Polygon cornersToPolygon(List<Corner> cList) {
        ArrayList<Coordinate> coordList = new ArrayList<>();
        cList.forEach(c -> coordList.add(new Coordinate(c.getLat(), c.getLon())));
        coordList.add(new Coordinate(cList.get(0).getLat(), cList.get(0).getLon()));

        CoordinateSequence cs = new CoordinateArraySequence(coordList.toArray(new Coordinate[0]));

        GeometryFactory gf = new GeometryFactory();

        LinearRing shell = new LinearRing(cs, gf);


        return new Polygon(shell, null, gf);

    }

}
