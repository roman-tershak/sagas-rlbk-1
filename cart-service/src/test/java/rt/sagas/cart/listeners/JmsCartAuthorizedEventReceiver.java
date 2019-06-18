package rt.sagas.cart.listeners;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.CartAuthorizedEvent;
import rt.sagas.testutils.JmsReceiver;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.CART_AUTHORIZED_EVENT_QUEUE;

@Component
public class JmsCartAuthorizedEventReceiver extends JmsReceiver<CartAuthorizedEvent> {

    public JmsCartAuthorizedEventReceiver() {
        super(CartAuthorizedEvent.class);
    }

    @Transactional
    @JmsListener(destination = CART_AUTHORIZED_EVENT_QUEUE)
    @Override
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {
        super.receiveMessage(textMessage.getText());
    }
}
