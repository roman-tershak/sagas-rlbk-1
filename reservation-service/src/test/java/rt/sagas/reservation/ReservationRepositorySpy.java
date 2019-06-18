package rt.sagas.reservation;

import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.repositories.ReservationRepository;
import rt.sagas.testutils.TestRuntimeException;

import java.util.Optional;

public class ReservationRepositorySpy implements ReservationRepository {

    private ReservationRepository reservationRepository;
    private static boolean throwExceptionInSave = false;

    public ReservationRepositorySpy(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public <S extends Reservation> S save(S s) {
        S saved = reservationRepository.save(s);
        if (throwExceptionInSave)
            throw new TestRuntimeException("Intended exception - ignore");
        return saved;
    }

    @Override
    public <S extends Reservation> Iterable<S> saveAll(Iterable<S> iterable) {
        return reservationRepository.saveAll(iterable);
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return reservationRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return reservationRepository.existsById(id);
    }

    @Override
    public Iterable<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Iterable<Reservation> findAllById(Iterable<String> iterable) {
        return reservationRepository.findAllById(iterable);
    }

    @Override
    public Optional<Reservation> findByOrderId(Long orderId) {
        return reservationRepository.findByOrderId(orderId);
    }

    @Override
    public long count() {
        return reservationRepository.count();
    }

    @Override
    public void deleteById(String id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public void delete(Reservation Reservation) {
        reservationRepository.delete(Reservation);
    }

    @Override
    public void deleteAll(Iterable<? extends Reservation> iterable) {
        reservationRepository.deleteAll(iterable);
    }

    @Override
    public void deleteAll() {
        reservationRepository.deleteAll();
    }

    public void setThrowExceptionInSave(boolean throwExceptionInSave) {
        this.throwExceptionInSave = throwExceptionInSave;
    }
}
