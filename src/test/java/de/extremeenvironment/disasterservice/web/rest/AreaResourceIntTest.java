package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.config.JHipsterProperties;
import de.extremeenvironment.disasterservice.domain.*;
import de.extremeenvironment.disasterservice.domain.Area;
import de.extremeenvironment.disasterservice.repository.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.hasItem;

import org.mockito.MockitoAnnotations;
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


    @Inject
    private AreaRepository areaRepository;

    @Inject
    private CornerRepository cornerRepository;

    @Inject
    private DisasterRepository disasterRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private NGORepository ngoRepository;

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
    }

    @Before
    public void initTest() {
        userRepository.saveAndFlush(new User());
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
//
//    @Test
//    @Transactional
//    public void createAreaWithCorners() throws Exception {
//        Set<Corner> cornerList = new HashSet<>();
//
//        Corner c11 = new Corner();
//        c11.setLat(1F);
//        c11.setLon(1F);
//        cornerList.add(c11);
////        cornerRepository.save(c11);
//
//        Corner c12 = new Corner();
//        c12.setLat(2F);
//        c12.setLon(1F);
//        cornerList.add(c12);
////        cornerRepository.save(c12);
//
//
//        Corner c13 = new Corner();
//        c13.setLat(2F);
//        c13.setLon(2F);
//        cornerList.add(c13);
////        cornerRepository.save(c13);
//
//
//        Corner c14 = new Corner();
//        c14.setLat(1F);
//        c14.setLon(2F);
//        cornerList.add(c14);
////        cornerRepository.save(c14);
//
//
//        Area a = new Area();
//        a.setCorners(cornerList);
//
//        System.out.println(a.getCorners());
//
//        System.out.println("\n\nArea: \n" + TestUtil.convertObjectToJsonBytes(a) + "\n\n");
//
//        restAreaMockMvc.perform(post("/api/actions")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content(TestUtil.convertObjectToJsonBytes(a)));
//
//        List<Area> results = areaRepository.findAll();
//
//        assertThat(results.get(0).getCorners().contains(c11));
//        assertThat(results.get(0).getCorners().contains(c12));
//        assertThat(results.get(0).getCorners().contains(c13));
//        assertThat(results.get(0).getCorners().contains(c14));
//
//    }
}
