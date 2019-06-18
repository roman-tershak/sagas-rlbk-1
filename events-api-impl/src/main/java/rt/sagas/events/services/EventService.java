package rt.sagas.events.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import rt.sagas.events.SagaEvent;
import rt.sagas.events.entities.EventEntity;
import rt.sagas.events.repositories.EventRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static javax.transaction.Transactional.TxType.REQUIRED;

@Service
public class EventService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventSender eventSender;

    @Transactional(REQUIRED)
    public void storeOutgoingEvent(String destination, SagaEvent event) throws Exception {

        eventRepository.save(
                new EventEntity(
                        destination, objectMapper.writeValueAsString(event)));

        LOGGER.info("Stored outgoing Event {} for {}", event, destination);
    }

    @org.springframework.transaction.annotation.Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.REPEATABLE_READ,
            noRollbackFor = ObjectOptimisticLockingFailureException.class
    )
    public void sendOutgoingEvents() {
        List<Long> ids = new ArrayList<>();

        eventRepository.findAll().forEach(eventEntity -> {

            eventSender.sendEvent(eventEntity.getDestination(), eventEntity.getEvent());
            ids.add(eventEntity.getId());
        });

        int idsCount = ids.size();
        if (idsCount > 0) {
            eventRepository.deleteByIds(ids);
        }
        LOGGER.info("A number of events {} sent", idsCount);
    }
}
