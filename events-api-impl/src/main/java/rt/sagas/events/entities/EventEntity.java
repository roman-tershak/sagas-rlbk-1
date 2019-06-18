package rt.sagas.events.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String destination;
    private String event;

    public EventEntity() {
    }

    public EventEntity(String destination, String event) {
        this.destination = destination;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public String getDestination() {
        return destination;
    }

    public String getEvent() {
        return event;
    }
}
