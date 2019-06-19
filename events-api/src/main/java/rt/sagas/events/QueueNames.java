package rt.sagas.events;

public interface QueueNames {

    String ORDER_CREATED_EVENT_QUEUE = "order.created.event.queue";

    String RESERVATION_CREATED_EVENT_QUEUE = "reservation.created.event.queue";
    String RESERVATION_CONFIRMED_EVENT_QUEUE = "reservation.confirmed.event.queue";
    String RESERVATION_CANCELLED_EVENT_QUEUE = "reservation.cancelled.event.queue";

    String CART_AUTHORIZED_EVENT_QUEUE = "cart.authorized.event.queue";
    String CART_REJECTED_EVENT_QUEUE = "cart.rejected.event.queue";
}
