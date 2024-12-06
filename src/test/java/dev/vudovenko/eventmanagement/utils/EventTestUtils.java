package dev.vudovenko.eventmanagement.utils;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventTestUtils {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserTestUtils userTestUtils;
    @Autowired
    private LocationTestUtils locationTestUtils;

    public Event getCreatedEvent() {
        return eventService.createEvent(
                new Event(
                        null,
                        "event-" + RandomUtils.getRandomInt(),
                        userTestUtils.getRegisteredUser(),
                        50,
                        30,
                        LocalDateTime.now().plusDays(1),
                        1200,
                        60,
                        locationTestUtils.getCreatedLocation(),
                        EventStatus.WAIT_START
                )
        );
    }

    public EventCreateRequestDto getWrongEventCreateRequestDto() {
        return new EventCreateRequestDto(
                "   ",
                -10,
                null,
                -100,
                15,
                null
        );
    }
}
