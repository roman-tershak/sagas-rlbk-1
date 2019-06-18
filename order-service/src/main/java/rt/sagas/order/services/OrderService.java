package rt.sagas.order.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rt.sagas.events.OrderCreatedEvent;
import rt.sagas.events.QueueNames;
import rt.sagas.events.services.EventService;
import rt.sagas.order.entities.Order;
import rt.sagas.order.repositories.OrderRepository;

import javax.transaction.Transactional;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static rt.sagas.events.QueueNames.ORDER_CREATED_EVENT_QUEUE;
import static rt.sagas.order.entities.OrderStatus.COMPLETE;
import static rt.sagas.order.entities.OrderStatus.NEW;

@Service
public class OrderService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EventService eventService;

    @Transactional(value = REQUIRES_NEW)
    public Order createOrder(Order order) throws Exception {

        Order orderSaved = orderRepository.save(order);
        LOGGER.info("Order {} created", orderSaved);

        eventService.storeOutgoingEvent(
                ORDER_CREATED_EVENT_QUEUE,
                new OrderCreatedEvent(
                        orderSaved.getId(), orderSaved.getUserId(), orderSaved.getCartNumber()));

        return orderSaved;
    }

    @Transactional(REQUIRES_NEW)
    public void completeOrder(String reservationId, Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            if (order.getStatus() == NEW) {
                order.setReservationId(reservationId);
                order.setStatus(COMPLETE);
                orderRepository.save(order);
            } else {
                LOGGER.warn("Order with id {} and reservationId {} has already been completed",
                        orderId, reservationId);
            }

        } else {
            LOGGER.error("Order with id {} and reservationId {} was not found",
                    orderId, reservationId);
        }
    }
}
