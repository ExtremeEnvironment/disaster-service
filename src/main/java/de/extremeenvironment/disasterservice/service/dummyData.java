package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.DisasterServiceApp;
import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.ActionObject;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.domain.DisasterType;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import de.extremeenvironment.disasterservice.repository.*;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by linus on 24.06.16.
 */


@Service
public class DummyData {

    @Inject
    DisasterTypeRepository disasterTypeRepository;

    @Inject
    ActionRepository actionRepository;

    @Inject
    ActionObjectRepository actionObjectRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    DisasterRepository disasterRepository;

//DummyData(ActionObjectRepository actionObjectRepository,UserRepository userRepository, DisasterRepository disasterRepository,DisasterTypeRepository disasterTypeRepository, ActionRepository actionRepository){
//    this.actionRepository = actionRepository;
//    this.actionObjectRepository = actionObjectRepository;
//    this.disasterRepository = disasterRepository;
//    this.disasterTypeRepository = disasterTypeRepository;
//    this.userRepository = userRepository;
//}


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
        disaster.setLat(23F);
        disaster.setLon(23F);
        disaster.setDisasterType(disType);
        disaster.setTitle("Berlin Erdbeben");
        Disaster disaster1 = new Disaster();
        disaster1.setTitle("New York Zombie-Angriff");
        disaster1.setDisasterType(disasterType);
        disaster1.setLon(45F);
        disaster1.setLat(45F);
        Disaster disaster2 = new Disaster();
        disaster2.setTitle("London Brexit");
        disaster2.setDisasterType(dt);
        disaster2.setLon(34F);
        disaster2.setLat(34F);
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
        a7.setActionType(ActionType.KNOWLEDGE);
        a8.setActionType(ActionType.KNOWLEDGE);
        a9.setActionType(ActionType.KNOWLEDGE);
        a10.setActionType(ActionType.KNOWLEDGE);
        a11.setActionType(ActionType.KNOWLEDGE);
        a12.setActionType(ActionType.KNOWLEDGE);
        a13.setActionType(ActionType.KNOWLEDGE);
        a14.setActionType(ActionType.KNOWLEDGE);
        a15.setActionType(ActionType.KNOWLEDGE);
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
        actionRepository.save(a);
        actionRepository.save(a2);
        actionRepository.save(a3);
        actionRepository.save(a4);
        actionRepository.save(a5);
        actionRepository.save(a6);
        actionRepository.save(a7);
        actionRepository.save(a8);
        actionRepository.save(a9);
        actionRepository.save(a10);
        actionRepository.save(a11);
        actionRepository.save(a12);
        actionRepository.save(a13);
        actionRepository.save(a14);
        actionRepository.save(a15);
        actionRepository.flush();












    }
}
