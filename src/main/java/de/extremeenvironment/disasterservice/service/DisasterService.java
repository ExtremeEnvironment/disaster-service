package de.extremeenvironment.disasterservice.service;

import de.extremeenvironment.disasterservice.domain.Disaster;
import de.extremeenvironment.disasterservice.repository.DisasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by over on 05.06.2016.
 */
@Service
public class DisasterService {

    private final Logger log = LoggerFactory.getLogger(DisasterService.class);

    @Inject
    private DisasterRepository disasterRepository;


    public Disaster createDisater(Long lat, Long lon) {
        Disaster disaster = new Disaster();
        disaster.setLat(lat);
        disaster.setLon(lon);

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
}
