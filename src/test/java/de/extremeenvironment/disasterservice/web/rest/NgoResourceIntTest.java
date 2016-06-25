package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.Ngo;
import de.extremeenvironment.disasterservice.repository.NGORepository;

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
 * Test class for the NGOResource REST controller.
 *
 * @see NGOResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class NgoResourceIntTest {


    @Inject
    private NGORepository nGORepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restNGOMockMvc;

    private Ngo nGO;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        NGOResource nGOResource = new NGOResource();
        ReflectionTestUtils.setField(nGOResource, "nGORepository", nGORepository);
        this.restNGOMockMvc = MockMvcBuilders.standaloneSetup(nGOResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        nGO = new Ngo();
    }

    @Test
    @Transactional
    public void createNGO() throws Exception {
        int databaseSizeBeforeCreate = nGORepository.findAll().size();

        // Create the Ngo

        restNGOMockMvc.perform(post("/api/n-gos")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(nGO)))
                .andExpect(status().isCreated());

        // Validate the Ngo in the database
        List<Ngo> nGOS = nGORepository.findAll();
        assertThat(nGOS).hasSize(databaseSizeBeforeCreate + 1);
        Ngo testNgo = nGOS.get(nGOS.size() - 1);
    }

    @Test
    @Transactional
    public void getAllNGOS() throws Exception {
        // Initialize the database
        nGORepository.saveAndFlush(nGO);

        // Get all the nGOS
        restNGOMockMvc.perform(get("/api/n-gos?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(nGO.getId().intValue())));
    }

    @Test
    @Transactional
    public void getNGO() throws Exception {
        // Initialize the database
        nGORepository.saveAndFlush(nGO);

        // Get the nGO
        restNGOMockMvc.perform(get("/api/n-gos/{id}", nGO.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(nGO.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingNGO() throws Exception {
        // Get the nGO
        restNGOMockMvc.perform(get("/api/n-gos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNGO() throws Exception {
        // Initialize the database
        nGORepository.saveAndFlush(nGO);
        int databaseSizeBeforeUpdate = nGORepository.findAll().size();

        // Update the nGO
        Ngo updatedNgo = new Ngo();
        updatedNgo.setId(nGO.getId());

        restNGOMockMvc.perform(put("/api/n-gos")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedNgo)))
                .andExpect(status().isOk());

        // Validate the Ngo in the database
        List<Ngo> nGOS = nGORepository.findAll();
        assertThat(nGOS).hasSize(databaseSizeBeforeUpdate);
        Ngo testNgo = nGOS.get(nGOS.size() - 1);
    }

    @Test
    @Transactional
    public void deleteNGO() throws Exception {
        // Initialize the database
        nGORepository.saveAndFlush(nGO);
        int databaseSizeBeforeDelete = nGORepository.findAll().size();

        // Get the nGO
        restNGOMockMvc.perform(delete("/api/n-gos/{id}", nGO.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Ngo> nGOS = nGORepository.findAll();
        assertThat(nGOS).hasSize(databaseSizeBeforeDelete - 1);
    }
}
