package dev.vudovenko.eventmanagement.eventRegistrations.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.domain.EventRegistration;
import dev.vudovenko.eventmanagement.eventRegistrations.entities.EventRegistrationEntity;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.AlreadyRegisteredForEventException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventRegistrationNotFoundException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventStatusNotAllowedForRegistrationException;
import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.exceptions.InsufficientSeatsException;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventService eventService;
    private final EventRegistrationRepository eventRegistrationRepository;

    private final EntityMapper<EventRegistration, EventRegistrationEntity> eventRegistrationEntityMapper;

    @Transactional
    @Override
    public void registerForEvent(Long eventId, User user) {
        Event event = eventService.findById(eventId);

        checkReRegistration(eventId, user);
        checkThatEventStatusAllowsRegistration(eventId, event);
        checkAvailabilityPlace(event);

        EventRegistration eventRegistration = new EventRegistration(
                null,
                user,
                event
        );

        eventRegistrationRepository.save(
                eventRegistrationEntityMapper.toEntity(eventRegistration)
        );

        eventService.increaseOccupiedPlaces(eventId);
    }

    private static void checkAvailabilityPlace(Event event) {
        int availablePlaces = event.getMaxPlaces() - (event.getOccupiedPlaces() + 1);

        if (availablePlaces <= 0) {
            throw new InsufficientSeatsException(0);
        }
    }

    private static void checkThatEventStatusAllowsRegistration(Long eventId, Event event) {
        if (event.getStatus().equals(EventStatus.CANCELLED)
                || event.getStatus().equals(EventStatus.FINISHED)) {
            throw new EventStatusNotAllowedForRegistrationException(eventId, event.getStatus());
        }
    }

    private void checkReRegistration(Long eventId, User user) {
        if (eventRegistrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new AlreadyRegisteredForEventException(eventId);
        }
    }

    @Override
    public EventRegistration findByUserIdAndEventId(Long userId, Long eventId) {
        EventRegistrationEntity eventRegistrationEntity = eventRegistrationRepository
                .findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new EventRegistrationNotFoundException(userId, eventId));

        return eventRegistrationEntityMapper.toDomain(eventRegistrationEntity);
    }

    @Override
    public boolean isUserRegisteredForEvent(Long userId, Long eventId) {
        return eventRegistrationRepository.existsByUserIdAndEventId(userId, eventId);
    }
}
