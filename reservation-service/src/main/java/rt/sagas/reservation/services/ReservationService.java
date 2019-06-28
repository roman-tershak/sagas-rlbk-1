package rt.sagas.reservation.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;
import rt.sagas.events.ReservationCancelledEvent;
import rt.sagas.events.ReservationConfirmedEvent;
import rt.sagas.events.ReservationCreatedEvent;
import rt.sagas.events.services.EventService;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.entities.ReservationFactory;
import rt.sagas.reservation.exceptions.ReservationException;
import rt.sagas.reservation.repositories.ReservationRepository;

import javax.transaction.Transactional;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static rt.sagas.events.QueueNames.*;
import static rt.sagas.reservation.entities.ReservationStatus.*;

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
    public String getReservation(Long orderId, Long userId) {

        Reservation reservation;
        Optional<Reservation> possiblyReservation = reservationRepository.findByOrderId(orderId);
        if (possiblyReservation.isPresent()) {
            LOGGER.warn("Reservations for Order Id {} has already been created", orderId);

            reservation = possiblyReservation.get();
        } else {
            reservation = reservationFactory.createNewReservationFor(orderId, userId);

            LOGGER.info("About to create Reservation {}", reservation);
            reservation = reservationRepository.save(reservation);
        }
        return reservation.getId();
    }

    @Transactional(value = REQUIRES_NEW, rollbackOn = {ReservationException.class})
    public void makeReservation(String reservationId, String cartNumber) throws ReservationException {

        Optional<Reservation> shouldBeReservation = reservationRepository.findById(reservationId);
        if (!shouldBeReservation.isPresent()) {
            throw new ReservationException("Reservation with id " + reservationId + " does not exist");
        }
        Reservation reservation = shouldBeReservation.get();

        if (reservation.getStatus() != NEW) {
            LOGGER.warn("Reservation status is not NEW {}, skipping", reservation);
            return;
        }

        try {
            Integer reservationNumber = reservationFactory.getReservationNumber();
            reservation.setReservationNumber(reservationNumber);
            reservation.setStatus(PENDING);
            reservationRepository.saveAndFlush(reservation);

            LOGGER.info("Made reservation for {} ", reservation);

            eventService.storeOutgoingEvent(
                    RESERVATION_CREATED_EVENT_QUEUE,
                    new ReservationCreatedEvent(
                            reservation.getId(), reservation.getOrderId(), reservation.getUserId(), cartNumber));

        } catch (NonTransientDataAccessException e) {
            throw new ReservationException(e.getMessage());
        }
    }

    @Transactional(REQUIRES_NEW)
    public void handleReservationError(ReservationException re, Long orderId, Long userId) {
        Optional<Reservation> byOrderId = reservationRepository.findByOrderId(orderId);
        if (!byOrderId.isPresent()) {
            LOGGER.error("Reservations for Order Id {} does not exist", orderId);
            return;
        }

        String errorString = re.getMessage().substring(0, 100);

        Reservation reservation = byOrderId.get();
        reservation.setStatus(DECLINED);
        reservation.setNotes(errorString);
        reservationRepository.save(reservation);
        LOGGER.warn("Declined Reservation {}", reservation);

        eventService.storeOutgoingEvent(
                RESERVATION_CANCELLED_EVENT_QUEUE,
                new ReservationCancelledEvent(null, orderId, userId,
                        errorString));
    }

    @Transactional(REQUIRES_NEW)
    public void confirmReservation(String reservationId, Long orderId, Long userId) throws Exception {

        Reservation reservation = getReservation(reservationId, orderId, userId);

        if (reservation.getStatus() == PENDING) {

            reservation.setStatus(CONFIRMED);
            reservation.setReservationNumber(null);
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

    @Transactional(REQUIRES_NEW)
    public void cancelReservation(String reservationId, Long orderId, Long userId, String reason) throws Exception {

        Reservation reservation = getReservation(reservationId, orderId, userId);

        if (reservation.getStatus() == PENDING) {

            reservation.setStatus(CANCELLED);
            reservation.setReservationNumber(null);
            reservation.setNotes(reason);
            reservationRepository.save(reservation);

            eventService.storeOutgoingEvent(
                    RESERVATION_CANCELLED_EVENT_QUEUE,
                    new ReservationCancelledEvent(
                            reservation.getId(), reservation.getOrderId(), reservation.getUserId(), reason));

            LOGGER.info("Reservation {} cancelled", reservation);
        } else {
            LOGGER.error("Reservation: {} is not PENDING, cannot cancel it", reservation);
        }
    }

    private Reservation getReservation(String reservationId, Long orderId, Long userId) {
        return reservationRepository.findById(reservationId).orElseGet(() -> {
                LOGGER.warn("Reservation with id {} does not exist, looking for one with orderId {}",
                        reservationId, orderId);

                return reservationRepository.findByOrderId(orderId).orElseGet(() -> {
                    LOGGER.warn("Reservation for orderId {} does not exist, creating it", orderId);

                    return reservationFactory.createNewPendingReservationFor(orderId, userId);
                });
            });
    }
}
