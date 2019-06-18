package rt.sagas.reservation.entities;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReservationFactory {

    public Reservation createNewPendingReservationFor(Long orderId, Long userId) {
        UUID uuid = UUID.randomUUID();
        return new Reservation(uuid.toString(), orderId, userId, ReservationStatus.PENDING);
    }
}
