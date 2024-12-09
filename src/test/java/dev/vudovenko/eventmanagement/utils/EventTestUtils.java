package dev.vudovenko.eventmanagement.utils;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventCreateRequestDto;
import dev.vudovenko.eventmanagement.events.dto.EventUpdateRequestDto;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.users.domain.User;
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

    public Event getCreatedEvent(User eventCreator) {
        return eventService.createEvent(
                new Event(
                        null,
                        "event-" + RandomUtils.getRandomInt(),
                        eventCreator,
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

    public Event getCreatedEvent(int occupiedPlaces, int maxPlaces, Location location, User eventCreator) {
        return eventService.createEvent(
                new Event(
                        null,
                        "event-" + RandomUtils.getRandomInt(),
                        eventCreator,
                        maxPlaces,
                        occupiedPlaces,
                        LocalDateTime.now().plusDays(1),
                        1200,
                        60,
                        location,
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

    public EventUpdateRequestDto getWrongEventUpdateRequestDto() {
        return new EventUpdateRequestDto(
                "   ",
                -10,
                null,
                -100,
                15,
                null
        );
    }
}
