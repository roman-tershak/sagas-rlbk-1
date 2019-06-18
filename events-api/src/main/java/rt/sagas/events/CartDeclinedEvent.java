package rt.sagas.events;

public class CartDeclinedEvent extends CartEvent {

    private String reason;

    public CartDeclinedEvent() {
    }

    public CartDeclinedEvent(String reservationId, String cartNumber, Long orderId, Long userId, String reason) {
        super(reservationId, cartNumber, orderId, userId);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CartDeclinedEvent{");
        sb.append(super.toString());
        sb.append(", reason='").append(reason).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
