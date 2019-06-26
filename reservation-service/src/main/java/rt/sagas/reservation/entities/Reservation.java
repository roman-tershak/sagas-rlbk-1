package rt.sagas.reservation.entities;

import javax.persistence.*;

@Entity(name = "RESERVATIONS")
public class Reservation {

    @Id
    private String id;
    @Column(unique = true, length = 3)
    private Integer reservationNumber;
    @Column(unique = true)
    private Long orderId;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private String notes = "";

    public Reservation() {
    }

    public Reservation(String id, Integer reservationNumber, Long orderId, Long userId, ReservationStatus status) {
        this.id = id;
        this.reservationNumber = reservationNumber;
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Integer getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(Integer reservationNumber) {
        this.reservationNumber = reservationNumber;
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
        sb.append(", reservationNumber=").append(reservationNumber);
        sb.append(", orderId=").append(orderId);
        sb.append(", userId=").append(userId);
        sb.append(", status=").append(status);
        sb.append(", notes=").append(notes);
        sb.append('}');
        return sb.toString();
    }
}
