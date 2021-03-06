package rt.sagas.order.listeners;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rt.sagas.events.ReservationCancelledEvent;
import rt.sagas.events.ReservationConfirmedEvent;
import rt.sagas.order.OrderRepositorySpy;
import rt.sagas.order.AbstractOrderTest;
import rt.sagas.order.entities.Order;
import rt.sagas.order.entities.OrderStatus;
import rt.sagas.testutils.JmsSender;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static rt.sagas.events.QueueNames.RESERVATION_CANCELLED_EVENT_QUEUE;
import static rt.sagas.events.QueueNames.RESERVATION_CONFIRMED_EVENT_QUEUE;
import static rt.sagas.order.entities.OrderStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReservationEventsListenerTest extends AbstractOrderTest {

    private static final long USER_ID = 12L;
    private static final String RESERVATION_ID = "ABCDEF-1234-8765-UVWXYZ-12";

    @Autowired
    private JmsSender jmsSender;
    @Autowired
    private OrderRepositorySpy orderRepositorySpy;


    private long orderId;

    @Before
    public void setUp() {
        orderRepositorySpy.setThrowExceptionInSave(false);

        Order order = new Order(USER_ID, "1234567890123456");
        final Order orderSaved = orderRepositorySpy.save(order);
        orderId = orderSaved.getId();
    }

    @After
    public void tearDown() {
        orderRepositorySpy.setThrowExceptionInSave(false);
    }

    @Test
    public void testOrderBecomesCompletedOnReservationConfirmedEvent() throws Exception {
        ReservationConfirmedEvent reservationConfirmedEvent = new ReservationConfirmedEvent(
                RESERVATION_ID, orderId, USER_ID);

        jmsSender.send(RESERVATION_CONFIRMED_EVENT_QUEUE, reservationConfirmedEvent);

        Order order = waitTillCompletedAndGetOrderFromDb(5000L);
        assertThat(order, is(notNullValue()));
        assertThat(order.getStatus(), is(COMPLETE));
        assertThat(order.getUserId(), is(USER_ID));
        assertThat(order.getReservationId(), is(RESERVATION_ID));
    }

    @Test
    public void testOrderBecomesCompletedOnReservationConfirmedEventSentTwice() throws Exception {
        ReservationConfirmedEvent reservationConfirmedEvent = new ReservationConfirmedEvent(
                RESERVATION_ID, orderId, USER_ID);

        jmsSender.send(RESERVATION_CONFIRMED_EVENT_QUEUE, reservationConfirmedEvent);
        jmsSender.send(RESERVATION_CONFIRMED_EVENT_QUEUE, reservationConfirmedEvent);

        Order order = waitTillCompletedAndGetOrderFromDb(5000L);
        assertThat(order, is(notNullValue()));
        orderRepositorySpy.delete(order);
        assertThat(waitTillCompletedAndGetOrderFromDb(5000L), is(nullValue()));
    }

    @Test
    public void testOrderDoesNotBecomeCompleteWhenExceptionIsThrownOnReservationConfirmedEvent() throws Exception {
        orderRepositorySpy.setThrowExceptionInSave(true);

        ReservationConfirmedEvent reservationConfirmedEvent = new ReservationConfirmedEvent(
                RESERVATION_ID, orderId, USER_ID);

        jmsSender.send(RESERVATION_CONFIRMED_EVENT_QUEUE, reservationConfirmedEvent);

        Order order = waitTillCompletedAndGetOrderFromDb(5000L);
        assertThat(order, is(notNullValue()));
        assertThat(order.getStatus(), is(NEW));
        assertThat(order.getUserId(), is(USER_ID));
        assertThat(order.getReservationId(), is(nullValue()));
    }

    @Test
    public void testOrderBecomesFailedOnReservationCancelledEvent() throws Exception {
        ReservationCancelledEvent reservationCancelledEvent = new ReservationCancelledEvent(
                RESERVATION_ID, orderId, USER_ID, "Bad reason");

        jmsSender.send(RESERVATION_CANCELLED_EVENT_QUEUE, reservationCancelledEvent);

        Order order = waitTillStatusAndGetOrderFromDb(FAILED, 5000L);
        assertThat(order, is(notNullValue()));
        assertThat(order.getStatus(), is(FAILED));
        assertThat(order.getUserId(), is(USER_ID));
        assertThat(order.getReservationId(), is(RESERVATION_ID));
    }

    private Order waitTillCompletedAndGetOrderFromDb(long waitTimeout) throws InterruptedException {
        return waitTillStatusAndGetOrderFromDb(COMPLETE, waitTimeout);
    }

    private Order waitTillStatusAndGetOrderFromDb(OrderStatus expectedStatus, long waitTimeout) throws InterruptedException {
        long stop = System.currentTimeMillis() + waitTimeout;
        Order order = null;
        do {
            Optional<Order> optionalOrder = orderRepositorySpy.findById(orderId);
            if (optionalOrder.isPresent()) {

                order = optionalOrder.get();

                if (order.getStatus() == expectedStatus) {
                    break;
                }
            }
            Thread.sleep(100L);
        } while (System.currentTimeMillis() < stop);

        return order;
    }
}
