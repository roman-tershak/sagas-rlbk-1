package rt.sagas.reservation.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rt.sagas.events.*;
import rt.sagas.events.services.EventService;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.entities.ReservationFactory;
import rt.sagas.reservation.repositories.ReservationRepository;

import javax.transaction.Transactional;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static rt.sagas.events.QueueNames.*;
import static rt.sagas.reservation.entities.ReservationStatus.CONFIRMED;
import static rt.sagas.reservation.entities.ReservationStatus.PENDING;
import static rt.sagas.reservation.entities.ReservationStatus.REJECTED;

@Service
public class ReservationService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private ReservationFactory reservationFactory;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private EventService eventService;

    @Transactional(REQUIRES_NEW)
    public void createReservation(Long orderId, Long userId, String cartNumber)
            throws Exception {

        Optional<Reservation> reservationsByOrderId = reservationRepository.findByOrderId(orderId);
        if (!reservationsByOrderId.isPresent()) {

            Reservation reservation = reservationFactory.createNewPendingReservationFor(orderId, userId);

            if (orderId.toString().endsWith("1")) {
                String reason = "Order Id ends with 1";

                reservation.setStatus(REJECTED);
                reservation.setNotes(reason);
                reservationRepository.save(reservation);

                eventService.storeOutgoingEvent(
                        RESERVATION_REJECTED_EVENT_QUEUE,
                        new ReservationRejectedEvent(
                                reservation.getId(), reservation.getOrderId(), reservation.getUserId(), reason));

                LOGGER.info("Reservation {} rejected", reservation);
            } else {
                reservationRepository.save(reservation);

                eventService.storeOutgoingEvent(
                        RESERVATION_CREATED_EVENT_QUEUE,
                        new ReservationCreatedEvent(
                                reservation.getId(), reservation.getOrderId(), reservation.getUserId(), cartNumber));

                LOGGER.info("Reservation {} created", reservation);
            }
        } else {
            LOGGER.warn("Reservations for Order Id {} has already been created", orderId);
        }
    }

    @Transactional(REQUIRES_NEW)
    public void confirmReservation(String reservationId, Long orderId, Long userId) throws Exception {

        Reservation reservation = reservationRepository.findById(reservationId).orElseGet(() -> {
            LOGGER.warn("Reservation with id {} does not exist, looking for one with orderId {}",
                    reservationId, orderId);

            return reservationRepository.findByOrderId(orderId).orElseGet(() -> {
                LOGGER.warn("Reservation for orderId {} does not exist, creating it", orderId);

                return reservationFactory.createNewPendingReservationFor(orderId, userId);
            });
        });

        if (reservation.getStatus() == PENDING) {

            reservation.setStatus(CONFIRMED);
            reservationRepository.save(reservation);

            eventService.storeOutgoingEvent(
                    RESERVATION_CONFIRMED_EVENT_QUEUE,
                    new ReservationConfirmedEvent(
                            reservation.getId(), reservation.getOrderId(), reservation.getUserId()));

            LOGGER.info("Reservation {} confirmed", reservation);
        } else {
            LOGGER.warn("Reservation: {} is not PENDING, skipping", reservation);
        }
    }
}
