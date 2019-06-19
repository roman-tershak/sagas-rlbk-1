package rt.sagas.reservation;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.QueueNames;
import rt.sagas.events.ReservationCreatedEvent;
import rt.sagas.events.ReservationRejectedEvent;
import rt.sagas.testutils.JmsReceiver;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.RESERVATION_CREATED_EVENT_QUEUE;
import static rt.sagas.events.QueueNames.RESERVATION_REJECTED_EVENT_QUEUE;

@Component
public class JmsReservationRejectedEventReceiver extends JmsReceiver<ReservationRejectedEvent> {

    public JmsReservationRejectedEventReceiver() {
        super(ReservationRejectedEvent.class);
    }

    @Transactional
    @JmsListener(destination = RESERVATION_REJECTED_EVENT_QUEUE)
    @Override
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {
        super.receiveMessage(textMessage.getText());
    }
}
