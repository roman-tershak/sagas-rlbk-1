package rt.sagas.events;

import java.util.Objects;

public class CartEvent extends SagaEvent {

    private String reservationId;
    private String cartNumber;
    private Long orderId;
    private Long userId;

    public CartEvent() {
    }

    public CartEvent(String reservationId, String cartNumber, Long orderId, Long userId) {
        this.reservationId = reservationId;
        this.cartNumber = cartNumber;
        this.orderId = orderId;
        this.userId = userId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getCartNumber() {
        return cartNumber;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CartEvent{");
        sb.append("reservationId='").append(reservationId).append('\'');
        sb.append(", cartNumber='").append(cartNumber).append('\'');
        sb.append(", orderId=").append(orderId);
        sb.append(", userId=").append(userId);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartEvent cartEvent = (CartEvent) o;
        return Objects.equals(reservationId, cartEvent.reservationId) &&
                Objects.equals(cartNumber, cartEvent.cartNumber) &&
                Objects.equals(orderId, cartEvent.orderId) &&
                Objects.equals(userId, cartEvent.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, cartNumber, orderId, userId);
    }
}
