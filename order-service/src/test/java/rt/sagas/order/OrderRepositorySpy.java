package rt.sagas.order;

import rt.sagas.order.entities.Order;
import rt.sagas.order.repositories.OrderRepository;
import rt.sagas.testutils.TestRuntimeException;

import java.util.Optional;

public class OrderRepositorySpy implements OrderRepository {

    private OrderRepository orderRepository;
    private static boolean throwExceptionInSave = false;

    public OrderRepositorySpy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public <S extends Order> S save(S s) {
        S saved = orderRepository.save(s);
        if (throwExceptionInSave)
            throw new TestRuntimeException("Intended exception - ignore");
        return saved;
    }

    @Override
    public <S extends Order> Iterable<S> saveAll(Iterable<S> iterable) {
        return orderRepository.saveAll(iterable);
    }

    @Override
    public Optional<Order> findById(Long aLong) {
        return orderRepository.findById(aLong);
    }

    @Override
    public boolean existsById(Long aLong) {
        return orderRepository.existsById(aLong);
    }

    @Override
    public Iterable<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Iterable<Order> findAllById(Iterable<Long> iterable) {
        return orderRepository.findAllById(iterable);
    }

    @Override
    public long count() {
        return orderRepository.count();
    }

    @Override
    public void deleteById(Long aLong) {
        orderRepository.deleteById(aLong);
    }

    @Override
    public void delete(Order order) {
        orderRepository.delete(order);
    }

    @Override
    public void deleteAll(Iterable<? extends Order> iterable) {
        orderRepository.deleteAll(iterable);
    }

    @Override
    public void deleteAll() {
        orderRepository.deleteAll();
    }

    public void setThrowExceptionInSave(boolean throwExceptionInSave) {
        this.throwExceptionInSave = throwExceptionInSave;
    }
}
