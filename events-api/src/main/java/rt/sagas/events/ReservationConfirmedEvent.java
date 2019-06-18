package rt.sagas.events;

public class ReservationConfirmedEvent extends ReservationEvent {

    public ReservationConfirmedEvent() {
    }

    public ReservationConfirmedEvent(String reservationId, Long orderId, Long userId) {
        super(reservationId, orderId, userId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReservationConfirmedEvent{");
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
