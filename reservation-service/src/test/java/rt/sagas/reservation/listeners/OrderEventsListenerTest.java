package rt.sagas.reservation.listeners;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rt.sagas.events.OrderCreatedEvent;
import rt.sagas.events.ReservationCancelledEvent;
import rt.sagas.events.ReservationCreatedEvent;
import rt.sagas.reservation.JmsReservationCancelledEventReceiver;
import rt.sagas.reservation.JmsReservationCreatedEventReceiver;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.entities.ReservationFactory;
import rt.sagas.testutils.JmsSender;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static rt.sagas.events.QueueNames.ORDER_CREATED_EVENT_QUEUE;
import static rt.sagas.reservation.entities.ReservationStatus.PENDING;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderEventsListenerTest extends AbstractListenerTest {

    private static final Long ORDER_ID = 123333L;
    private static final Long OTHER_ORDER_ID = 789911L;
    private static final Long USER_ID = 111111L;
    private static final Long OTHER_USER_ID = 789123L;
    private static final String CART_NUMBER = "1111567890123456";

    @Autowired
    private ReservationFactory reservationFactory;
    @Autowired
    private JmsSender jmsSender;
    @Autowired
    private JmsReservationCreatedEventReceiver reservationCreatedEventReceiver;
    @Autowired
    private JmsReservationCancelledEventReceiver reservationCancelledEventReceiver;

    @Before
    public void setUp() {
        when(reservationFactory.createNewPendingReservationFor(anyLong(), anyLong()))
                .thenCallRealMethod();
    }

    @After
    public void tearDown() {
        super.tearDown();
        reservationCreatedEventReceiver.clear();
    }

    @Test
    public void testPendingReservationIsCreatedAndReservationCreatedEventIsSentOnOrderCreatedEvent() throws Exception {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        Reservation reservation = waitAndGetReservationsByOrderIdAndStatusFromDb(
                ORDER_ID, PENDING, 10000L);
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));

        ReservationCreatedEvent reservationCreatedEvent = reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(reservation.getOrderId()),10000L);
        assertThat(reservationCreatedEvent, is(notNullValue()));
        assertThat(reservationCreatedEvent.getReservationId(), is(reservation.getId()));
        assertThat(reservationCreatedEvent.getOrderId(), is(ORDER_ID));
        assertThat(reservationCreatedEvent.getUserId(), is(USER_ID));
        assertThat(reservationCreatedEvent.getCartNumber(), is(CART_NUMBER));
    }

    @Test
    public void testReservationCreatedEventIsNotSentTwiceOnDoubleOrderCreatedEvent() throws Exception {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        Reservation reservation = waitAndGetReservationsByOrderIdAndStatusFromDb(
                ORDER_ID, PENDING, 10000L);
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));

        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(reservation.getOrderId()),10000L), is(notNullValue()));
        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(reservation.getOrderId()),10000L), is(nullValue()));
    }

    @Test
    public void testPendingReservationIsNotCreatedIfItHasAlreadyBeenCreatedForAGivenOrderId() throws Exception {
        Reservation pendingReservation = reservationFactory.createNewPendingReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(pendingReservation);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID), 10000L), is(nullValue()));

        Optional<Reservation> byOrderId = reservationRepository.findByOrderId(ORDER_ID);
        assertThat(byOrderId.isPresent(), is(true));
        Reservation reservationFromDb = byOrderId.get();
        assertThat(reservationFromDb.getId(), is(pendingReservation.getId()));
        assertThat(reservationFromDb.getOrderId(), is(pendingReservation.getOrderId()));
        assertThat(reservationFromDb.getUserId(), is(pendingReservation.getUserId()));
        assertThat(reservationFromDb.getStatus(), is(PENDING));
    }

    @Test
    public void testReservationGetsDeclinedIfReservationNumberDuplicates() throws Exception {
        Reservation otherPendingReservation = reservationFactory.createNewPendingReservationFor(
                OTHER_ORDER_ID, OTHER_USER_ID);
        Integer reservationNumber = otherPendingReservation.getReservationNumber();
        reservationRepository.save(otherPendingReservation);

        Reservation pendingReservation = reservationFactory.createNewPendingReservationFor(ORDER_ID, USER_ID);
        pendingReservation.setReservationNumber(reservationNumber);
        doReturn(pendingReservation).when(reservationFactory).createNewPendingReservationFor(ORDER_ID, USER_ID);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        assertThat(waitAndGetReservationsByOrderIdFromDb(ORDER_ID,10000L), is(nullValue()));

        ReservationCancelledEvent reservationCancelledEvent = reservationCancelledEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID),10000L);
        assertThat(reservationCancelledEvent, is(notNullValue()));
        assertThat(reservationCancelledEvent.getReservationId(), is(nullValue()));
        assertThat(reservationCancelledEvent.getOrderId(), is(ORDER_ID));
        assertThat(reservationCancelledEvent.getUserId(), is(USER_ID));
        assertThat(reservationCancelledEvent.getReason(), is(notNullValue()));

        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID), 10000L), is(nullValue()));
    }
}
