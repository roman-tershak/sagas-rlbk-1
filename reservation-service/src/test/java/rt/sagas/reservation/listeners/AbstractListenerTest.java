package rt.sagas.reservation.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.entities.ReservationStatus;
import rt.sagas.reservation.repositories.ReservationRepository;

import java.util.Objects;
import java.util.Optional;

public class AbstractListenerTest {

    @Autowired
    protected ReservationRepository reservationRepository;

    public void tearDown() {
        reservationRepository.deleteAll();
    }

    protected Reservation waitAndGetReservationsByOrderIdAndStatusFromDb(
            Long orderId, ReservationStatus status, long waitTimeout) throws Exception {

        long stop = System.currentTimeMillis() + waitTimeout;
        do {
            Optional<Reservation> byOrderId = reservationRepository.findByOrderId(orderId);
            if (byOrderId.isPresent()) {
                Reservation r = byOrderId.get();
                if (Objects.equals(r.getStatus(), status)) {
                    return r;
                }
            }
            Thread.sleep(100L);
        } while (System.currentTimeMillis() < stop);

        return null;
    }

    protected Reservation waitAndGetReservationsByIdAndStatusFromDb(
            String reservationId, ReservationStatus status, long waitTimeout) throws Exception {

        long stop = System.currentTimeMillis() + waitTimeout;
        do {
            Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);
            if (optionalReservation.isPresent()) {
                Reservation reservation = optionalReservation.get();
                if (Objects.equals(reservation.getStatus(), status)) {
                    return reservation;
                }
            }
            Thread.sleep(100L);
        } while (System.currentTimeMillis() < stop);

        return null;
    }
}
