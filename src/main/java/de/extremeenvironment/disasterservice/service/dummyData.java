package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.DisasterType;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.*;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by linus on 24.06.16.
 */
public class dummyData {
    @Inject
    ActionRepository actionRepository;

    @Inject
    ActionObjectRepository actionObjectRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    DisasterRepository disasterRepository;

    @Inject
    DisasterTypeRepository disasterTypeRepository;

    @Singleton
    public void dataCreate(){
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
        ActionObject ao1 = new ActionObject();
        ao1.setName("Holz");
        ActionObject ao2 = new ActionObject();
        ao2.setName("Generator");
        ActionObject ao3 = new ActionObject();
        ao3.setName("Verbandszeug");
        ActionObject ao4 = new ActionObject();
        ao4.setName("Rollstuhl");
        ActionObject ao5 = new ActionObject();
        ao5.setName("Standardessen");
        ActionObject ao6 = new ActionObject();
        ao6.setName("Wasser");
        ActionObject ao7 = new ActionObject();
        ao7.setName("Supplemente");
        ActionObject ao8 = new ActionObject();
        ao8.setName("Zelt");
        ActionObject ao9 = new ActionObject();
        ao9.setName("Betten");
        ActionObject ao10 = new ActionObject();
        ao10.setName("Jacken");
        ActionObject ao11 = new ActionObject();
        ao11.setName("Schrottflinte");
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
        disaster.setLat(23L);
        disaster.setLon(23L);
        disaster.setTitle("Berlin Erdbeben");
        Disaster disaster1 = new Disaster();
        disaster1.setTitle("New York Zombie-Angriff");
        disaster1.setLon(45L);
        disaster1.setLat(45L);
        Disaster disaster2 = new Disaster();
        disaster2.setTitle("London Brexit");
        disaster2.setLon(34L);
        disaster2.setLat(34L);
        disasterRepository.saveAndFlush(disaster);
        disasterRepository.saveAndFlush(disaster1);
        disasterRepository.saveAndFlush(disaster2);
        Action a = new Action();
        a.setLat(23F);
        a.setLon(23F);
        a.addActionObject(ao);
        a.addActionObject(ao1);
        a.addActionObject(ao4);
        a.setDisaster(disaster);
        a.setActionType(ActionType.SEEK);
        Action a2 = new Action();
        a2.setLat(23F);
        a2.setLon(23F);
        a2.addActionObject(ao11);
        a2.addActionObject(ao5);
        a2.addActionObject(ao2);
        a2.addActionObject(ao4);
        a2.setDisaster(disaster);
        a2.setActionType(ActionType.OFFER);
        Action a3 = new Action();
        a3.setActionType(ActionType.SEEK);
        a3.addActionObject(ao2);
        a3.addActionObject(ao7);
        a3.addActionObject(ao8);
        a3.setDisaster(disaster1);
        a3.setLat(45F);
        a3.setLon(45F);
        Action a4 = new Action();
        a4.setActionType(ActionType.SEEK);
        a4.addActionObject(ao1);
        a4.addActionObject(ao9);
        a4.addActionObject(ao8);
        a4.setDisaster(disaster1);
        a4.setLat(45F);
        a4.setLon(45F);
        Action a5 = new Action();
        a5.setActionType(ActionType.SEEK);
        a5.addActionObject(ao2);
        a5.addActionObject(ao9);
        a5.addActionObject(ao8);
        a5.setDisaster(disaster1);
        a5.setLat(45F);
        a5.setLon(45F);









    }
}
