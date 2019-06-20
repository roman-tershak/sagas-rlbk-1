package rt.sagas.reservation.listeners;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rt.sagas.events.CartAuthorizedEvent;
import rt.sagas.events.CartRejectedEvent;
import rt.sagas.events.ReservationCancelledEvent;
import rt.sagas.events.ReservationConfirmedEvent;
import rt.sagas.reservation.JmsReservationCancelledEventReceiver;
import rt.sagas.reservation.JmsReservationConfirmedEventReceiver;
import rt.sagas.reservation.ReservationRepositorySpy;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.entities.ReservationFactory;
import rt.sagas.testutils.JmsSender;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static rt.sagas.events.QueueNames.CART_AUTHORIZED_EVENT_QUEUE;
import static rt.sagas.events.QueueNames.CART_REJECTED_EVENT_QUEUE;
import static rt.sagas.reservation.entities.ReservationStatus.CANCELLED;
import static rt.sagas.reservation.entities.ReservationStatus.CONFIRMED;
import static rt.sagas.reservation.entities.ReservationStatus.PENDING;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartEventsListenerTest extends AbstractListenerTest {

    public static final Long ORDER_ID = 15L;
    public static final Long USER_ID = 11112L;
    public static final String CART_NUMBER = "2222543211234567";

    @Autowired
    private ReservationFactory reservationFactory;
    @Autowired
    private ReservationRepositorySpy reservationRepositorySpy;
    @Autowired
    private JmsSender jmsSender;
    @Autowired
    private JmsReservationConfirmedEventReceiver reservationConfirmedEventReceiver;
    @Autowired
    private JmsReservationCancelledEventReceiver reservationCancelledEventReceiver;

    @After
    public void setUp() {
        reservationRepositorySpy.setThrowExceptionInSave(false);
    }

    @After
    public void tearDown() {
        reservationRepositorySpy.setThrowExceptionInSave(false);
        super.tearDown();
        reservationConfirmedEventReceiver.clear();
    }

    @Test
    public void testReservationIsCompletedAndReservationCompletedEventIsSentWhenCartApprovedEventIsReceived() throws Exception {
        Reservation pendingReservation = reservationFactory.createNewPendingReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(pendingReservation);
        String reservationId = pendingReservation.getId();

        CartAuthorizedEvent cartAuthorizedEvent = new CartAuthorizedEvent(
                reservationId, CART_NUMBER, ORDER_ID, USER_ID);
        jmsSender.send(CART_AUTHORIZED_EVENT_QUEUE, cartAuthorizedEvent);

        Reservation reservation = waitAndGetReservationsByIdAndStatusFromDb(
                reservationId, CONFIRMED, 5000L);
        assertThat(reservation, is(notNullValue()));
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));

        ReservationConfirmedEvent reservationConfirmedEvent = reservationConfirmedEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId));
        assertThat(reservationConfirmedEvent, is(notNullValue()));
        assertThat(reservationConfirmedEvent.getReservationId(), is(pendingReservation.getId()));
        assertThat(reservationConfirmedEvent.getOrderId(), is(ORDER_ID));
        assertThat(reservationConfirmedEvent.getUserId(), is(USER_ID));

        assertThat(reservationCancelledEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId), 5000L), is(nullValue()));
    }

    @Test
    public void testReservationCompletedEventIsNotSentTwiceWhenCartApprovedEventAreDoubled() throws Exception {
        Reservation pendingReservation = reservationFactory.createNewPendingReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(pendingReservation);
        String reservationId = pendingReservation.getId();

        CartAuthorizedEvent cartAuthorizedEvent = new CartAuthorizedEvent(
                reservationId, CART_NUMBER, ORDER_ID, USER_ID);
        jmsSender.send(CART_AUTHORIZED_EVENT_QUEUE, cartAuthorizedEvent);
        jmsSender.send(CART_AUTHORIZED_EVENT_QUEUE, cartAuthorizedEvent);

        Reservation reservation = waitAndGetReservationsByIdAndStatusFromDb(
                reservationId, CONFIRMED, 5000L);
        assertThat(reservation, is(notNullValue()));
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));

        assertThat(reservationConfirmedEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId)), is(notNullValue()));
        assertThat(reservationConfirmedEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId)), is(nullValue()));

        assertThat(reservationCancelledEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId), 5000L), is(nullValue()));
    }

    @Test
    public void testReservationConfirmedEventIsNotSentWhenExceptionOccurs() throws Exception {
        Reservation pendingReservation = reservationFactory.createNewPendingReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(pendingReservation);
        String reservationId = pendingReservation.getId();

        reservationRepositorySpy.setThrowExceptionInSave(true);

        CartAuthorizedEvent cartAuthorizedEvent = new CartAuthorizedEvent(
                reservationId, CART_NUMBER, ORDER_ID, USER_ID);
        jmsSender.send(CART_AUTHORIZED_EVENT_QUEUE, cartAuthorizedEvent);

        assertThat(reservationConfirmedEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId), 5000L), is(nullValue()));

        Reservation reservation = waitAndGetReservationsByIdAndStatusFromDb(
                reservationId, PENDING, 0);
        assertThat(reservation, is(notNullValue()));
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));
    }

    @Test
    public void testReservationIsCancelledAndReservationCancelledEventIsSentWhenCartRejectedEventIsReceived() throws Exception {
        Reservation pendingReservation = reservationFactory.createNewPendingReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(pendingReservation);
        String reservationId = pendingReservation.getId();

        CartRejectedEvent cartRejectedEvent = new CartRejectedEvent(
                reservationId, CART_NUMBER, ORDER_ID, USER_ID, "Bad cart number");
        jmsSender.send(CART_REJECTED_EVENT_QUEUE, cartRejectedEvent);

        Reservation reservation = waitAndGetReservationsByIdAndStatusFromDb(
                reservationId, CANCELLED, 5000L);
        assertThat(reservation, is(notNullValue()));
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));
        assertThat(reservation.getStatus(), is(CANCELLED));
        assertThat(reservation.getNotes(), is("Bad cart number"));

        ReservationCancelledEvent reservationCancelledEvent = reservationCancelledEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId));
        assertThat(reservationCancelledEvent, is(notNullValue()));
        assertThat(reservationCancelledEvent.getReservationId(), is(pendingReservation.getId()));
        assertThat(reservationCancelledEvent.getOrderId(), is(ORDER_ID));
        assertThat(reservationCancelledEvent.getUserId(), is(USER_ID));
        assertThat(reservationCancelledEvent.getReason(), is("Bad cart number"));

        assertThat(reservationConfirmedEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId), 5000L), is(nullValue()));
    }

    @Test
    public void testReservationCancelledEventIsNotSentTwiceWhenCartRejectedEventAreDoubled() throws Exception {
        Reservation pendingReservation = reservationFactory.createNewPendingReservationFor(ORDER_ID, USER_ID);
        reservationRepository.save(pendingReservation);
        String reservationId = pendingReservation.getId();

        CartRejectedEvent cartRejectedEvent = new CartRejectedEvent(
                reservationId, CART_NUMBER, ORDER_ID, USER_ID, "Very bad cart number");
        jmsSender.send(CART_REJECTED_EVENT_QUEUE, cartRejectedEvent);
        jmsSender.send(CART_REJECTED_EVENT_QUEUE, cartRejectedEvent);

        Reservation reservation = waitAndGetReservationsByIdAndStatusFromDb(
                reservationId, CANCELLED, 5000L);
        assertThat(reservation, is(notNullValue()));
        assertThat(reservation.getOrderId(), is(ORDER_ID));
        assertThat(reservation.getUserId(), is(USER_ID));

        assertThat(reservationCancelledEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId)), is(notNullValue()));
        assertThat(reservationCancelledEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId)), is(nullValue()));

        assertThat(reservationConfirmedEventReceiver.pollEvent(
                e -> e.getReservationId().equals(reservationId), 5000L), is(nullValue()));
    }

}
