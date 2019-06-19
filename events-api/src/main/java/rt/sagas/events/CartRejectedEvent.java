package rt.sagas.events;

public class CartRejectedEvent extends CartEvent {

    private String reason;

    public CartRejectedEvent() {
    }

    public CartRejectedEvent(String reservationId, String cartNumber, Long orderId, Long userId, String reason) {
        super(reservationId, cartNumber, orderId, userId);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CartRejectedEvent{");
        sb.append(super.toString());
        sb.append(", reason='").append(reason).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
