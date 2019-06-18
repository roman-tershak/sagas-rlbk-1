package rt.sagas.events;

public class ReservationEvent extends SagaEvent {

    private String reservationId;
    private Long orderId;
    private Long userId;

    public ReservationEvent() {
    }

    public ReservationEvent(String reservationId, Long orderId, Long userId) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.userId = userId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReservationEvent{");
        sb.append("reservationId='").append(reservationId).append('\'');
        sb.append(", orderId=").append(orderId);
        sb.append(", userId=").append(userId);
        sb.append('}');
        return sb.toString();
    }
}
