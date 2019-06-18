package rt.sagas.reservation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import rt.sagas.reservation.repositories.ReservationRepository;

@Configuration
@ComponentScan(basePackages = "rt.sagas")
public class TestConfiguration {

    @Primary
    @Bean
    public ReservationRepositorySpy reservationRepositorySpy(ReservationRepository reservationRepository) {
        return new ReservationRepositorySpy(reservationRepository);
    }
}
