package rt.sagas.reservation.exceptions;

public class ReservationException extends Exception {

    public ReservationException(String message) {
        super(message);
    }

    public ReservationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReservationException(Throwable cause) {
        super(cause);
    }
}
