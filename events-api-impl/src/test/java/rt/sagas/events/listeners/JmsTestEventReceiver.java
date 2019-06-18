package rt.sagas.events.listeners;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.TestEvent;
import rt.sagas.testutils.JmsReceiver;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.TestConfiguration.TEST_DESTINATION;

@Component
public class JmsTestEventReceiver extends JmsReceiver<TestEvent> {

    public JmsTestEventReceiver() {
        super(TestEvent.class);
    }

    @Transactional
    @JmsListener(destination = TEST_DESTINATION)
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {
        super.receiveMessage(textMessage.getText());
    }
}
