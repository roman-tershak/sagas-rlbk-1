package rt.sagas.order;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.OrderCreatedEvent;
import rt.sagas.testutils.JmsReceiver;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.ORDER_CREATED_EVENT_QUEUE;

@Component
public class JmsOrderCreatedEventReceiver extends JmsReceiver<OrderCreatedEvent> {

    public JmsOrderCreatedEventReceiver() {
        super(OrderCreatedEvent.class);
    }

    @Transactional
    @JmsListener(destination = ORDER_CREATED_EVENT_QUEUE)
    @Override
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {
        super.receiveMessage(textMessage.getText());
    }
}
