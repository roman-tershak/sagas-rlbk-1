package rt.sagas.cart.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import rt.sagas.cart.entities.Transaction;

import java.util.Optional;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    Optional<Transaction> findByOrderId(Long orderId);
}
