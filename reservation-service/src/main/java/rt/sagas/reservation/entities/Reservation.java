package rt.sagas.reservation.entities;

import javax.persistence.*;

@Entity(name = "RESERVATIONS")
public class Reservation {

    @Id
    private String id;
    @Column(unique = true)
    private Long orderId;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private String notes = "";

    public Reservation() {
    }

    public Reservation(String id, Long orderId, Long userId, ReservationStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
    }

    public Reservation(String id, Long orderId, Long userId, ReservationStatus status, String notes) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Reservation{");
        sb.append("id='").append(id).append('\'');
        sb.append(", userId=").append(userId);
        sb.append(", orderId=").append(orderId);
        sb.append(", status=").append(status);
        sb.append(", notes=").append(notes);
        sb.append('}');
        return sb.toString();
    }
}
