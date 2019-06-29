package rt.sagas.events.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import rt.sagas.events.SagaEvent;

import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.REQUIRED;

@Component
public class EventSender {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(REQUIRED)
    public void sendEvent(String destination, SagaEvent event) {
        try {
            String eventString = objectMapper.writeValueAsString(event);

            jmsTemplate.send(destination, session -> {
                return session.createTextMessage(eventString);
            });

            LOGGER.info("Event {} sent to {}", event, destination);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unexpected non-retriable error occurred: ", e);
        }
    }
}
