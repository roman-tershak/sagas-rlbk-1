package rt.sagas.events;

public class TestEvent extends SagaEvent {

    private String eventMessage;

    public TestEvent() {
    }

    public TestEvent(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    public String getEventMessage() {
        return eventMessage;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestEvent{");
        sb.append("eventMessage='").append(eventMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
