package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.domain.Action;
import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import de.extremeenvironment.disasterservice.web.rest.ActionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by over on 05.06.2016.
 */
@Service
public class DisasterService {

    private final Logger log = LoggerFactory.getLogger(DisasterService.class);

    @Inject
    private DisasterRepository disasterRepository;


    public Disaster createDisater() {
        Disaster disaster = new Disaster();


        disasterRepository.save(disaster);

        log.debug("Created Information for Disaster: {}", disaster);

        return disaster;
    }

    public void deleteDisaster(Long id) {
        disasterRepository.delete(disasterRepository.findById(id).get());
    }

    public List<Disaster> getAllDisasters() {
        return disasterRepository.findAll();
    }

    /**
     * @param disaster
     * @return the nearest disaster of an action location, in a radius of 15000km
     */
    public Disaster getDisasterForDisaster(Disaster disaster) {
        return getDisasterByPosition(disaster.getLon(), disaster.getLat());
    }

    public Disaster getDisasterForAction(Action action) {
        return getDisasterByPosition(action.getLon(), action.getLat());
    }

    public Disaster getDisasterByPosition(float lon, float lat) {
        float distance = 15000;
        Disaster disasterReturn = null;

        List<Disaster> disasterList = disasterRepository.findAll();

        for (int i = 0; i < disasterList.size(); i++) {
            Disaster disaster = disasterList.get(i);
            Float disasterLon = disaster.getLon();
            Float disasterLat = disaster.getLat();
            float distanceBetween = getDistance(lat, lon, disasterLat, disasterLon);
            if (distanceBetween < 15000) {
                if (distanceBetween < distance) {
                    distance = distanceBetween;
                    disasterReturn = disaster;
                }
            }

        }
        return disasterReturn;
    }


    /**
     * calculates the distance of two coordinates, subtracts one kilometer ber day waited
     *
     * @param lat1 the latitude of the first coordinate
     * @param lon1 the longitude of the first coordinate
     * @param lat2 the latitude of the second coordinate
     * @param lon2 the longitude of the second coordinate
     * @return the distance
     */
    public static Float getDistance(float lat1, float lon1, float lat2, float lon2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }


    /**
     * calculates the distance of two coordinates, subtracts one kilometer ber day waited
     *
     * @param lat1 the latitude of the first coordinate
     * @param lon1 the longitude of the first coordinate
     * @param lat2 the latitude of the second coordinate
     * @param lon2 the longitude of the second coordinate
     * @param seekDate the date the bonus shall be calculated from
     * @return the distance
     */
    public static Float getDistance(float lat1, float lon1, float lat2, float lon2, ZonedDateTime seekDate) {
        Duration d = Duration.between(seekDate, ZonedDateTime.now());
        long waitingDuration = d.getSeconds();

        final float BONUS = 1 / (60 * 60 * 24); // 1 km per day waited
        //TODO set bonus via web interface

        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist - (waitingDuration * BONUS);
    }


    /**
     * @see DisasterService#getDistance(float, float, float, float) with seekDate set to now
     */
    public static Float getOldDistance(float lat1, float lon1, float lat2, float lon2) {
        return getDistance(lat1, lon1, lat2, lon2, ZonedDateTime.now());
    }


}
