package dev.vudovenko.eventmanagement.scheduler;

import dev.vudovenko.eventmanagement.events.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventService eventService;

    @Scheduled(fixedRateString = "${scheduler.event.statuses.ISO}")
    public void updateEventStatuses() {
        log.info("Updating event statuses");

        eventService.updateEventStatuses();
    }
}
