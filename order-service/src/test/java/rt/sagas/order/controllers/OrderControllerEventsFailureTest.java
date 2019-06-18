package rt.sagas.order.controllers;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import rt.sagas.events.OrderCreatedEvent;
import rt.sagas.order.AbstractOrderTest;
import rt.sagas.order.JmsOrderCreatedEventReceiver;
import rt.sagas.order.OrderRepositorySpy;
import rt.sagas.order.entities.Order;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderControllerEventsFailureTest extends AbstractOrderTest {

    @Autowired
    private JmsOrderCreatedEventReceiver jmsOrderCreatedEventReceiver;
    @Autowired
    private OrderRepositorySpy orderRepositorySpy;

    @After
    public void tearDown() throws Exception {
        orderRepositorySpy.setThrowExceptionInSave(false);
        orderRepositorySpy.deleteAll();
    }

    @Test
    public void testOrderCreatedEventIsNotSentWhenTransactionIsRolledBack() throws Exception {
        orderRepositorySpy.setThrowExceptionInSave(true);

        mvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(convertToJson(
                        new Order(11L, "123212321232123212321"))))
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        assertThat(jmsOrderCreatedEventReceiver.pollEvent(
                e-> e.getCartNumber().equals("123212321232123212321"),
                10000L), is(nullValue()));
        assertThat(orderRepositorySpy.count(), is(0L));
    }

    @Test
    public void testOrderCreatedEventIsSentWhenTransactionIsNotRolledBack() throws Exception {

        mvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(convertToJson(
                        new Order(11L, "123456784444444444"))))
                .andExpect(status().is(HttpStatus.CREATED.value()));

        OrderCreatedEvent orderCreatedEvent = jmsOrderCreatedEventReceiver.pollEvent(
                e -> e.getCartNumber().equals("123456784444444444"), 20000L);
        assertThat(orderCreatedEvent, is(notNullValue()));
        assertThat(orderCreatedEvent.getUserId(), is(11L));

        assertThat(orderRepositorySpy.count(), is(1L));
        Order orderCreated = orderRepositorySpy.findAll().iterator().next();
        assertThat(orderCreated.getCartNumber(),
                is("123456784444444444"));
    }

}

