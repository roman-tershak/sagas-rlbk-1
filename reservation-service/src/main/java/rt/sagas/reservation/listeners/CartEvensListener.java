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
import rt.sagas.reservation.services.ReservationService;

import javax.jms.TextMessage;
import javax.transaction.Transactional;

import static rt.sagas.events.QueueNames.CART_AUTHORIZED_EVENT_QUEUE;
import static rt.sagas.events.QueueNames.CART_REJECTED_EVENT_QUEUE;

@Component
public class CartEvensListener {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @JmsListener(destination = CART_AUTHORIZED_EVENT_QUEUE)
    public void receiveCartAuthorizedMessage(@Payload TextMessage textMessage) throws Exception {
        try {
            CartAuthorizedEvent cartAuthorizedEvent = objectMapper.readValue(
                    textMessage.getText(), CartAuthorizedEvent.class);

            LOGGER.info("Cart Authorized Event received: {}", cartAuthorizedEvent);

            reservationService.confirmReservation(
                    cartAuthorizedEvent.getReservationId(),
                    cartAuthorizedEvent.getOrderId(),
                    cartAuthorizedEvent.getUserId());

            LOGGER.info("About to complete Cart Authorized Event handling: {}", cartAuthorizedEvent);
        } catch (Exception e) {
            LOGGER.error("An exception occurred in Cart Authorized Event handling: {}, {}", textMessage, e);
            throw e;
        }
    }

    @Transactional
    @JmsListener(destination = CART_REJECTED_EVENT_QUEUE)
    public void receiveCartRejectedMessage(@Payload TextMessage textMessage) throws Exception {
        try {
            CartRejectedEvent cartRejectedEvent = objectMapper.readValue(
                    textMessage.getText(), CartRejectedEvent.class);

            LOGGER.info("Cart Rejected Event received: {}", cartRejectedEvent);

            reservationService.cancelReservation(
                    cartRejectedEvent.getReservationId(),
                    cartRejectedEvent.getOrderId(),
                    cartRejectedEvent.getUserId(),
                    cartRejectedEvent.getReason());

            LOGGER.info("About to complete Cart Rejected Event handling: {}", cartRejectedEvent);
        } catch (Exception e) {
            LOGGER.error("An exception occurred in Cart Rejected Event handling: {}, {}", textMessage, e);
            throw e;
        }
    }
}
