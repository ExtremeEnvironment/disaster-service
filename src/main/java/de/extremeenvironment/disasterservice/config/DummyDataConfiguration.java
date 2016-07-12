package de.extremeenvironment.disasterservice.config;

import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.client.UserService;
import de.extremeenvironment.disasterservice.domain.*;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.*;
import de.extremeenvironment.disasterservice.service.ActionService;
import de.extremeenvironment.disasterservice.service.DisasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;

/**
 * Created by linus on 25.06.16.
 */
@Configuration
@Profile("!s2m")
public class DummyDataConfiguration {
    private static final int MAX_TRIES = 10;
    Logger log = LoggerFactory.getLogger(DummyDataConfiguration.class);

    @Inject
    DisasterTypeRepository disasterTypeRepository;

    @Inject
    DisasterRepository disasterRepository;

    @Inject
    DisasterService disasterService;

    @Inject
    MessageClient messageClient;

    @Inject
    ActionService actionService;

    @Inject
    ActionObjectRepository actionObjectRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    UserService userService;

    private boolean loaded = false;

    /**
     * during the application boot up several things may fail (timeouts, service discovery...)
     * this
     */
    public void interServiceCallWarmUp() throws InterruptedException {
        int counter = 0;

        while (counter < MAX_TRIES) {
            counter++;
            try {
                messageClient.getConversations();
                userService.findOrCreateById(1L);
            } catch (Exception e) {
                log.info("something failing '{}', repeat in 1.5s. Throw in {}", e.getMessage(), MAX_TRIES - counter);
                Thread.sleep(1500);
                if ((counter + 1) == MAX_TRIES) {
                    throw e;
                }
            }
        }
    }

    //HACK, aber funzt
    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    public void dataCreate() throws InterruptedException {
        if (loaded) {
            return;
        }
        loaded = true;

        log.info("starting data creation");
        if (actionService.findAll().size() == 0) {
            interServiceCallWarmUp();

            User anonymous = userService.findOrCreateById(2L); //adds "anonymous"
            User admin = userService.findOrCreateById(3L); //adds "admin"
            User user = userService.findOrCreateById(4L); //adds "user"


            Category category = new Category();
            Category category2 = new Category();
            Category category3 = new Category();
            Category category4 = new Category();
            Category category5 = new Category();
            Category category6 = new Category();
            category.setName("Nahrung");
            category2.setName("Waffen");
            category3.setName("Erste-Hilfe");
            category4.setName("Hilfsmittel");
            category5.setName("Baumittel");
            category6.setName("Anziehsachen");
            categoryRepository.save(category);
            categoryRepository.save(category2);
            categoryRepository.save(category3);
            categoryRepository.save(category4);
            categoryRepository.save(category5);
            categoryRepository.save(category6);
            categoryRepository.flush();

            DisasterType disType = new DisasterType();
            disType.setName("Erdbeben");
            DisasterType dT = new DisasterType();
            dT.setName("Überschwemmung");
            DisasterType dt = new DisasterType();
            dt.setName("Stromausfall");
            DisasterType disasterType = new DisasterType();
            disasterType.setName("Zombie-Angriff");
            disasterTypeRepository.saveAndFlush(disasterType);
            disasterTypeRepository.saveAndFlush(disType);
            disasterTypeRepository.saveAndFlush(dT);
            disasterTypeRepository.saveAndFlush(dt);
            ActionObject ao = new ActionObject();
            ao.setName("Schmerzmittel");
            ao.setCategory(category3);
            ActionObject ao1 = new ActionObject();
            ao1.setName("Holz");
            ao1.setCategory(category5);
            ActionObject ao2 = new ActionObject();
            ao2.setName("Generator");
            ao2.setCategory(category4);
            ActionObject ao3 = new ActionObject();
            ao3.setName("Verbandszeug");
            ao3.setCategory(category3);
            ActionObject ao4 = new ActionObject();
            ao4.setName("Rollstuhl");
            ao4.setCategory(category4);
            ActionObject ao5 = new ActionObject();
            ao5.setName("Standardessen");
            ao5.setCategory(category);
            ActionObject ao6 = new ActionObject();
            ao6.setName("Wasser");
            ao6.setCategory(category);
            ActionObject ao7 = new ActionObject();
            ao7.setName("Supplemente");
            ao7.setCategory(category3);
            ActionObject ao8 = new ActionObject();
            ao8.setName("Zelt");
            ao8.setCategory(category4);
            ActionObject ao9 = new ActionObject();
            ao9.setName("Betten");
            ao9.setCategory(category4);
            ActionObject ao10 = new ActionObject();
            ao10.setName("Jacken");
            ao10.setCategory(category6);
            ActionObject ao11 = new ActionObject();
            ao11.setName("Schrottflinte");
            ao11.setCategory(category2);
            actionObjectRepository.save(ao);
            actionObjectRepository.save(ao1);
            actionObjectRepository.save(ao2);
            actionObjectRepository.save(ao3);
            actionObjectRepository.save(ao4);
            actionObjectRepository.save(ao5);
            actionObjectRepository.save(ao6);
            actionObjectRepository.save(ao7);
            actionObjectRepository.save(ao8);
            actionObjectRepository.save(ao9);
            actionObjectRepository.save(ao10);
            actionObjectRepository.save(ao11);
            actionObjectRepository.flush();

            Disaster disaster = new Disaster();
            disaster.setLat(23F);
            disaster.setLon(23F);
            disaster.setIsExpired(false);
            disaster.setDisasterType(disType);
            disaster.setTitle("Berlin Erdbeben");
            Disaster disaster1 = new Disaster();
            disaster1.setTitle("New York Zombie-Angriff");
            disaster1.setDisasterType(disasterType);
            disaster1.setLon(45F);
            disaster1.setLat(45F);
            disaster1.setIsExpired(false);
            Disaster disaster2 = new Disaster();
            disaster2.setTitle("London Brexit");
            disaster2.setDisasterType(dt);
            disaster2.setLon(34F);
            disaster2.setLat(34F);
            disaster2.setIsExpired(false);
//            disasterRepository.saveAndFlush(disaster);
//            disasterRepository.saveAndFlush(disaster1);
//            disasterRepository.saveAndFlush(disaster2);

            disasterService.createDisaster(disaster);
            disasterService.createDisaster(disaster1);
            disasterService.createDisaster(disaster2);
            Action a = new Action();
            a.setLat(23F);
            a.setLon(23F);
            a.addActionObject(ao);
            a.addActionObject(ao1);
            a.addActionObject(ao4);
            a.setDisaster(disaster);
            a.setActionType(ActionType.SEEK);
            a.setUser(anonymous);
            Action a2 = new Action();
            a2.setLat(23F);
            a2.setLon(23F);
            a2.addActionObject(ao11);
            a2.addActionObject(ao5);
            a2.addActionObject(ao2);
            a2.addActionObject(ao4);
            a2.setDisaster(disaster);
            a2.setActionType(ActionType.OFFER);
            a2.setUser(admin);
            Action a3 = new Action();
            a3.setActionType(ActionType.SEEK);
            a3.addActionObject(ao2);
            a3.addActionObject(ao7);
            a3.addActionObject(ao8);
            a3.setDisaster(disaster1);
            a3.setLat(45F);
            a3.setLon(45F);
            a3.setUser(admin);
            Action a4 = new Action();
            a4.setActionType(ActionType.SEEK);
            a4.addActionObject(ao1);
            a4.addActionObject(ao9);
            a4.addActionObject(ao8);
            a4.setDisaster(disaster1);
            a4.setLat(45F);
            a4.setLon(45F);
            a4.setUser(user);

            Action a5 = new Action();
            a5.setActionType(ActionType.SEEK);
            a5.addActionObject(ao2);
            a5.addActionObject(ao9);
            a5.addActionObject(ao8);
            a5.setDisaster(disaster1);
            a5.setLat(45F);
            a5.setLon(45F);
            a5.setUser(anonymous);

            Action a6 = new Action();
            Action a15 = new Action();
            Action a7 = new Action();
            Action a8 = new Action();
            Action a9 = new Action();
            Action a10 = new Action();
            Action a11 = new Action();
            Action a12 = new Action();
            Action a13 = new Action();
            Action a14 = new Action();
            a6.setDisaster(disaster2);
            a7.setDisaster(disaster2);
            a8.setDisaster(disaster2);
            a9.setDisaster(disaster2);
            a10.setDisaster(disaster2);
            a11.setDisaster(disaster2);
            a12.setDisaster(disaster2);
            a13.setDisaster(disaster2);
            a14.setDisaster(disaster2);
            a15.setDisaster(disaster2);
            a6.setActionType(ActionType.KNOWLEDGE);
            a6.setUser(admin);
            a7.setActionType(ActionType.KNOWLEDGE);
            a7.setUser(user);
            a8.setActionType(ActionType.KNOWLEDGE);
            a8.setUser(anonymous);
            a9.setActionType(ActionType.KNOWLEDGE);
            a9.setUser(admin);
            a10.setActionType(ActionType.KNOWLEDGE);
            a10.setUser(user);
            a11.setActionType(ActionType.KNOWLEDGE);
            a11.setUser(admin);
            a12.setActionType(ActionType.KNOWLEDGE);
            a12.setUser(anonymous);
            a13.setActionType(ActionType.KNOWLEDGE);
            a13.setUser(user);
            a14.setActionType(ActionType.KNOWLEDGE);
            a14.setUser(admin);
            a15.setActionType(ActionType.KNOWLEDGE);
            a15.setUser(user);
            a6.setLon(34.03F);
            a6.setLat(34.03F);
            a7.setLon(34.03F);
            a7.setLat(34F);
            a8.setLon(34.05F);
            a8.setLat(34.03F);
            a9.setLon(34F);
            a9.setLat(34.03F);
            a10.setLon(34.033F);
            a10.setLat(34.03F);
            a11.setLon(34.07F);
            a11.setLat(34.00F);
            a12.setLon(34.03F);
            a12.setLat(34.04F);
            a13.setLon(34.05F);
            a13.setLat(34.03F);
            a14.setLon(34.02F);
            a14.setLat(34.03F);
            a15.setLon(34.01F);
            a15.setLat(34.03F);
            actionService.save(a);
            actionService.save(a2);
            actionService.save(a3);
            actionService.save(a4);
            actionService.save(a5);
            actionService.save(a6);
            actionService.save(a7);
            actionService.save(a8);
            actionService.save(a9);
            actionService.save(a10);
            actionService.save(a11);
            actionService.save(a12);
            actionService.save(a13);
            actionService.save(a14);
            actionService.save(a15);
            log.info("Daten wurden Hinzugefügt");
        } else {
            log.info("schon Daten in der Datenbank");
        }
    }
}
