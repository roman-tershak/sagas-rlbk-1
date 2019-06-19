package rt.sagas.events;

public class ReservationRejectedEvent extends ReservationEvent {

    private String reason;

    public ReservationRejectedEvent() {
    }

    public ReservationRejectedEvent(String reservationId, Long orderId, Long userId, String reason) {
        super(reservationId, orderId, userId);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReservationCreatedEvent{");
        sb.append("reason='").append(reason).append('\'');
        sb.append(", ").append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
