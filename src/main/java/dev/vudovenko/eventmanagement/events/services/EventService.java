package dev.vudovenko.eventmanagement.events.services;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    Event createEvent(Event event);

    boolean existsById(Long id);

    Event findById(Long id);

    Event findByIdWithOwner(Long eventId);

    void deleteEvent(Long eventId);

    Event updateEvent(Long eventId, Event event);

    List<Event> searchEvents(
            String name,
            Integer placeMin,
            Integer placeMax,
            LocalDateTime dateStartAfter,
            LocalDateTime dateStartBefore,
            Integer costMin,
            Integer costMax,
            Integer durationMin,
            Integer durationMax,
            Long locationId,
            EventStatus eventStatus
    );

    List<Event> getUserEvents(User user);

    void increaseOccupiedPlaces(Long eventId);
}
