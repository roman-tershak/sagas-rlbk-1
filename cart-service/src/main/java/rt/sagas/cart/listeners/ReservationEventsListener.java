package rt.sagas.cart.listeners;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.cart.services.TransactionService;
import rt.sagas.events.ReservationCreatedEvent;

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

    @Transactional
    @JmsListener(destination = RESERVATION_CREATED_EVENT_QUEUE)
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {
        try {
            ReservationCreatedEvent reservationCreatedEvent = objectMapper.readValue(
                    textMessage.getText(), ReservationCreatedEvent.class);

            LOGGER.info("Reservation Created Event received: {}", reservationCreatedEvent);

            transactionService.authorizeTransaction(
                    reservationCreatedEvent.getReservationId(),
                    reservationCreatedEvent.getOrderId(),
                    reservationCreatedEvent.getUserId(),
                    reservationCreatedEvent.getCartNumber());

            LOGGER.info("About to complete Reservation Created Event handling: {}", reservationCreatedEvent);
        } catch (Exception e) {
            LOGGER.error("An exception occurred in Reservation Created Event handling: {}, {}", textMessage, e);
            throw e;
        }
    }
}
