package rt.sagas.events.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestEventLoopScheduler {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private EventService eventService;

    @Scheduled(fixedDelayString = "${application.event-loop-scheduler.fixed-delay-ms:5000}")
    public void scheduleEventSend() {
        LOGGER.info("scheduleEventSend called");

        eventService.sendOutgoingEvents();
    }
}
