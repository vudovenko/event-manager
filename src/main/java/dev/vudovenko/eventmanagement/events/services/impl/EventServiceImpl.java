package dev.vudovenko.eventmanagement.events.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.exceptions.*;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import dev.vudovenko.eventmanagement.security.authentication.AuthenticationService;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final AuthenticationService authenticationService;

    private final EntityMapper<Event, EventEntity> eventEntityMapper;

    @Transactional
    @Override
    public Event createEvent(Event event) {
        checkCorrectnessDate(event);
        checkAvailabilityPlaces(event);

        EventEntity createdEvent = eventRepository.save(
                eventEntityMapper.toEntity(event)
        );

        return eventEntityMapper.toDomain(createdEvent);
    }

    private static void checkCorrectnessDate(Event event) {
        if (event.getDate().isBefore(LocalDateTime.now())) {
            throw new DateEventInPastException(event.getDate());
        }
    }

    private void checkAvailabilityPlaces(Event event) {
        Long locationId = event.getLocation().getId();
        if (!locationService.existsById(locationId)) {
            throw new LocationNotFoundException(locationId);
        }

        Integer availablePlaces = locationService.getNumberAvailableSeats(locationId);

        if (event.getMaxPlaces() > availablePlaces) {
            throw new InsufficientSeatsException(availablePlaces);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }

    @Override
    public Event findById(Long id) {
        EventEntity eventEntity = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        return eventEntityMapper.toDomain(eventEntity);
    }

    @Override
    public Event findByIdWithOwner(Long eventId) {
        EventEntity eventEntity = eventRepository.findByIdWithOwner(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return eventEntityMapper.toDomain(eventEntity);
    }

    @Transactional
    @Override
    public void deleteEvent(Long eventId) {
        Event event = findByIdWithOwner(eventId);

        checkRightsToDeleteEvent(event);
        checkIfEventHasAlreadyBeenCanceled(event);
        checkIfEventHasStarted(event);

        event.setStatus(EventStatus.CANCELLED);

        eventRepository.save(eventEntityMapper.toEntity(event));
    }

    private void checkRightsToDeleteEvent(Event event) {
        User currentUser = authenticationService.getCurrentAuthenticatedUserOrThrow();
        if (currentUser.getRole().equals(UserRole.USER)
                && !event.getOwner().equals(currentUser)) {
            throw new UserNotEventCreatorException(currentUser.getLogin(), event.getName());
        }
    }

    private static void checkIfEventHasAlreadyBeenCanceled(Event event) {
        if (event.getStatus().equals(EventStatus.CANCELLED)) {
            throw new EventAlreadyCancelledException(event.getName());
        }
    }

    private static void checkIfEventHasStarted(Event event) {
        if (!event.getStatus().equals(EventStatus.WAIT_START)) {
            throw new CannotDeleteStartedEventException(event.getName(), event.getDate());
        }
    }

    @Transactional
    @Override
    public Event updateEvent(Long eventId, Event event) {
        throw new UnsupportedOperationException();
    }
}
