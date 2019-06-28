package rt.sagas.events;

public class ReservationCancelledEvent extends ReservationEvent {

    private String reason;

    public ReservationCancelledEvent() {
    }

    public ReservationCancelledEvent(String reservationId, Long orderId, Long userId, String reason) {
        super(reservationId, orderId, userId);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReservationCancelledEvent{");
        sb.append("reason='").append(reason).append('\'');
        sb.append(", ").append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
