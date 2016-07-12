package de.extremeenvironment.disasterservice.service;


import de.extremeenvironment.disasterservice.client.Conversation;
import de.extremeenvironment.disasterservice.client.MessageClient;
import de.extremeenvironment.disasterservice.domain.Action;
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

    private DisasterRepository disasterRepository;

    private MessageClient messageClient;

    @Inject
    public DisasterService(DisasterRepository disasterRepository, MessageClient messageClient) {
        this.disasterRepository = disasterRepository;
        this.messageClient = messageClient;
    }

    public Disaster createDisater(Disaster disaster) {
        disaster = disasterRepository.save(disaster);

        try {
            messageClient.addConversation(Conversation.forDisaster(disaster));
        } catch (Exception e) {
            log.error("could not create conversation, deleting disaster");
            disasterRepository.delete(disaster);
            throw e;
        }

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
