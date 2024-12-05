package dev.vudovenko.eventmanagement.events.services;

import dev.vudovenko.eventmanagement.events.domain.Event;

public interface EventService {

    Event createEvent(Event event);

    boolean existsById(Long id);

    Event findById(Long id);
}
