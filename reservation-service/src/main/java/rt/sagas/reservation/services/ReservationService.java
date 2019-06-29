package rt.sagas.reservation.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.exceptions.ReservationException;
import rt.sagas.reservation.repositories.ReservationRepository;

import javax.transaction.Transactional;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static rt.sagas.reservation.entities.ReservationStatus.*;

@Service
public class ReservationService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private ReservationFactory reservationFactory;
    @Autowired
    private ReservationRepository reservationRepository;

    @Transactional(REQUIRES_NEW)
    public String createReservation(Long orderId, Long userId) {

        Optional<Reservation> optionalReservation = reservationRepository.findByOrderId(orderId);
        if (optionalReservation.isPresent()) {
            LOGGER.warn("Reservations for Order Id {} has already been created", orderId);

            return optionalReservation.get().getId();

        } else {
            Reservation reservation = reservationRepository.saveAndFlush(
                    reservationFactory.createNewPendingReservationFor(orderId, userId));

            LOGGER.info("Reservation created: {}", reservation);

            return reservation.getId();
        }
    }

    @Transactional(value = REQUIRES_NEW, rollbackOn = ReservationException.class)
    public void confirmReservation(String reservationId) throws ReservationException {

        Reservation reservation = getReservation(reservationId);

        if (reservation.getStatus() == PENDING) {

            reservation.setStatus(CONFIRMED);
            reservation.setReservationNumber(null);
            reservationRepository.save(reservation);

            LOGGER.info("Reservation {} confirmed", reservation);
        } else {
            LOGGER.warn("Reservation: {} is not PENDING, skipping", reservation);
        }
    }

    @Transactional(value = REQUIRES_NEW, rollbackOn = ReservationException.class)
    public void cancelReservation(String reservationId, String reason) throws ReservationException {

        Reservation reservation = getReservation(reservationId);

        if (reservation.getStatus() == PENDING) {

            reservation.setStatus(CANCELLED);
            reservation.setReservationNumber(null);
            reservation.setNotes(reason);
            reservationRepository.save(reservation);

            LOGGER.info("Reservation {} cancelled", reservation);
        } else {
            LOGGER.warn("Reservation: {} is not PENDING, skipping", reservation);
        }
    }

    private Reservation getReservation(String reservationId) throws ReservationException {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new ReservationException("Reservation with id " + reservationId + " does not exist"));
    }
}
