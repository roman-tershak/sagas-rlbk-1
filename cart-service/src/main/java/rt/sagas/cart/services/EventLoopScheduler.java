package rt.sagas.cart.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rt.sagas.events.services.EventService;

@Component
public class EventLoopScheduler {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private EventService eventService;

    @Scheduled(fixedDelayString = "${application.event-loop-scheduler.fixed-delay-ms:5000}")
    public void scheduleEventSend() {
        LOGGER.info("Event Loop Scheduler called");

        eventService.sendOutgoingEvents();
    }
}
