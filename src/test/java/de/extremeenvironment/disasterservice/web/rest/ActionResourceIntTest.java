package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.client.UserService;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionObjectRepository;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import de.extremeenvironment.disasterservice.service.DisasterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import util.WithMockOAuth2Authentication;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ActionResource REST controller.
 *
 * @see ActionResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebIntegrationTest({
    "spring.profiles.active:test",
    "server.port:0"
})
public class ActionResourceIntTest {


    private static final Float DEFAULT_LAT = 1F;
    private static final Float UPDATED_LAT = 2F;

    private static final Float DEFAULT_LON = 1F;
    private static final Float UPDATED_LON = 2F;

    private static final Boolean DEFAULT_IS_EXPIRED = false;
    private static final Boolean UPDATED_IS_EXPIRED = true;

    private static final ActionType DEFAULT_ACTION_TYPE = ActionType.OFFER;
    private static final ActionType UPDATED_ACTION_TYPE = ActionType.SEEK;

    private static User user;

    @Inject
    private WebApplicationContext context;


    @Inject
    private ActionRepository actionRepository;

    @Inject
    private ActionObjectRepository actionObjectRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private UserRepository userRepository;

    @Inject
    private DisasterRepository disasterRepository;

    @Inject
    private DisasterService disasterService;

    @Inject
    private UserService userService;

    MessageClient messageClient;


    private MockMvc restActionMockMvc;

    private Action action;

    private Disaster disaster;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ActionResource actionResource = new ActionResource(
            actionRepository,
            disasterRepository,
            messageClient,
            disasterService,
            userService
        );
        this.restActionMockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Before
    public void initTest() {
        action = new Action();
        action.setLat(DEFAULT_LAT);
        action.setLon(DEFAULT_LON);
        action.setIsExpired(DEFAULT_IS_EXPIRED);
        action.setActionType(DEFAULT_ACTION_TYPE);

        user = new User();
        userRepository.saveAndFlush(user);
        disaster = new Disaster();
        disaster.setLat(64F);
        disaster.setLon(64F);
        disasterRepository.saveAndFlush(disaster);


    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void createAction() throws Exception {
        int databaseSizeBeforeCreate = actionRepository.findAll().size();

        // Create the Action

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action)))
            .andExpect(status().isCreated());

        // Validate the Action in the database
        List<Action> actions = actionRepository.findAll();
        assertThat(actions).hasSize(databaseSizeBeforeCreate + 1);
        Action testAction = actions.get(actions.size() - 1);
        assertThat(testAction.getLat()).isEqualTo(DEFAULT_LAT);
        assertThat(testAction.getLon()).isEqualTo(DEFAULT_LON);
        assertThat(testAction.isIsExpired()).isEqualTo(DEFAULT_IS_EXPIRED);
        assertThat(testAction.getActionType()).isEqualTo(DEFAULT_ACTION_TYPE);
        // assertThat(testAction.getUser().getId()).isEqualTo(user.getId());
        //  user.getActions().add(action);
        System.out.println(action.toString());
        actionRepository.findAll().forEach(a -> System.out.println(a + " : " + a.getUser()));
        System.out.println(user.toString());

    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void checkLatIsRequired() throws Exception {
        int databaseSizeBeforeTest = actionRepository.findAll().size();
        // set the field null
        action.setLat(null);

        // Create the Action, which fails.

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action)))
            .andExpect(status().isBadRequest());

        List<Action> actions = actionRepository.findAll();
        assertThat(actions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void checkLonIsRequired() throws Exception {
        int databaseSizeBeforeTest = actionRepository.findAll().size();
        // set the field null
        action.setLon(null);

        // Create the Action, which fails.

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action)))
            .andExpect(status().isBadRequest());

        List<Action> actions = actionRepository.findAll();
        assertThat(actions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void checkActionTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = actionRepository.findAll().size();
        // set the field null
        action.setActionType(null);

        // Create the Action, which fails.

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action)))
            .andExpect(status().isBadRequest());

        List<Action> actions = actionRepository.findAll();
        assertThat(actions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void getAllActions() throws Exception {
        // Initialize the database
        actionRepository.saveAndFlush(action);

        // Get all the actions
        restActionMockMvc.perform(get("/api/actions?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(action.getId().intValue())))
            .andExpect(jsonPath("$.[*].lat").value(hasItem(DEFAULT_LAT.doubleValue())))
            .andExpect(jsonPath("$.[*].lon").value(hasItem(DEFAULT_LON.doubleValue())))
            .andExpect(jsonPath("$.[*].isExpired").value(hasItem(DEFAULT_IS_EXPIRED.booleanValue())))
            .andExpect(jsonPath("$.[*].actionType").value(hasItem(DEFAULT_ACTION_TYPE.toString())));
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void getAction() throws Exception {
        // Initialize the database
        actionRepository.saveAndFlush(action);

        // Get the action
        restActionMockMvc.perform(get("/api/actions/{id}", action.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(action.getId().intValue()))
            .andExpect(jsonPath("$.lat").value(DEFAULT_LAT.doubleValue()))
            .andExpect(jsonPath("$.lon").value(DEFAULT_LON.doubleValue()))
            .andExpect(jsonPath("$.isExpired").value(DEFAULT_IS_EXPIRED.booleanValue()))
            .andExpect(jsonPath("$.actionType").value(DEFAULT_ACTION_TYPE.toString()));
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void getNonExistingAction() throws Exception {
        // Get the action
        restActionMockMvc.perform(get("/api/actions/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void updateAction() throws Exception {
        // Initialize the database
        actionRepository.saveAndFlush(action);
        int databaseSizeBeforeUpdate = actionRepository.findAll().size();

        // Update the action
        Action updatedAction = new Action();
        updatedAction.setId(action.getId());
        updatedAction.setLat(UPDATED_LAT);
        updatedAction.setLon(UPDATED_LON);
        updatedAction.setIsExpired(UPDATED_IS_EXPIRED);
        updatedAction.setActionType(UPDATED_ACTION_TYPE);

        restActionMockMvc.perform(put("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAction)))
            .andExpect(status().isOk());

        // Validate the Action in the database
        List<Action> actions = actionRepository.findAll();
        assertThat(actions).hasSize(databaseSizeBeforeUpdate);
        Action testAction = actions.get(actions.size() - 1);
        assertThat(testAction.getLat()).isEqualTo(UPDATED_LAT);
        assertThat(testAction.getLon()).isEqualTo(UPDATED_LON);
        assertThat(testAction.isIsExpired()).isEqualTo(UPDATED_IS_EXPIRED);
        assertThat(testAction.getActionType()).isEqualTo(UPDATED_ACTION_TYPE);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void deleteAction() throws Exception {
        // Initialize the database
        actionRepository.saveAndFlush(action);
        int databaseSizeBeforeDelete = actionRepository.findAll().size();
        System.out.println(action);
        //  user.getActions().add(action);
        userRepository.save(user);
        System.out.println(user);
        // Get the action
        restActionMockMvc.perform(delete("/api/actions/{id}", action.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Action> actions = actionRepository.findAll();
        assertThat(actions).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void getActionByType() throws Exception {

        userRepository.saveAndFlush(user);

        Action action2 = new Action();
        action2.setLat(DEFAULT_LAT);
        action2.setLon(DEFAULT_LON);
        action2.setIsExpired(DEFAULT_IS_EXPIRED);
        action2.setActionType(ActionType.OFFER);
        action2.setUser(user);

        actionRepository.saveAndFlush(action2);


        action.setUser(user);
        actionRepository.saveAndFlush(action);

        restActionMockMvc.perform(get("/api/action/{userId}/{actionType}", user.getId(), ActionType.OFFER))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].actionType").value(hasItem(ActionType.OFFER.name())));




    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void testActionIsMatchWithCatastrophy() throws Exception {
        Float lat = 64F;
        Float lon = 64F;
        Action actionT = new Action();
        Action actionT2 = new Action();
        actionT2.setLat(84F);
        actionT2.setLon(84F);
        actionT2.setActionType(UPDATED_ACTION_TYPE);
        actionT2.setIsExpired(DEFAULT_IS_EXPIRED);
        actionT2.setUser(user);
        actionT.setLat(lat);
        actionT.setLon(lon);
        actionT.setActionType(UPDATED_ACTION_TYPE);
        actionT.setIsExpired(DEFAULT_IS_EXPIRED);
        actionT.setUser(user);
        System.out.println(actionT2.toString());


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(actionT)))
            .andExpect(status().isCreated());

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(actionT2)))
            .andExpect(status().isCreated());

        System.out.println(action.toString());

        List<Action> actions = actionRepository.findAll();

        Action testAction = actions.get(actions.size() - 2);
        Action testAction2 = actions.get(actions.size() - 1);


        assertTrue(testAction.getDisaster().equals(disaster));
        assertFalse(testAction2.getDisaster().equals(disaster));

    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void testLikes() throws Exception {

        List<Action>actions = actionRepository.findAll();

        int i = (int) (Math.random() * actions.size());

        Action action = actions.get(i);
        Long countBefore = action.getLikeCounter();

        restActionMockMvc.perform(put("/api/actions/{id}/likes", action.getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().is2xxSuccessful());

        assertThat(countBefore).isEqualTo(action.getLikeCounter() - 1);

        restActionMockMvc.perform(put("/api/actions/{id}/likes", -1000)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().is4xxClientError());

    }
    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void testActionKnowledgeByCatastrophe() throws Exception {

       Disaster disaster= disasterRepository.findAll().get(0);
        List<Action> actions =actionRepository.findActionByActionType(ActionType.KNOWLEDGE);
        System.out.println(disaster);

        restActionMockMvc.perform(get("/api/actions/{id}/knowledge", 3)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.[*].id").value(hasItem(actions.get(actions.size()-1).getId().intValue())))
            .andExpect(jsonPath("$.[*].id").value(hasItem(actions.get(actions.size()-2).getId().intValue())))
            .andExpect(jsonPath("$.[*].id").value(hasItem(actions.get(actions.size()-3).getId().intValue()))
            )
        ;
    }


}
