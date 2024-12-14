package dev.vudovenko.eventmanagement.eventRegistrations.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.domain.EventRegistration;
import dev.vudovenko.eventmanagement.eventRegistrations.entities.EventRegistrationEntity;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventRegistrationNotFoundException;
import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.eventRegistrations.services.validations.EventRegistrationValidationService;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventService eventService;

    private final EventRegistrationRepository eventRegistrationRepository;

    private final EventRegistrationValidationService eventRegistrationValidationService;

    private final EntityMapper<EventRegistration, EventRegistrationEntity> eventRegistrationEntityMapper;
    private final EntityMapper<Event, EventEntity> eventEntityMapper;

    @Transactional
    @Override
    public void registerForEvent(Long eventId, User user) {
        Event event = eventService.findById(eventId);

        eventRegistrationValidationService.checkReRegistration(eventId, user);
        eventRegistrationValidationService.checkThatEventStatusAllowsRegistration(event);
        eventRegistrationValidationService.checkAvailabilityPlace(event);

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

    @Transactional
    @Override
    public void cancelRegistration(Long eventId, User user) {
        Event event = eventService.findById(eventId);

        eventRegistrationValidationService.checkIfThereIsRegistrationForEvent(eventId, user);
        eventRegistrationValidationService.checkThatEventStatusAllowsCancelRegistration(event);

        eventRegistrationRepository.deleteByUserIdAndEventId(user.getId(), eventId);
        eventService.decreaseOccupiedPlaces(eventId);
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

    @Override
    public List<Event> getEventsInWhichUserIsRegistered(User user) {
        List<EventEntity> eventEntities = eventRegistrationRepository
                .findByUserId(user.getId())
                .stream()
                .map(EventRegistrationEntity::getEvent)
                .toList();

        return eventEntities
                .stream()
                .map(eventEntityMapper::toDomain)
                .toList();
    }
}
