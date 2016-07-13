package de.extremeenvironment.disasterservice.web.rest;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.client.UserService;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.ActionObjectRepository;
import de.extremeenvironment.disasterservice.repository.ActionRepository;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.repository.UserRepository;
import de.extremeenvironment.disasterservice.service.DisasterService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import util.WithMockOAuth2Authentication;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the Matching algorithm.
 *
 * @see de.extremeenvironment.disasterservice.service.ActionService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DisasterServiceApp.class)
@WebIntegrationTest({
    "spring.profiles.active:test",
    "server.port:0"
})

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MatchingIntTest {

    private static User user;

    @Inject
    private WebApplicationContext context;

    @Inject
    private ActionRepository actionRepository;

    @Inject
    private ActionObjectRepository actionObjectRepository;

    @Inject
    private MessageClient messageClient;

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

    private List<Action> matchingActions = new LinkedList<>();



    private MockMvc restActionMockMvc;

//    private Action action1Seek, action1Offer, action2Seek, action2Offer;

    ActionObject actObj1, actObj2;

    private Disaster disaster;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Action action1Seek;
    private Action action2Offer;


    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);


        this.restActionMockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        Disaster d1 = new Disaster();
        d1.setLat(23F);
        d1.setLon(23F);
        disasterRepository.saveAndFlush(d1);
        Disaster d2 = new Disaster();
        d2.setLat(24F);
        d2.setLon(24F);
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
    @WithMockOAuth2Authentication(username = "user1", scope = "web-app")
    public void correctMatch() throws Exception {
        action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));

        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));
        User fortyTwo = userRepository.save(new User(42));
        action1Seek.setUser(fortyTwo);
        actionRepository.saveAndFlush(action1Seek);


        action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
//        action2Offer.setDisaster(disasters.get(disasters.size()-1));
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)))
            .andExpect(status().isCreated())
        ;


        List<Action> results = actionRepository.findAll();
        action2Offer = results.stream()
            .filter(action -> action.getActionType() == ActionType.OFFER)
            .findFirst()
            .orElse(action2Offer);

        assertTrue(action2Offer.getMatch().equals(action1Seek));
        assertTrue(action1Seek.getMatch().equals(action2Offer));
    }


    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void actionsDifferentActionObjectTypes() throws Exception {
        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 2));

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasters.get(disasters.size() - 1));
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        List<Action> results = actionRepository.findAll();

        assertTrue(results.get(results.size() - 2).getMatch() == null);
        assertTrue(results.get(results.size() - 1).getMatch() == null);

    }


    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void actionsTooMuchDistance() throws Exception {
        Action action1Seek = new Action();
        action1Seek.setLat(0F);
        action1Seek.setLon(0F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action1Seek)));


        Action action2Offer = new Action();
        action2Offer.setLat(0F);
        action2Offer.setLon(1F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasters.get(disasters.size() - 1));
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action2Offer)));


        List<Action> results = actionRepository.findAll();

        assertTrue(results.get(results.size() - 2).getMatch() == null);
        assertTrue(results.get(results.size() - 1).getMatch() == null);


    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void matchAlreadySet() throws Exception {

        User fortyTwo = userRepository.save(new User(42));
        User fortyTree = userRepository.save(new User(43));

        Action action1Seek = new Action();
        action1Seek.setLat(1F);
        action1Seek.setLon(1F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        List<ActionObject> actionObjects = actionObjectRepository.findAll();
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action1Seek.setUser(fortyTwo);

        action1Seek = actionRepository.saveAndFlush(action1Seek);

        Action action2Offer = new Action();
        action2Offer.setLat(1.005F);
        action2Offer.setLon(1.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
        action2Offer.setDisaster(disasters.get(disasters.size() - 1));

        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action2Offer.setUser(fortyTree);

        action2Offer = actionRepository.saveAndFlush(action2Offer);

        Action action3Seek = new Action();
        action3Seek.setLat(1.005F);
        action3Seek.setLon(1.005F);
        action3Seek.setIsExpired(false);
        action3Seek.setActionType(ActionType.OFFER);
        action3Seek.setDisaster(disasters.get(disasters.size() - 1));
        action3Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));


        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action3Seek)));


        List<Action> results = actionRepository.findAll();

        assertTrue(results.get(results.size() - 3).getMatch().equals(results.get(results.size() - 1)));
        assertTrue(results.get(results.size() - 1).getMatch().equals(results.get(results.size() - 3)));
        assertTrue(results.get(results.size() - 2).getMatch() == null);

    }

    @Test
    @Transactional
    @WithMockOAuth2Authentication(scope = "web-app")
    public void testRejectingMatch() throws Exception {

        User fortyTwo = userRepository.save(new User(42));
        User fortyTree = userRepository.save(new User(43));


        List<ActionObject> actionObjects = actionObjectRepository.findAll();

//        for (Action a : actionRepository.findAll()) {
//            actionRepository.delete(a);
//        }

        Action action1Seek = new Action();
        action1Seek.setLat(2F);
        action1Seek.setLon(2F);
        action1Seek.setIsExpired(false);
        action1Seek.setActionType(ActionType.SEEK);
        List<Disaster> disasters = disasterRepository.findAll();
        action1Seek.setDisaster(disasters.get(disasters.size() - 1));
        action1Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action1Seek.setUser(fortyTwo);


        Action action2Offer = new Action();
        action2Offer.setLat(2.005F);
        action2Offer.setLon(2.005F);
        action2Offer.setIsExpired(false);
        action2Offer.setActionType(ActionType.OFFER);
//        action2Offer.setDisaster(disasters.get(disasters.size()-1));
        action2Offer.addActionObject(actionObjects.get(actionObjects.size() - 1));
        action2Offer.setUser(fortyTree);

        Action action3Seek = new Action();
        action3Seek.setLat(2.005F);
        action3Seek.setLon(2.005F);
        action3Seek.setIsExpired(false);
        action3Seek.setActionType(ActionType.SEEK);
        action3Seek.setDisaster(disasters.get(disasters.size() - 1));
        action3Seek.addActionObject(actionObjects.get(actionObjects.size() - 1));


        actionRepository.saveAndFlush(action1Seek);
        actionRepository.saveAndFlush(action2Offer);

        restActionMockMvc.perform(post("/api/actions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(action3Seek)));


        List<Action> results = actionRepository.findAll();
//        results.forEach(r -> System.out.println(r + " : " + r.getMatch()));


        assertTrue(results.get(results.size() - 2).getMatch().equals(results.get(results.size() - 1)));
        assertTrue(results.get(results.size() - 1).getMatch().equals(results.get(results.size() - 2)));
        assertTrue(results.get(results.size() - 3).getMatch() == null);


        restActionMockMvc.perform(put("/api/actions/{id}/rejectMatch", results.get(results.size() - 1).getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());

        results = actionRepository.findAll();
//        results.forEach(r -> System.out.println(r + " : " + r.getMatch()));

        assertTrue(results.get(results.size() - 1).getMatch() == null);
        assertTrue(results.get(results.size() - 3).getMatch().equals(results.get(results.size() - 2)));
        assertTrue(results.get(results.size() - 2).getMatch().equals(results.get(results.size() - 3)));

        assertTrue(results.get(results.size() - 1).getRejectedMatches().contains(results.get(results.size() - 2)));
        assertTrue(results.get(results.size() - 2).getRejectedMatches().contains(results.get(results.size() - 1)));


    }
}
