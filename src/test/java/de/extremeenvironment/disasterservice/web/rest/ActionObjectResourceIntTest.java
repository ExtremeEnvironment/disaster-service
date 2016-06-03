package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.repository.ActionObjectRepository;

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
 * Test class for the ActionObjectResource REST controller.
 *
 * @see ActionObjectResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class ActionObjectResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAA";
    private static final String UPDATED_NAME = "BBBBB";

    @Inject
    private ActionObjectRepository actionObjectRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restActionObjectMockMvc;

    private ActionObject actionObject;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ActionObjectResource actionObjectResource = new ActionObjectResource();
        ReflectionTestUtils.setField(actionObjectResource, "actionObjectRepository", actionObjectRepository);
        this.restActionObjectMockMvc = MockMvcBuilders.standaloneSetup(actionObjectResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        actionObject = new ActionObject();
        actionObject.setName(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createActionObject() throws Exception {
        int databaseSizeBeforeCreate = actionObjectRepository.findAll().size();

        // Create the ActionObject

        restActionObjectMockMvc.perform(post("/api/action-objects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(actionObject)))
                .andExpect(status().isCreated());

        // Validate the ActionObject in the database
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        assertThat(actionObjects).hasSize(databaseSizeBeforeCreate + 1);
        ActionObject testActionObject = actionObjects.get(actionObjects.size() - 1);
        assertThat(testActionObject.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void getAllActionObjects() throws Exception {
        // Initialize the database
        actionObjectRepository.saveAndFlush(actionObject);

        // Get all the actionObjects
        restActionObjectMockMvc.perform(get("/api/action-objects?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(actionObject.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getActionObject() throws Exception {
        // Initialize the database
        actionObjectRepository.saveAndFlush(actionObject);

        // Get the actionObject
        restActionObjectMockMvc.perform(get("/api/action-objects/{id}", actionObject.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(actionObject.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingActionObject() throws Exception {
        // Get the actionObject
        restActionObjectMockMvc.perform(get("/api/action-objects/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateActionObject() throws Exception {
        // Initialize the database
        actionObjectRepository.saveAndFlush(actionObject);
        int databaseSizeBeforeUpdate = actionObjectRepository.findAll().size();

        // Update the actionObject
        ActionObject updatedActionObject = new ActionObject();
        updatedActionObject.setId(actionObject.getId());
        updatedActionObject.setName(UPDATED_NAME);

        restActionObjectMockMvc.perform(put("/api/action-objects")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedActionObject)))
                .andExpect(status().isOk());

        // Validate the ActionObject in the database
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        assertThat(actionObjects).hasSize(databaseSizeBeforeUpdate);
        ActionObject testActionObject = actionObjects.get(actionObjects.size() - 1);
        assertThat(testActionObject.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void deleteActionObject() throws Exception {
        // Initialize the database
        actionObjectRepository.saveAndFlush(actionObject);
        int databaseSizeBeforeDelete = actionObjectRepository.findAll().size();

        // Get the actionObject
        restActionObjectMockMvc.perform(delete("/api/action-objects/{id}", actionObject.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        assertThat(actionObjects).hasSize(databaseSizeBeforeDelete - 1);
    }
}
