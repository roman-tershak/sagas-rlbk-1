package rt.sagas.reservation.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.OrderCreatedEvent;
import rt.sagas.events.ReservationCancelledEvent;
import rt.sagas.events.ReservationCreatedEvent;
import rt.sagas.events.services.EventSender;
import rt.sagas.reservation.exceptions.ReservationException;
import rt.sagas.reservation.services.ReservationService;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.*;

@Component
public class OrderEventsListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private EventSender eventSender;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @JmsListener(destination = ORDER_CREATED_EVENT_QUEUE)
    public void receiveMessage(@Payload TextMessage textMessage) throws Exception {

        OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(textMessage.getText(),
                OrderCreatedEvent.class);
        LOGGER.info("Order Created Event received: {}", orderCreatedEvent);

        Long orderId = orderCreatedEvent.getOrderId();
        Long userId = orderCreatedEvent.getUserId();
        String cartNumber = orderCreatedEvent.getCartNumber();

        try {
            String reservationId = reservationService.createReservation(orderId, userId);

            eventSender.sendEvent(
                    RESERVATION_CREATED_EVENT_QUEUE,
                    new ReservationCreatedEvent(
                            reservationId, orderId, userId, cartNumber));

            LOGGER.info("About to complete Order Created Event handling: {}", orderCreatedEvent);

        } catch (ReservationException e) {
            LOGGER.warn("An exception occurred: {}", e.getMessage());

            eventSender.sendEvent(
                    RESERVATION_CANCELLED_EVENT_QUEUE,
                    new ReservationCancelledEvent(null, orderId, userId,
                            e.getMessage().substring(0, 100)));
        }
    }
}
