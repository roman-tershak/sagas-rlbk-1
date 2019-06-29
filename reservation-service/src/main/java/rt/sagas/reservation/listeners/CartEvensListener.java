package rt.sagas.reservation.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import rt.sagas.events.CartAuthorizedEvent;
import rt.sagas.events.CartRejectedEvent;
import rt.sagas.events.ReservationCancelledEvent;
import rt.sagas.events.ReservationConfirmedEvent;
import rt.sagas.events.services.EventSender;
import rt.sagas.reservation.exceptions.ReservationException;
import rt.sagas.reservation.services.ReservationService;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.*;

@Component
public class CartEvensListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventSender eventSender;

    @Transactional
    @JmsListener(destination = CART_AUTHORIZED_EVENT_QUEUE)
    public void receiveCartAuthorizedMessage(@Payload TextMessage textMessage) throws Exception {
        try {
            CartAuthorizedEvent cartAuthorizedEvent = objectMapper.readValue(
                    textMessage.getText(), CartAuthorizedEvent.class);

            LOGGER.info("Cart Authorized Event received: {}", cartAuthorizedEvent);

            String reservationId = cartAuthorizedEvent.getReservationId();
            Long orderId = cartAuthorizedEvent.getOrderId();
            Long userId = cartAuthorizedEvent.getUserId();

            reservationService.confirmReservation(reservationId);

            eventSender.sendEvent(
                    RESERVATION_CONFIRMED_EVENT_QUEUE,
                    new ReservationConfirmedEvent(
                            reservationId, orderId, userId));

            LOGGER.info("About to complete Cart Authorized Event handling: {}", cartAuthorizedEvent);
        } catch (ReservationException e) {
            LOGGER.error("An error occurred in Cart Authorized Event handling: {}, {}", textMessage, e);
        }
    }

    @Transactional
    @JmsListener(destination = CART_REJECTED_EVENT_QUEUE)
    public void receiveCartRejectedMessage(@Payload TextMessage textMessage) throws Exception {
        try {
            CartRejectedEvent cartRejectedEvent = objectMapper.readValue(
                    textMessage.getText(), CartRejectedEvent.class);

            LOGGER.info("Cart Rejected Event received: {}", cartRejectedEvent);

            String reservationId = cartRejectedEvent.getReservationId();
            Long orderId = cartRejectedEvent.getOrderId();
            Long userId = cartRejectedEvent.getUserId();
            String reason = cartRejectedEvent.getReason();

            reservationService.cancelReservation(reservationId, reason);

            eventSender.sendEvent(
                    RESERVATION_CANCELLED_EVENT_QUEUE,
                    new ReservationCancelledEvent(
                            reservationId, orderId, userId, reason));

            LOGGER.info("About to complete Cart Rejected Event handling: {}", cartRejectedEvent);
        } catch (ReservationException e) {
            LOGGER.error("An error occurred in Cart Rejected Event handling: {}, {}", textMessage, e);
        }
    }
}
