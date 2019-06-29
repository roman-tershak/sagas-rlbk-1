package rt.sagas.reservation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import rt.sagas.reservation.services.ReservationFactory;
import rt.sagas.reservation.repositories.ReservationRepository;

import static org.mockito.Mockito.spy;

@Configuration
@ComponentScan(basePackages = "rt.sagas")
public class TestConfiguration {

    @Primary
    @Bean
    public ReservationRepositorySpy reservationRepositorySpy(ReservationRepository reservationRepository) {
        return new ReservationRepositorySpy(reservationRepository);
    }

    @Primary
    @Bean
    public ReservationFactory reservationFactorySpy(ReservationFactory reservationFactory) {
        return spy(reservationFactory);
    }
}
