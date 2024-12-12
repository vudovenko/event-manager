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
        return eventService.createEvent(getEvent());
    }

    public Event getCreatedEvent(User eventCreator) {
        Event event = getEvent();
        event.setOwner(eventCreator);
        return eventService.createEvent(event);
    }

    public Event getCreatedEvent(int occupiedPlaces, int maxPlaces, EventStatus eventStatus) {
        Event event = getEvent();
        event.setOccupiedPlaces(occupiedPlaces);
        event.setMaxPlaces(maxPlaces);
        event.setStatus(eventStatus);
        return eventService.createEvent(event);
    }

    public Event getCreatedEvent(
            int occupiedPlaces,
            int maxPlaces,
            Location location,
            User eventCreator
    ) {
        Event event = getEvent();
        event.setOwner(eventCreator);
        event.setOccupiedPlaces(occupiedPlaces);
        event.setMaxPlaces(maxPlaces);
        event.setLocation(location);
        return eventService.createEvent(event);
    }

    public Event getCreatedEvent(EventStatus status) {
        Event event = getEvent();
        event.setStatus(status);
        return eventService.createEvent(event);
    }

    private Event getEvent() {
        return new Event(
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
