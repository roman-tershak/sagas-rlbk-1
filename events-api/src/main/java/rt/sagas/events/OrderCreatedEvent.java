package rt.sagas.events;

public class OrderCreatedEvent extends OrderEvent {

    private Long orderId;
    private Long userId;
    private String cartNumber;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId, Long userId, String cartNumber) {
        this.orderId = orderId;
        this.userId = userId;
        this.cartNumber = cartNumber;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCartNumber() {
        return cartNumber;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OrderCreatedEvent{");
        sb.append("orderId=").append(orderId);
        sb.append(", userId=").append(userId);
        sb.append(", cartNumber='").append(cartNumber).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
