package rt.sagas.events.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rt.sagas.events.TestEvent;
import rt.sagas.events.listeners.JmsTestEventReceiver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static rt.sagas.events.TestConfiguration.TEST_DESTINATION;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventServiceTest {

    @Autowired
    private EventSender unit;
    @Autowired
    private JmsTestEventReceiver testEventReceiver;

    private ObjectMapper objectMapper = new ObjectMapper();

    @After
    public void tearDown() {
        testEventReceiver.clear();
    }

    @Test
    public void testTransactionEventIsSentToTheQueue() throws Exception {
        unit.sendEvent(TEST_DESTINATION, new TestEvent("111111-1234-5678-AAABBBB"));

        TestEvent testEvent = testEventReceiver.pollEvent(
                e -> e.getEventMessage().equals("111111-1234-5678-AAABBBB"), 10000L);
        assertThat(testEvent, is(notNullValue()));
    }
}
