package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the DisasterResource REST controller.
 *
 * @see DisasterResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class DisasterResourceIntTest {


    private static final Boolean DEFAULT_IS_EXPIRED = false;
    private static final Boolean UPDATED_IS_EXPIRED = true;

    private static final Long DEFAULT_LAT = 1L;
    private static final Long UPDATED_LAT = 2L;

    private static final Long DEFAULT_LON = 1L;
    private static final Long UPDATED_LON = 2L;

    @Inject
    private DisasterRepository disasterRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restDisasterMockMvc;

    private Disaster disaster;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DisasterResource disasterResource = new DisasterResource();
        ReflectionTestUtils.setField(disasterResource, "disasterRepository", disasterRepository);
        this.restDisasterMockMvc = MockMvcBuilders.standaloneSetup(disasterResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        disaster = new Disaster();
        disaster.setIsExpired(DEFAULT_IS_EXPIRED);
        disaster.setLat(DEFAULT_LAT);
        disaster.setLon(DEFAULT_LON);
    }

    @Test
    @Transactional
    public void createDisaster() throws Exception {
        int databaseSizeBeforeCreate = disasterRepository.findAll().size();

        // Create the Disaster

        restDisasterMockMvc.perform(post("/api/disasters")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(disaster)))
                .andExpect(status().isCreated());

        // Validate the Disaster in the database
        List<Disaster> disasters = disasterRepository.findAll();
        assertThat(disasters).hasSize(databaseSizeBeforeCreate + 1);
        Disaster testDisaster = disasters.get(disasters.size() - 1);
        assertThat(testDisaster.isIsExpired()).isEqualTo(DEFAULT_IS_EXPIRED);
        assertThat(testDisaster.getLat()).isEqualTo(DEFAULT_LAT);
        assertThat(testDisaster.getLon()).isEqualTo(DEFAULT_LON);
    }

    @Test
    @Transactional
    public void getAllDisasters() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);

        // Get all the disasters
        restDisasterMockMvc.perform(get("/api/disasters?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(disaster.getId().intValue())))
                .andExpect(jsonPath("$.[*].isExpired").value(hasItem(DEFAULT_IS_EXPIRED.booleanValue())))
                .andExpect(jsonPath("$.[*].lat").value(hasItem(DEFAULT_LAT.intValue())))
                .andExpect(jsonPath("$.[*].lon").value(hasItem(DEFAULT_LON.intValue())));
    }

    @Test
    @Transactional
    public void getDisaster() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);

        // Get the disaster
        restDisasterMockMvc.perform(get("/api/disasters/{id}", disaster.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(disaster.getId().intValue()))
            .andExpect(jsonPath("$.isExpired").value(DEFAULT_IS_EXPIRED.booleanValue()))
            .andExpect(jsonPath("$.lat").value(DEFAULT_LAT.intValue()))
            .andExpect(jsonPath("$.lon").value(DEFAULT_LON.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingDisaster() throws Exception {
        // Get the disaster
        restDisasterMockMvc.perform(get("/api/disasters/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDisaster() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);
        int databaseSizeBeforeUpdate = disasterRepository.findAll().size();

        // Update the disaster
        Disaster updatedDisaster = new Disaster();
        updatedDisaster.setId(disaster.getId());
        updatedDisaster.setIsExpired(UPDATED_IS_EXPIRED);
        updatedDisaster.setLat(UPDATED_LAT);
        updatedDisaster.setLon(UPDATED_LON);

        restDisasterMockMvc.perform(put("/api/disasters")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedDisaster)))
                .andExpect(status().isOk());

        // Validate the Disaster in the database
        List<Disaster> disasters = disasterRepository.findAll();
        assertThat(disasters).hasSize(databaseSizeBeforeUpdate);
        Disaster testDisaster = disasters.get(disasters.size() - 1);
        assertThat(testDisaster.isIsExpired()).isEqualTo(UPDATED_IS_EXPIRED);
        assertThat(testDisaster.getLat()).isEqualTo(UPDATED_LAT);
        assertThat(testDisaster.getLon()).isEqualTo(UPDATED_LON);
    }

    @Test
    @Transactional
    public void deleteDisaster() throws Exception {
        // Initialize the database
        disasterRepository.saveAndFlush(disaster);
        int databaseSizeBeforeDelete = disasterRepository.findAll().size();

        // Get the disaster
        restDisasterMockMvc.perform(delete("/api/disasters/{id}", disaster.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Disaster> disasters = disasterRepository.findAll();
        assertThat(disasters).hasSize(databaseSizeBeforeDelete - 1);
    }
}
