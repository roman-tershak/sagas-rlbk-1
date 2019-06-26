package rt.sagas.reservation;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import rt.sagas.reservation.entities.Reservation;
import rt.sagas.reservation.repositories.ReservationRepository;
import rt.sagas.testutils.TestRuntimeException;

import java.util.List;
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
    public <S extends Reservation> S saveAndFlush(S entity) {
        S saved = reservationRepository.saveAndFlush(entity);
        if (throwExceptionInSave)
            throw new TestRuntimeException("Intended exception - ignore");
        return saved;
    }

    @Override
    public <S extends Reservation> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = reservationRepository.saveAll(entities);
        if (throwExceptionInSave)
            throw new TestRuntimeException("Intended exception - ignore");
        return saved;
    }

    @Override
    public Optional<Reservation> findByOrderId(Long orderId) {
        return reservationRepository.findByOrderId(orderId);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> findAll(Sort sort) {
        return reservationRepository.findAll(sort);
    }

    @Override
    public Page<Reservation> findAll(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    @Override
    public List<Reservation> findAllById(Iterable<String> strings) {
        return reservationRepository.findAllById(strings);
    }

    @Override
    public long count() {
        return reservationRepository.count();
    }

    @Override
    public void deleteById(String s) {
        reservationRepository.deleteById(s);
    }

    @Override
    public void delete(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    @Override
    public void deleteAll(Iterable<? extends Reservation> iterable) {
        reservationRepository.deleteAll(iterable);
    }

    @Override
    public void deleteAll() {
        reservationRepository.deleteAll();
    }

    @Override
    public Optional<Reservation> findById(String s) {
        return reservationRepository.findById(s);
    }

    @Override
    public boolean existsById(String s) {
        return reservationRepository.existsById(s);
    }

    @Override
    public void flush() {
        reservationRepository.flush();
    }

    @Override
    public void deleteInBatch(Iterable<Reservation> entities) {
        reservationRepository.deleteInBatch(entities);
    }

    @Override
    public void deleteAllInBatch() {
        reservationRepository.deleteAllInBatch();
    }

    @Override
    public Reservation getOne(String s) {
        return reservationRepository.getOne(s);
    }

    @Override
    public <S extends Reservation> Optional<S> findOne(Example<S> example) {
        return reservationRepository.findOne(example);
    }

    @Override
    public <S extends Reservation> List<S> findAll(Example<S> example) {
        return reservationRepository.findAll(example);
    }

    @Override
    public <S extends Reservation> List<S> findAll(Example<S> example, Sort sort) {
        return reservationRepository.findAll(example, sort);
    }

    @Override
    public <S extends Reservation> Page<S> findAll(Example<S> example, Pageable pageable) {
        return reservationRepository.findAll(example, pageable);
    }

    @Override
    public <S extends Reservation> long count(Example<S> example) {
        return reservationRepository.count(example);
    }

    @Override
    public <S extends Reservation> boolean exists(Example<S> example) {
        return reservationRepository.exists(example);
    }

    public static void setThrowExceptionInSave(boolean throwException) {
        throwExceptionInSave = throwException;
    }
}
