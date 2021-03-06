package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.Area;
import de.extremeenvironment.disasterservice.domain.Corner;
import de.extremeenvironment.disasterservice.repository.AreaRepository;
import de.extremeenvironment.disasterservice.repository.CornerRepository;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the CornerResource REST controller.
 *
 * @see CornerResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class CornerResourceIntTest {


    private static final Float DEFAULT_LAT = 1F;
    private static final Float UPDATED_LAT = 2F;

    private static final Float DEFAULT_LON = 1F;
    private static final Float UPDATED_LON = 2F;

    @Inject
    private CornerRepository cornerRepository;

    @Inject
    private AreaRepository areaRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restCornerMockMvc;

    private Corner corner;

    private Area a;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CornerResource cornerResource = new CornerResource();
        ReflectionTestUtils.setField(cornerResource, "cornerRepository", cornerRepository);
        this.restCornerMockMvc = MockMvcBuilders.standaloneSetup(cornerResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        a = new Area();

        corner = new Corner();
        corner.setLat(DEFAULT_LAT);
        corner.setLon(DEFAULT_LON);
        corner.setArea(a);

        Set<Corner> cornerList = new HashSet<>();
        cornerList.add(corner);
        a.setCorners(cornerList);
    }

    @Test
    @Transactional
    public void createCorner() throws Exception {
        int databaseSizeBeforeCreate = cornerRepository.findAll().size();

        // Create the Corner

        restCornerMockMvc.perform(post("/api/corners")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(corner)))
                .andExpect(status().isCreated());

        // Validate the Corner in the database
        List<Corner> corners = cornerRepository.findAll();
        assertThat(corners).hasSize(databaseSizeBeforeCreate + 1);
        Corner testCorner = corners.get(corners.size() - 1);
        assertThat(testCorner.getLat()).isEqualTo(DEFAULT_LAT);
        assertThat(testCorner.getLon()).isEqualTo(DEFAULT_LON);
    }

    @Test
    @Transactional
    public void getAllCorners() throws Exception {
        // Initialize the database
        cornerRepository.saveAndFlush(corner);

        // Get all the corners
        restCornerMockMvc.perform(get("/api/corners?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(corner.getId().intValue())))
                .andExpect(jsonPath("$.[*].lat").value(hasItem(DEFAULT_LAT.doubleValue())))
                .andExpect(jsonPath("$.[*].lon").value(hasItem(DEFAULT_LON.doubleValue())));
    }

    @Test
    @Transactional
    public void getCorner() throws Exception {
        // Initialize the database
        cornerRepository.saveAndFlush(corner);

        // Get the corner
        restCornerMockMvc.perform(get("/api/corners/{id}", corner.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(corner.getId().intValue()))
            .andExpect(jsonPath("$.lat").value(DEFAULT_LAT.doubleValue()))
            .andExpect(jsonPath("$.lon").value(DEFAULT_LON.doubleValue()));
    }

    @Test
    @Transactional
    public void getNonExistingCorner() throws Exception {
        // Get the corner
        restCornerMockMvc.perform(get("/api/corners/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCorner() throws Exception {
        // Initialize the database
        cornerRepository.saveAndFlush(corner);
        int databaseSizeBeforeUpdate = cornerRepository.findAll().size();

        // Update the corner
        Corner updatedCorner = new Corner();
        updatedCorner.setId(corner.getId());
        updatedCorner.setLat(UPDATED_LAT);
        updatedCorner.setLon(UPDATED_LON);
        updatedCorner.setArea(a);

        restCornerMockMvc.perform(put("/api/corners")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedCorner)))
                .andExpect(status().isOk());

        // Validate the Corner in the database
        List<Corner> corners = cornerRepository.findAll();
        assertThat(corners).hasSize(databaseSizeBeforeUpdate);
        Corner testCorner = corners.get(corners.size() - 1);
        assertThat(testCorner.getLat()).isEqualTo(UPDATED_LAT);
        assertThat(testCorner.getLon()).isEqualTo(UPDATED_LON);
    }

    @Test
    @Transactional
    public void deleteCorner() throws Exception {
        // Initialize the database
        cornerRepository.saveAndFlush(corner);
        int databaseSizeBeforeDelete = cornerRepository.findAll().size();

        // Get the corner
        restCornerMockMvc.perform(delete("/api/corners/{id}", corner.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Corner> corners = cornerRepository.findAll();
        assertThat(corners).hasSize(databaseSizeBeforeDelete - 1);
    }
}
