package rt.sagas.reservation.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.OrderCreatedEvent;
import rt.sagas.reservation.services.ReservationService;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.ORDER_CREATED_EVENT_QUEUE;

@Component
public class OrderEventsListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @JmsListener(destination = ORDER_CREATED_EVENT_QUEUE)
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {
        try {
            OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(
                    textMessage.getText(), OrderCreatedEvent.class);

            LOGGER.info("Order Created Event received: {}", orderCreatedEvent);

            reservationService.createReservation(
                    orderCreatedEvent.getOrderId(),
                    orderCreatedEvent.getUserId(),
                    orderCreatedEvent.getCartNumber());

            LOGGER.info("About to complete Order Created Event handling: {}", orderCreatedEvent);
        } catch (Exception e) {
            LOGGER.error("An exception occurred in Order Created Event handling: {}, {}", textMessage, e);
            throw e;
        }
    }
}
