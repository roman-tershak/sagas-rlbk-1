package rt.sagas.events;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestConfiguration {

    public static final String TEST_DESTINATION = "testDestination";

}
