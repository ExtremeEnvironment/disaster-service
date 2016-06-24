package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.domain.Disaster;

/**
 * Created by linus on 24.06.16.
 */
public class dummyData {


    public void dataCreate(){
        Disaster disaster = new Disaster();
        disaster.setLat(23L);
        disaster.setLon(23L);
        disaster.setTitle("");
    }
}
