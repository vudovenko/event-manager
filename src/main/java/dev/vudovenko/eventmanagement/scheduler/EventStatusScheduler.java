package dev.vudovenko.eventmanagement.scheduler;

import dev.vudovenko.eventmanagement.events.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventService eventService;

    @Scheduled(fixedRateString = "PT01M")
    public void updateEventStatuses() {
        eventService.updateEventStatuses();
    }
}
