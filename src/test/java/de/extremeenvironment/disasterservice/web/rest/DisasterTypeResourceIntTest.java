package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.DisasterType;
import de.extremeenvironment.disasterservice.repository.DisasterTypeRepository;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the DisasterTypeResource REST controller.
 *
 * @see DisasterTypeResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class DisasterTypeResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";

    @Inject
    private DisasterTypeRepository disasterTypeRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restDisasterTypeMockMvc;

    private DisasterType disasterType;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DisasterTypeResource disasterTypeResource = new DisasterTypeResource();
        ReflectionTestUtils.setField(disasterTypeResource, "disasterTypeRepository", disasterTypeRepository);
        this.restDisasterTypeMockMvc = MockMvcBuilders.standaloneSetup(disasterTypeResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        disasterType = new DisasterType();
        disasterType.setName(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createDisasterType() throws Exception {
        int databaseSizeBeforeCreate = disasterTypeRepository.findAll().size();

        // Create the DisasterType

        restDisasterTypeMockMvc.perform(post("/api/disaster-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(disasterType)))
            .andExpect(status().isCreated());

        // Validate the DisasterType in the database
        List<DisasterType> disasterTypes = disasterTypeRepository.findAll();
        assertThat(disasterTypes).hasSize(databaseSizeBeforeCreate + 1);
        DisasterType testDisasterType = disasterTypes.get(disasterTypes.size() - 1);
        assertThat(testDisasterType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void getAllDisasterTypes() throws Exception {
        // Initialize the database
        disasterTypeRepository.saveAndFlush(disasterType);

        // Get all the disasterTypes
        restDisasterTypeMockMvc.perform(get("/api/disaster-types?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(disasterType.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getDisasterType() throws Exception {
        // Initialize the database
        disasterTypeRepository.saveAndFlush(disasterType);

        // Get the disasterType
        restDisasterTypeMockMvc.perform(get("/api/disaster-types/{id}", disasterType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(disasterType.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingDisasterType() throws Exception {
        // Get the disasterType
        restDisasterTypeMockMvc.perform(get("/api/disaster-types/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDisasterType() throws Exception {
        // Initialize the database
        disasterTypeRepository.saveAndFlush(disasterType);
        int databaseSizeBeforeUpdate = disasterTypeRepository.findAll().size();

        // Update the disasterType
        DisasterType updatedDisasterType = new DisasterType();
        updatedDisasterType.setId(disasterType.getId());
        updatedDisasterType.setName(UPDATED_NAME);

        restDisasterTypeMockMvc.perform(put("/api/disaster-types")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedDisasterType)))
            .andExpect(status().isOk());

        // Validate the DisasterType in the database
        List<DisasterType> disasterTypes = disasterTypeRepository.findAll();
        assertThat(disasterTypes).hasSize(databaseSizeBeforeUpdate);
        DisasterType testDisasterType = disasterTypes.get(disasterTypes.size() - 1);
        assertThat(testDisasterType.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void deleteDisasterType() throws Exception {
        // Initialize the database
        disasterTypeRepository.saveAndFlush(disasterType);
        int databaseSizeBeforeDelete = disasterTypeRepository.findAll().size();

        // Get the disasterType
        restDisasterTypeMockMvc.perform(delete("/api/disaster-types/{id}", disasterType.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<DisasterType> disasterTypes = disasterTypeRepository.findAll();
        assertThat(disasterTypes).hasSize(databaseSizeBeforeDelete - 1);
    }

}


