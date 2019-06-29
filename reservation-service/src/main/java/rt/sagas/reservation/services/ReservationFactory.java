package rt.sagas.reservation.services;

import org.springframework.stereotype.Component;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.entities.ReservationStatus;

import java.util.UUID;

@Component
public class ReservationFactory {

    public static Integer MAX_RESERVATION_NUMBER = 19;

    public Reservation createNewPendingReservationFor(Long orderId, Long userId) {
        UUID uuid = UUID.randomUUID();
        int reservationNumber = new Double(Math.random() * MAX_RESERVATION_NUMBER).intValue();
        return new Reservation(uuid.toString(), reservationNumber, orderId, userId, ReservationStatus.PENDING);
    }
}
