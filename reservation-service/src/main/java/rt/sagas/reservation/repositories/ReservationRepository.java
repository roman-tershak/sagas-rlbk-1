package rt.sagas.reservation.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import rt.sagas.reservation.entities.Reservation;

import java.util.Optional;

@Repository
public interface ReservationRepository extends CrudRepository<Reservation, String> {

    Optional<Reservation> findByOrderId(Long orderId);
}
