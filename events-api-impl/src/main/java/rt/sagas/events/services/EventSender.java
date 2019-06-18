package rt.sagas.events.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

@Component
public class EventSender {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private JmsTemplate jmsTemplate;

    @Transactional(REQUIRES_NEW)
    public void sendEvent(String destination, String event) {

        jmsTemplate.send(destination, session -> {
            return session.createTextMessage(event);
        });

        LOGGER.info("Event {} sent to {}", event, destination);
    }
}
