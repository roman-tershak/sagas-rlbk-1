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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static rt.sagas.events.QueueNames.ORDER_CREATED_EVENT_QUEUE;
import static rt.sagas.reservation.entities.ReservationStatus.DECLINED;
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
        when(reservationFactory.getReservationNumber())
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
                e -> e.getOrderId().equals(reservation.getOrderId()));
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
                e -> e.getOrderId().equals(reservation.getOrderId())), is(notNullValue()));
        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(reservation.getOrderId())), is(nullValue()));
    }

    @Test
    public void testPendingReservationIsNotCreatedIfItHasAlreadyBeenCreatedForAGivenOrderId() throws Exception {
        Reservation pendingReservation = reservationFactory.createNewReservationFor(ORDER_ID, USER_ID);
        pendingReservation.setStatus(PENDING);
        reservationRepository.save(pendingReservation);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID)), is(nullValue()));

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
        Reservation otherPendingReservation = reservationFactory.createNewReservationFor(
                OTHER_ORDER_ID, OTHER_USER_ID);
        otherPendingReservation.setStatus(PENDING);
        otherPendingReservation.setReservationNumber(77);
        reservationRepository.save(otherPendingReservation);

        doReturn(77).when(reservationFactory).getReservationNumber();

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        Reservation reservation = waitAndGetReservationsByOrderIdAndStatusFromDb(
                ORDER_ID, DECLINED, 10000L);
        assertThat(reservation, is(notNullValue()));
        assertThat(reservation.getId(), is(notNullValue()));
        assertThat(reservation.getId(), is(not(otherPendingReservation.getId())));
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));
        assertThat(reservation.getStatus(), is(DECLINED));
        assertThat(reservation.getNotes(), is(notNullValue()));
        assertThat(reservation.getReservationNumber(), is(nullValue()));

        ReservationCancelledEvent reservationCancelledEvent = reservationCancelledEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID),10000L);
        assertThat(reservationCancelledEvent, is(notNullValue()));
        assertThat(reservationCancelledEvent.getReservationId(), is(nullValue()));
        assertThat(reservationCancelledEvent.getOrderId(), is(ORDER_ID));
        assertThat(reservationCancelledEvent.getUserId(), is(USER_ID));
        assertThat(reservationCancelledEvent.getReason(), is(notNullValue()));

        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID)), is(nullValue()));
    }

    @Test
    public void testANewReservationGetsDeclinedIfReservationNumberDuplicates() throws Exception {
        Reservation otherPendingReservation = reservationFactory.createNewReservationFor(
                OTHER_ORDER_ID, OTHER_USER_ID);
        otherPendingReservation.setStatus(PENDING);
        otherPendingReservation.setReservationNumber(88);
        reservationRepository.save(otherPendingReservation);

        Reservation newReservation = reservationFactory.createNewReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(newReservation);

        doReturn(88).when(reservationFactory).getReservationNumber();

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        Reservation reservation = waitAndGetReservationsByOrderIdAndStatusFromDb(
                ORDER_ID, DECLINED, 10000L);
        assertThat(reservation, is(notNullValue()));
        assertThat(reservation.getId(), is(newReservation.getId()));
        assertThat(reservation.getId(), is(not(otherPendingReservation.getId())));
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));
        assertThat(reservation.getStatus(), is(DECLINED));
        assertThat(reservation.getNotes(), is(notNullValue()));
        assertThat(reservation.getReservationNumber(), is(nullValue()));

        ReservationCancelledEvent reservationCancelledEvent = reservationCancelledEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID));
        assertThat(reservationCancelledEvent, is(notNullValue()));
        assertThat(reservationCancelledEvent.getReservationId(), is(nullValue()));
        assertThat(reservationCancelledEvent.getOrderId(), is(ORDER_ID));
        assertThat(reservationCancelledEvent.getUserId(), is(USER_ID));
        assertThat(reservationCancelledEvent.getReason(), is(notNullValue()));

        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID)), is(nullValue()));
    }

    @Test
    public void testANewReservationGetsPendingOnOrderCreatedEvent() throws Exception {
        Reservation reservation = reservationFactory.createNewReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(reservation);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        assertThat(reservationCancelledEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID)), is(nullValue()));

        ReservationCreatedEvent reservationCreatedEvent = reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID));
        assertThat(reservationCreatedEvent, is(notNullValue()));
        assertThat(reservationCreatedEvent.getReservationId(), is(reservation.getId()));
        assertThat(reservationCreatedEvent.getOrderId(), is(ORDER_ID));
        assertThat(reservationCreatedEvent.getUserId(), is(USER_ID));
        assertThat(reservationCreatedEvent.getCartNumber(), is(CART_NUMBER));

        Reservation reservationFromDb = waitAndGetReservationsByOrderIdAndStatusFromDb(
                ORDER_ID, PENDING, 0);
        assertThat(reservationFromDb, is(notNullValue()));
        assertThat(reservationFromDb.getId(), is(reservation.getId()));
        assertThat(reservationFromDb.getOrderId(), is(reservation.getOrderId()));
        assertThat(reservationFromDb.getUserId(), is(reservation.getUserId()));
        assertThat(reservationFromDb.getStatus(), is(PENDING));
        assertThat(reservationFromDb.getReservationNumber(), is(notNullValue()));
        assertThat(reservationFromDb.getNotes(), is(nullValue()));
    }

    @Test
    public void testReservationEventsAreNotSentIfReservationHasAlreadyBeenDeclined() throws Exception {
        Reservation reservation = reservationFactory.createNewReservationFor(ORDER_ID, USER_ID);
        reservation.setStatus(DECLINED);
        reservationRepository.save(reservation);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(ORDER_ID, USER_ID, CART_NUMBER);
        jmsSender.send(ORDER_CREATED_EVENT_QUEUE, orderCreatedEvent);

        assertThat(reservationCreatedEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID)), is(nullValue()));
        assertThat(reservationCancelledEventReceiver.pollEvent(
                e -> e.getOrderId().equals(ORDER_ID)), is(nullValue()));

        Reservation reservationFromDb = waitAndGetReservationsByOrderIdAndStatusFromDb(
                ORDER_ID, DECLINED, 0);
        assertThat(reservationFromDb, is(notNullValue()));
        assertThat(reservationFromDb.getId(), is(reservation.getId()));
        assertThat(reservationFromDb.getOrderId(), is(reservation.getOrderId()));
        assertThat(reservationFromDb.getUserId(), is(reservation.getUserId()));
        assertThat(reservationFromDb.getStatus(), is(DECLINED));
    }

}
