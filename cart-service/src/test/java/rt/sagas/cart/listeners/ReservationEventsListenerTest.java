package rt.sagas.cart.listeners;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rt.sagas.cart.entities.Transaction;
import rt.sagas.cart.repositories.TransactionRepository;
import rt.sagas.events.CartAuthorizedEvent;
import rt.sagas.events.CartRejectedEvent;
import rt.sagas.events.ReservationCreatedEvent;
import rt.sagas.testutils.JmsSender;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static rt.sagas.cart.entities.TransactionStatus.AUTHORIZED;
import static rt.sagas.cart.entities.TransactionStatus.REJECTED;
import static rt.sagas.events.QueueNames.RESERVATION_CREATED_EVENT_QUEUE;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReservationEventsListenerTest {

    private static final String RESERVATION_ID = "123456-1234-654321";
    private static final long ORDER_ID = 12345L;
    private static final long USER_ID = 123L;
    private static final String CART_NUMBER = "1234567890123456";

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private JmsCartAuthorizedEventReceiver cartAuthorizedEventReceiver;
    @Autowired
    private JmsCartRejectedEventReceiver cartRejectedEventReceiver;
    @Autowired
    private JmsSender jmsSender;

    @After
    public void tearDown() {
        transactionRepository.deleteAll();
    }

    @Test
    public void testTransactionIsCreatedAndCartAuthorizedEventIsSentOnReservationCreatedEvent() throws Exception {
        ReservationCreatedEvent reservationCreatedEvent = new ReservationCreatedEvent(
                RESERVATION_ID, ORDER_ID, USER_ID, CART_NUMBER);

        jmsSender.send(RESERVATION_CREATED_EVENT_QUEUE, reservationCreatedEvent);

        CartAuthorizedEvent cartAuthorizedEvent = cartAuthorizedEventReceiver.pollEvent(5000L);
        assertThat(cartAuthorizedEvent, is(notNullValue()));
        assertThat(cartAuthorizedEvent.getReservationId(), is(RESERVATION_ID));
        assertThat(cartAuthorizedEvent.getOrderId(), is(ORDER_ID));
        assertThat(cartAuthorizedEvent.getUserId(), is(USER_ID));
        assertThat(cartAuthorizedEvent.getCartNumber(), is(CART_NUMBER));

        Transaction transaction = waitAndGetTransactionByOrderIdFromDb(ORDER_ID, 5000L);
        assertThat(transaction, is(notNullValue()));
        assertThat(transaction.getOrderId(), is(ORDER_ID));
        assertThat(transaction.getUserId(), is(USER_ID));
        assertThat(transaction.getCartNumber(), is(CART_NUMBER));
        assertThat(transaction.getStatus(), is(AUTHORIZED));
    }

    @Test
    public void testTransactionIsCreatedAndCartAuthorizedEventIsSentOnceOnReservationCreatedEventSentTwice() throws Exception {
        ReservationCreatedEvent reservationCreatedEvent = new ReservationCreatedEvent(
                RESERVATION_ID, ORDER_ID, USER_ID, CART_NUMBER);

        jmsSender.send(RESERVATION_CREATED_EVENT_QUEUE, reservationCreatedEvent);
        jmsSender.send(RESERVATION_CREATED_EVENT_QUEUE, reservationCreatedEvent);

        assertThat(cartAuthorizedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID) &&
                    e.getReservationId().equals(RESERVATION_ID),10000L),
                is(notNullValue()));
        assertThat(cartAuthorizedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID) &&
                    e.getReservationId().equals(RESERVATION_ID),10000L),
                is(nullValue()));

        Transaction transaction = waitAndGetTransactionByOrderIdFromDb(ORDER_ID, 5000L);
        assertThat(transaction, is(notNullValue()));
        transactionRepository.delete(transaction);
        assertThat(waitAndGetTransactionByOrderIdFromDb(ORDER_ID, 5000L), is(nullValue()));
    }

    @Test
    public void testTransactionIsRejectedAndCartRejectedEventIsSentWhenCartNumberEndsWithSomeBadNumbers() throws Exception {
        int i = 0;
        for (String bd : Arrays.asList("1", "7", "9")) {
            String badCartNumber = CART_NUMBER.substring(0, CART_NUMBER.length() - 1) + bd;
            String reservationId = RESERVATION_ID.substring(0, RESERVATION_ID.length() - 1) + bd;
            long orderId = ORDER_ID + i++;

            ReservationCreatedEvent reservationCreatedEvent = new ReservationCreatedEvent(
                    reservationId, orderId, USER_ID, badCartNumber);

            jmsSender.send(RESERVATION_CREATED_EVENT_QUEUE, reservationCreatedEvent);

            CartAuthorizedEvent noCartAuthorizedEvent = cartAuthorizedEventReceiver.pollEvent(
                    e -> e.getReservationId().equals(reservationId), 10000L);
            assertThat(noCartAuthorizedEvent, is(nullValue()));

            CartRejectedEvent cartRejectedEvent = cartRejectedEventReceiver.pollEvent(
                    e -> e.getReservationId().equals(reservationId), 10000L);
            assertThat(cartRejectedEvent, is(notNullValue()));
            assertThat(cartRejectedEvent.getReservationId(), is(reservationId));
            assertThat(cartRejectedEvent.getOrderId(), is(orderId));
            assertThat(cartRejectedEvent.getUserId(), is(USER_ID));
            assertThat(cartRejectedEvent.getCartNumber(), is(badCartNumber));
            assertThat(cartRejectedEvent.getReason(), is("Cart number ends with " + bd));

            Transaction transaction = waitAndGetTransactionByOrderIdFromDb(orderId, 5000L);
            assertThat(transaction, is(notNullValue()));
            assertThat(transaction.getOrderId(), is(orderId));
            assertThat(transaction.getUserId(), is(USER_ID));
            assertThat(transaction.getCartNumber(), is(badCartNumber));
            assertThat(transaction.getStatus(), is(REJECTED));
            assertThat(transaction.getReason(), is("Cart number ends with " + bd));
        }
    }

    protected Transaction waitAndGetTransactionByOrderIdFromDb(
            Long orderId, long waitTimeout) throws Exception {

        long stop = System.currentTimeMillis() + waitTimeout;
        do {
            Optional<Transaction> optional = transactionRepository.findByOrderId(orderId);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                Thread.sleep(100L);
            }
        } while (System.currentTimeMillis() < stop);

        return null;
    }
}
