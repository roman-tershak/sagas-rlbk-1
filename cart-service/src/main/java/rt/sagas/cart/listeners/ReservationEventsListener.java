package rt.sagas.cart.listeners;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.cart.services.TransactionService;
import rt.sagas.events.CartEvent;
import rt.sagas.events.ReservationCreatedEvent;
import rt.sagas.events.services.EventSender;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.RESERVATION_CREATED_EVENT_QUEUE;

@Component
public class ReservationEventsListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventSender eventSender;

    @Transactional
    @JmsListener(destination = RESERVATION_CREATED_EVENT_QUEUE)
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {

        ReservationCreatedEvent reservationCreatedEvent = objectMapper.readValue(
                textMessage.getText(), ReservationCreatedEvent.class);
        LOGGER.info("Reservation Created Event received: {}", reservationCreatedEvent);

        CartEvent cartEvent = transactionService.authorizeCart(
                reservationCreatedEvent.getReservationId(),
                reservationCreatedEvent.getOrderId(),
                reservationCreatedEvent.getUserId(),
                reservationCreatedEvent.getCartNumber());

        eventSender.sendEvent(
                transactionService.getQueueName(cartEvent),
                cartEvent);

        LOGGER.info("About to complete Reservation Created Event handling: {}", reservationCreatedEvent);
    }
}
