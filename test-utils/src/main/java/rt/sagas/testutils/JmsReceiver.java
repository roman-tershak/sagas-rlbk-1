package rt.sagas.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.Payload;
import rt.sagas.events.SagaEvent;

import javax.jms.TextMessage;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class JmsReceiver<E extends SagaEvent> {

    public static final long DEFAULT_TIMEOUT = 10000L;

    private LinkedBlockingQueue<E> events = new LinkedBlockingQueue<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Class<E> eventClass;

    public JmsReceiver(Class<E> eventClass) {
        this.eventClass = eventClass;
    }

    public abstract void receiveMessage(@Payload TextMessage textMessage) throws Exception;

    public void receiveMessage(String message) throws Exception {
        E event = objectMapper.readValue(message, eventClass);
        addEvent(event);
    }

    public void addEvent(E event) {
        events.add(event);
    }

    public E pollEvent() throws InterruptedException {
        return pollEvent(DEFAULT_TIMEOUT);
    }

    public E pollEvent(long timeout) throws InterruptedException {
        return events.poll(timeout, TimeUnit.MILLISECONDS);
    }

    public E pollEvent(Predicate<E> predicate) throws InterruptedException {
        return pollEvent(predicate, DEFAULT_TIMEOUT);
    }

    public E pollEvent(Predicate<E> predicate, long timeout) throws InterruptedException {
        E event = null;
        long stop = System.currentTimeMillis() + timeout;
        do {
            Optional<E> optional = events.stream().filter(predicate).findFirst();
            if (optional.isPresent()) {
                event = optional.get();
                events.remove(event);
                break;
            } else {
                Thread.sleep(50L);
            }
        } while (System.currentTimeMillis() < stop);

        return event;
    }

    public void clear() {
        events.clear();
    }
}
