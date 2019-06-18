package rt.sagas.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import rt.sagas.order.repositories.OrderRepository;

@Configuration
@ComponentScan(basePackages = "rt.sagas")
public class TestConfiguration {

    @Primary
    @Bean
    public OrderRepository orderRepositorySpy(OrderRepository orderRepository) {
        return new OrderRepositorySpy(orderRepository);
    }
}
