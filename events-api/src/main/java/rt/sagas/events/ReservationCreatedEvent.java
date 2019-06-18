package rt.sagas.events;

public class ReservationCreatedEvent extends ReservationEvent {

    private String cartNumber;

    public ReservationCreatedEvent() {
    }

    public ReservationCreatedEvent(String reservationId, Long orderId, Long userId, String cartNumber) {
        super(reservationId, orderId, userId);

        this.cartNumber = cartNumber;
    }

    public String getCartNumber() {
        return cartNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReservationCreatedEvent{");
        sb.append("cartNumber='").append(cartNumber).append('\'');
        sb.append(", ").append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
