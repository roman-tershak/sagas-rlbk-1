package rt.sagas.reservation.entities;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReservationFactory {

    public static Integer MAX_RESERVATION_NUMBER = 199;

    public Reservation createNewReservationFor(Long orderId, Long userId) {
        UUID uuid = UUID.randomUUID();
        return new Reservation(uuid.toString(), orderId, userId, ReservationStatus.NEW);
    }

    public Integer getReservationNumber() {
        return new Double(Math.random() * MAX_RESERVATION_NUMBER).intValue();
    }

    @Deprecated
    public Reservation createNewPendingReservationFor(Long orderId, Long userId) {
        UUID uuid = UUID.randomUUID();
        int reservationNumber = new Double(Math.random() * MAX_RESERVATION_NUMBER).intValue();
        return new Reservation(uuid.toString(), reservationNumber, orderId, userId, ReservationStatus.PENDING);
    }
}
