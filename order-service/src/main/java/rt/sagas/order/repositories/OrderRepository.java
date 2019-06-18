package rt.sagas.order.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import rt.sagas.order.entities.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
}
