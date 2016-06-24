package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionObjectRepository;
import de.extremeenvironment.disasterservice.repository.ActionRepository;

import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Test class for the Matching algorithm.
 *
 * @see de.extremeenvironment.disasterservice.service.ActionService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebAppConfiguration
@IntegrationTest
public class MatchingIntTest {

    private static User user;

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


    private MockMvc restActionMockMvc;

//    private Action action1Seek, action1Offer, action2Seek, action2Offer;

    ActionObject actObj1, actObj2;

    private Disaster disaster;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ActionResource actionResource = new ActionResource(actionRepository, disasterRepository);
        ReflectionTestUtils.setField(actionResource, "actionRepository", actionRepository);
        this.restActionMockMvc = MockMvcBuilders.standaloneSetup(actionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    @Transactional
    public void initTest() {
        userRepository.saveAndFlush(new User());
        Disaster d1 = new Disaster();
        d1.setLat(23F);
        d1.setLon(23F);
        disasterRepository.saveAndFlush(d1);
        Disaster d2 = new Disaster();
        d2.setLat(23F);
        d2.setLon(23F);
        disasterRepository.saveAndFlush(d2);


        ActionObject actObj1 = new ActionObject();
        actObj1.setName("AAAA");
        actionObjectRepository.saveAndFlush(actObj1);

        ActionObject actObj2 = new ActionObject();
        actObj2.setName("BBBB");
        actionObjectRepository.saveAndFlush(actObj2);

    }

    @Test
    @Transactional
    public void correctMatch() throws Exception {
        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        action1Seek.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet1 = action1Seek.getActionObjects();
        aoSet1.add(actionObjectRepository.findAll().get(0));
        action1Seek.setActionObjects(aoSet1);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet2 = action2Offer.getActionObjects();
        aoSet2.add(actionObjectRepository.findAll().get(0));
        action2Offer.setActionObjects(aoSet2);


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        List<Action> results = actionRepository.findAll();

        assertThat(results.get(0).getMatch().equals(results.get(1)));
        assertThat(results.get(1).getMatch().equals(results.get(0)));

        actionRepository.delete(results.get(0));
        actionRepository.delete(results.get(1));
    }

    @Test
    @Transactional
    public void actionsInDifferentDisasters() throws Exception {
        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        action1Seek.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet1 = action1Seek.getActionObjects();
        aoSet1.add(actionObjectRepository.findAll().get(0));
        action1Seek.setActionObjects(aoSet1);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasterRepository.findAll().get(1));
        Set<ActionObject> aoSet2 = action2Offer.getActionObjects();
        aoSet2.add(actionObjectRepository.findAll().get(0));
        action2Offer.setActionObjects(aoSet2);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        List<Action> results = actionRepository.findAll();

        assertThat(results.get(0).getMatch() == null);
        assertThat(results.get(1).getMatch() == null);

        actionRepository.delete(results.get(0));
        actionRepository.delete(results.get(1));
    }

    @Test
    @Transactional
    public void actionsDifferentActionObjectTypes() throws Exception {
        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        action1Seek.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet1 = action1Seek.getActionObjects();
        aoSet1.add(actionObjectRepository.findAll().get(0));
        action1Seek.setActionObjects(aoSet1);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet2 = action2Offer.getActionObjects();
        aoSet2.add(actionObjectRepository.findAll().get(1));
        action2Offer.setActionObjects(aoSet2);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        List<Action> results = actionRepository.findAll();

        assertThat(results.get(0).getMatch() == null);
        assertThat(results.get(1).getMatch() == null);

        actionRepository.delete(results.get(0));
        actionRepository.delete(results.get(1));

    }

    @Test
    @Transactional
    public void matchAlreadySet() throws Exception {
        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        action1Seek.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet1 = action1Seek.getActionObjects();
        aoSet1.add(actionObjectRepository.findAll().get(0));
        action1Seek.setActionObjects(aoSet1);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet2 = action2Offer.getActionObjects();
        aoSet2.add(actionObjectRepository.findAll().get(0));
        action2Offer.setActionObjects(aoSet2);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        Action action3Seek = new Action();
        action3Seek.setLat(1.005F);
        action3Seek.setLon(1.005F);
        action3Seek.setIsExpired(false);
        action3Seek.setActionType(ActionType.OFFER);
        action3Seek.setDisaster(disasterRepository.findAll().get(0));
        Set<ActionObject> aoSet3 = action3Seek.getActionObjects();
        aoSet3.add(actionObjectRepository.findAll().get(0));
        action3Seek.setActionObjects(aoSet3);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action3Seek)));


        List<Action> results = actionRepository.findAll();

        assertThat(results.get(0).getMatch().equals(results.get(1)));
        assertThat(results.get(1).getMatch().equals(results.get(0)));
        assertThat(results.get(2).getMatch() == null);


        actionRepository.delete(results.get(0));
        actionRepository.delete(results.get(1));
        actionRepository.delete(results.get(2));
    }
}
