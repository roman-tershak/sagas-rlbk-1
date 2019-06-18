package rt.sagas.events;

public class CartAuthorizedEvent extends CartEvent {

    public CartAuthorizedEvent() {
    }

    public CartAuthorizedEvent(String reservationId, String cartNumber, Long orderId, Long userId) {
        super(reservationId, cartNumber, orderId, userId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CartAuthorizedEvent{");
        sb.append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
