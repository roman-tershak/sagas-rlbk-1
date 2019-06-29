package rt.sagas.order.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.ReservationCancelledEvent;
import rt.sagas.events.ReservationConfirmedEvent;
import rt.sagas.order.entities.OrderStatus;
import rt.sagas.order.services.OrderService;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.RESERVATION_CANCELLED_EVENT_QUEUE;
import static rt.sagas.events.QueueNames.RESERVATION_CONFIRMED_EVENT_QUEUE;

@Component
public class ReservationEventsListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private OrderService orderService;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @JmsListener(destination = RESERVATION_CONFIRMED_EVENT_QUEUE)
    public void receiveReservationConfirmedMessage(@Payload TextMessage textMessage) throws Exception {

        ReservationConfirmedEvent reservationConfirmedEvent = objectMapper.readValue(
                textMessage.getText(), ReservationConfirmedEvent.class);
        LOGGER.info("Reservation Confirmed Event received: {}", reservationConfirmedEvent);

        orderService.completeOrder(
                reservationConfirmedEvent.getReservationId(),
                reservationConfirmedEvent.getOrderId(),
                OrderStatus.COMPLETE);

        LOGGER.info("About to complete Reservation Confirmed Event handling: {}", reservationConfirmedEvent);
    }

    @Transactional
    @JmsListener(destination = RESERVATION_CANCELLED_EVENT_QUEUE)
    public void receiveReservationCancelledMessage(@Payload TextMessage textMessage) throws Exception {

        ReservationCancelledEvent reservationCancelledEvent = objectMapper.readValue(
                textMessage.getText(), ReservationCancelledEvent.class);
        LOGGER.info("Reservation Cancelled Event received: {}", reservationCancelledEvent);

        orderService.completeOrder(
                reservationCancelledEvent.getReservationId(),
                reservationCancelledEvent.getOrderId(),
                OrderStatus.FAILED);

        LOGGER.info("About to complete Reservation Cancelled Event handling: {}", reservationCancelledEvent);
    }
}
