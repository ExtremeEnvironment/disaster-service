package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.config.JHipsterProperties;
import de.extremeenvironment.disasterservice.domain.*;
import de.extremeenvironment.disasterservice.domain.Area;
import de.extremeenvironment.disasterservice.repository.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.hasItem;

import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the AreaResource REST controller.
 *
 * @see AreaResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class AreaResourceIntTest {

    Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private AreaRepository areaRepository;

    @Inject
    private CornerRepository cornerRepository;

    @Inject
    private DisasterRepository disasterRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private NgoRepository ngoRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restAreaMockMvc;

    private Area area;

    private Disaster disaster;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AreaResource areaResource = new AreaResource();
        ReflectionTestUtils.setField(areaResource, "areaRepository", areaRepository);
        this.restAreaMockMvc = MockMvcBuilders.standaloneSetup(areaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
//    }
//
//    @Before
//    public void initTest() {
//        userRepository.saveAndFlush(new User());
        area = new Area();

        disaster = new Disaster();
        disaster.setLat(23F);
        disaster.setLon(23F);
        disasterRepository.saveAndFlush(disaster);

        ngoRepository.saveAndFlush(new Ngo());


    }

    @Test
    @Transactional
    public void createArea() throws Exception {
        int databaseSizeBeforeCreate = areaRepository.findAll().size();

        Set<Corner> cornerSet = new HashSet<>();

        Corner c11 = new Corner();
        c11.setLat(1F);
        c11.setLon(1F);
        cornerSet.add(c11);
        c11.setArea(area);

        Corner c12 = new Corner();
        c12.setLat(2F);
        c12.setLon(1F);
        cornerSet.add(c12);
        c12.setArea(area);


        Corner c13 = new Corner();
        c13.setLat(2F);
        c13.setLon(2F);
        cornerSet.add(c13);
        c13.setArea(area);


        area.setCorners(cornerSet);


        // Create the Area

        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(area)))
            .andExpect(status().isCreated());

        // Validate the Area in the database
        List<Area> areas = areaRepository.findAll();
        assertThat(areas).hasSize(databaseSizeBeforeCreate + 1);
        Area testArea = areas.get(areas.size() - 1);
    }

    @Test
    @Transactional
    public void getAllAreas() throws Exception {
        // Initialize the database
        areaRepository.saveAndFlush(area);

        // Get all the areas
        restAreaMockMvc.perform(get("/api/areas?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(area.getId().intValue())));
    }

    @Test
    @Transactional
    public void getArea() throws Exception {
        // Initialize the database
        areaRepository.saveAndFlush(area);

        // Get the area
        restAreaMockMvc.perform(get("/api/areas/{id}", area.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(area.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingArea() throws Exception {
        // Get the area
        restAreaMockMvc.perform(get("/api/areas/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateArea() throws Exception {
        // Initialize the database
        areaRepository.saveAndFlush(area);
        int databaseSizeBeforeUpdate = areaRepository.findAll().size();

        // Update the area
        Area updatedArea = new Area();
        updatedArea.setId(area.getId());

        restAreaMockMvc.perform(put("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedArea)))
            .andExpect(status().isOk());

        // Validate the Area in the database
        List<Area> areas = areaRepository.findAll();
        assertThat(areas).hasSize(databaseSizeBeforeUpdate);
        Area testArea = areas.get(areas.size() - 1);
    }

    @Test
    @Transactional
    public void deleteArea() throws Exception {
        // Initialize the database
        areaRepository.saveAndFlush(area);
        int databaseSizeBeforeDelete = areaRepository.findAll().size();

        // Get the area
        restAreaMockMvc.perform(delete("/api/areas/{id}", area.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Area> areas = areaRepository.findAll();
        assertThat(areas).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void createAreaWithCorners() throws Exception {
        Set<Corner> cornerSet = new HashSet<>();

        Area a = new Area();

        Corner c11 = new Corner();
        c11.setLat(1F);
        c11.setLon(1F);
        cornerSet.add(c11);
        c11.setArea(a);

        Corner c12 = new Corner();
        c12.setLat(2F);
        c12.setLon(1F);
        cornerSet.add(c12);
        c12.setArea(a);


        Corner c13 = new Corner();
        c13.setLat(2F);
        c13.setLon(2F);
        cornerSet.add(c13);
        c13.setArea(a);


        Corner c14 = new Corner();
        c14.setLat(1F);
        c14.setLon(2F);
        cornerSet.add(c14);
        c14.setArea(a);


        a.setCorners(cornerSet);

        System.out.println(a.getCorners());


        log.debug("area {}", TestUtil.convertObjectToJsonBytes(a));

        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(a)))
            .andExpect(status().isCreated());

        List<Area> resultsArea = areaRepository.findAll();

        List<Corner> resultsCorner = cornerRepository.findAll();

        resultsCorner.forEach(c -> assertTrue(c.getArea().equals(resultsArea.get(resultsArea.size() - 1))));

    }

    @Test
    @Transactional
    public void createOverlappingArea() throws Exception {
        Set<Corner> cornerSet1 = new HashSet<>();
        Set<Corner> cornerSet2 = new HashSet<>();


        Area a1 = new Area();
        Area a2 = new Area();


        Corner c11 = new Corner();
        c11.setLat(1F);
        c11.setLon(1F);
        cornerSet1.add(c11);
        c11.setArea(a1);

        Corner c12 = new Corner();
        c12.setLat(2F);
        c12.setLon(1F);
        cornerSet1.add(c12);
        c12.setArea(a1);

        Corner c13 = new Corner();
        c13.setLat(2F);
        c13.setLon(2F);
        cornerSet1.add(c13);
        c13.setArea(a1);

        Corner c14 = new Corner();
        c14.setLat(1F);
        c14.setLon(2F);
        cornerSet1.add(c14);
        c14.setArea(a1);



        Corner c21 = new Corner();
        c21.setLat(1.5F);
        c21.setLon(1.5F);
        cornerSet2.add(c21);
        c21.setArea(a2);

        Corner c22 = new Corner();
        c22.setLat(3F);
        c22.setLon(1.5F);
        cornerSet2.add(c22);
        c22.setArea(a2);

        Corner c23 = new Corner();
        c23.setLat(3F);
        c23.setLon(3F);
        cornerSet2.add(c23);
        c23.setArea(a2);

        Corner c24 = new Corner();
        c24.setLat(1.5F);
        c24.setLon(3F);
        cornerSet2.add(c24);
        c24.setArea(a2);


        a1.setCorners(cornerSet1);
        a2.setCorners(cornerSet2);

//        System.out.println(a.getCorners());


        log.debug("area {}", TestUtil.convertObjectToJsonBytes(a1));
        log.debug("area {}", TestUtil.convertObjectToJsonBytes(a2));


        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(a1)))
            .andExpect(status().isCreated());

        log.debug("first insert finished");


        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(a2)))
            .andExpect(status().isBadRequest());


    }

    @Test
    @Transactional
    public void createNonOverlappingArea() throws Exception {
        Set<Corner> cornerSet1 = new HashSet<>();
        Set<Corner> cornerSet2 = new HashSet<>();


        Area a1 = new Area();
        Area a2 = new Area();


        Corner c11 = new Corner();
        c11.setLat(1F);
        c11.setLon(1F);
        cornerSet1.add(c11);
        c11.setArea(a1);

        Corner c12 = new Corner();
        c12.setLat(1F);
        c12.setLon(2F);
        cornerSet1.add(c12);
        c12.setArea(a1);

        Corner c13 = new Corner();
        c13.setLat(2F);
        c13.setLon(2F);
        cornerSet1.add(c13);
        c13.setArea(a1);

        Corner c14 = new Corner();
        c14.setLat(2F);
        c14.setLon(1F);
        cornerSet1.add(c14);
        c14.setArea(a1);



        Corner c21 = new Corner();
        c21.setLat(3F);
        c21.setLon(3F);
        cornerSet2.add(c21);
        c21.setArea(a2);

        Corner c22 = new Corner();
        c22.setLat(4F);
        c22.setLon(3F);
        cornerSet2.add(c22);
        c22.setArea(a2);

        Corner c23 = new Corner();
        c23.setLat(4F);
        c23.setLon(4F);
        cornerSet2.add(c23);
        c23.setArea(a2);

        Corner c24 = new Corner();
        c24.setLat(3F);
        c24.setLon(4F);
        cornerSet2.add(c24);
        c24.setArea(a2);


        a1.setCorners(cornerSet1);
        a2.setCorners(cornerSet2);

        System.out.println(a1.getCorners());
        System.out.println(a2.getCorners());


        log.debug("area {}", TestUtil.convertObjectToJsonBytes(a1));
        log.debug("area {}", TestUtil.convertObjectToJsonBytes(a2));

        log.debug("first insert begin");


        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(a1)))
            .andExpect(status().isCreated());

        log.debug("first insert finished");


        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(a2)))
            .andExpect(status().isCreated());


    }

    @Test
    @Transactional
    public void createOverlappingAreaWithinMargins() throws Exception {
        Set<Corner> cornerSet1 = new HashSet<>();
        Set<Corner> cornerSet2 = new HashSet<>();


        Area a1 = new Area();
        Area a2 = new Area();


        Corner c11 = new Corner();
        c11.setLat(1F);
        c11.setLon(1F);
        cornerSet1.add(c11);
        c11.setArea(a1);

        Corner c12 = new Corner();
        c12.setLat(1F);
        c12.setLon(2F);
        cornerSet1.add(c12);
        c12.setArea(a1);

        Corner c13 = new Corner();
        c13.setLat(2F);
        c13.setLon(2F);
        cornerSet1.add(c13);
        c13.setArea(a1);

        Corner c14 = new Corner();
        c14.setLat(2F);
        c14.setLon(1F);
        cornerSet1.add(c14);
        c14.setArea(a1);



        Corner c21 = new Corner();
        c21.setLat(1.95F);
        c21.setLon(1F);
        cornerSet2.add(c21);
        c21.setArea(a2);

        Corner c22 = new Corner();
        c22.setLat(1.95F);
        c22.setLon(2F);
        cornerSet2.add(c22);
        c22.setArea(a2);

        Corner c23 = new Corner();
        c23.setLat(2.95F);
        c23.setLon(2F);
        cornerSet2.add(c23);
        c23.setArea(a2);

        Corner c24 = new Corner();
        c24.setLat(2.95F);
        c24.setLon(1F);
        cornerSet2.add(c24);
        c24.setArea(a2);


        a1.setCorners(cornerSet1);
        a2.setCorners(cornerSet2);

        System.out.println(a1.getCorners());
        System.out.println(a2.getCorners());


        log.debug("area {}", TestUtil.convertObjectToJsonBytes(a1));
        log.debug("area {}", TestUtil.convertObjectToJsonBytes(a2));

        log.debug("first insert begin");


        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(a1)))
            .andExpect(status().isCreated());

        log.debug("first insert finished");


        restAreaMockMvc.perform(post("/api/areas")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(a2)))
            .andExpect(status().isCreated());


    }
}
