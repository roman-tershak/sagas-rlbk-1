package rt.sagas.events.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rt.sagas.events.TestEvent;
import rt.sagas.events.entities.EventEntity;
import rt.sagas.events.listeners.JmsTestEventReceiver;
import rt.sagas.events.repositories.EventRepository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static rt.sagas.events.TestConfiguration.TEST_DESTINATION;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventServiceTest {

    @Autowired
    private EventService unit;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private JmsTestEventReceiver cartAuthorizedEventReceiver;

    private ObjectMapper objectMapper = new ObjectMapper();

    @After
    public void tearDown() {
        eventRepository.deleteAll();
        cartAuthorizedEventReceiver.clear();
    }

    @Test
    public void testTransactionEventIsStoredIntoDb() throws Exception {
        TestEvent event = new TestEvent("123456-1234-5678-ABCDEF");
        unit.storeOutgoingEvent(TEST_DESTINATION, event);

        assertThat(eventRepository.count(), is(1L));
        EventEntity eventEntity = eventRepository.findAll().iterator().next();
        TestEvent entityEventFromDb = objectMapper.readValue(eventEntity.getEvent(),
                TestEvent.class);
        assertThat(entityEventFromDb.getEventMessage(), is("123456-1234-5678-ABCDEF"));
    }

    @Test
    public void testTransactionEventIsSentToTheQueue() throws Exception {
        unit.storeOutgoingEvent(TEST_DESTINATION, new TestEvent("111111-1234-5678-AAABBBB"));

        TestEvent cartAuthorizedEvent = cartAuthorizedEventReceiver.pollEvent(
                e -> e.getEventMessage().equals("111111-1234-5678-AAABBBB"), 10000L);
        assertThat(cartAuthorizedEvent, is(notNullValue()));
    }

    @Test
    public void testMoreThanOneTransactionEventsAreSentToTheQueueAndEventsAreRemovedFromTheDb() throws Exception {
        unit.storeOutgoingEvent(TEST_DESTINATION, new TestEvent("111111-1234-5678-AAAAAAA"));
        unit.storeOutgoingEvent(TEST_DESTINATION, new TestEvent("111111-1234-5678-BBBBBBB"));

        assertThat(cartAuthorizedEventReceiver.pollEvent(
                e -> e.getEventMessage().equals("111111-1234-5678-AAAAAAA"), 10000L),
                is(notNullValue()));
        assertThat(cartAuthorizedEventReceiver.pollEvent(
                e -> e.getEventMessage().equals("111111-1234-5678-BBBBBBB"), 10000L),
                is(notNullValue()));

        assertThat(eventRepository.count(), is(0L));
    }

}
