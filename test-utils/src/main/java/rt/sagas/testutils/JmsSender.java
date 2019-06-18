package rt.sagas.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import rt.sagas.events.SagaEvent;

import javax.transaction.Transactional;

@Component
public class JmsSender {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void send(String queue, SagaEvent event) throws Exception {

        String message = objectMapper.writeValueAsString(event);

        jmsTemplate.send(queue, session -> {
            return session.createTextMessage(message);
        });
    }
}
