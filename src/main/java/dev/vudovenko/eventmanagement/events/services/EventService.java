package dev.vudovenko.eventmanagement.events.services;

import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.dto.EventSearchRequestDto;

import java.util.List;

public interface EventService {

    Event createEvent(Event event);

    boolean existsById(Long id);

    Event findById(Long id);

    Event findByIdWithOwner(Long eventId);

    void deleteEvent(Long eventId);

    Event updateEvent(Long eventId, Event event);

    List<Event> searchEvents(EventSearchRequestDto eventSearchRequestDto);
}
