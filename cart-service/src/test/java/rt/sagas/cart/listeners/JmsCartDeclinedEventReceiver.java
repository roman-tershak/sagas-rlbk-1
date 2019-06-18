package rt.sagas.cart.listeners;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.CartDeclinedEvent;
import rt.sagas.testutils.JmsReceiver;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.CART_DECLINED_EVENT_QUEUE;

@Component
public class JmsCartDeclinedEventReceiver extends JmsReceiver<CartDeclinedEvent> {

    public JmsCartDeclinedEventReceiver() {
        super(CartDeclinedEvent.class);
    }

    @Transactional
    @JmsListener(destination = CART_DECLINED_EVENT_QUEUE)
    @Override
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {
        super.receiveMessage(textMessage.getText());
    }
}
