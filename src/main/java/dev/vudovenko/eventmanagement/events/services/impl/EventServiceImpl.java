package dev.vudovenko.eventmanagement.events.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.exceptions.EventNotFoundException;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.services.validations.EventValidationService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final EventValidationService eventValidationService;

    private final EntityMapper<Event, EventEntity> eventEntityMapper;
    private final EntityMapper<User, UserEntity> userEntityMapper;

    @Transactional
    @Override
    public Event createEvent(Event event) {
        eventValidationService.checkCorrectnessDate(event);
        eventValidationService.checkAvailabilityLocationPlaces(event);

        EventEntity createdEvent = eventRepository.save(
                eventEntityMapper.toEntity(event)
        );

        return eventEntityMapper.toDomain(createdEvent);
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

        eventValidationService.checkRightsToManageEvent(event);
        eventValidationService.checkIfEventHasAlreadyBeenCanceled(event);
        eventValidationService.checkIfEventHasStarted(event);

        event.setStatus(EventStatus.CANCELLED);

        eventRepository.save(eventEntityMapper.toEntity(event));
    }

    @Transactional
    @Override
    public Event updateEvent(Long eventId, Event event) {
        Event notUpdatedEvent = findByIdWithOwner(eventId);
        initializeFieldsForEventUpdate(event, notUpdatedEvent);

        eventValidationService.checkRightsToManageEvent(event);
        eventValidationService.checkCorrectnessDate(event);
        eventValidationService.checkAvailabilityLocationPlaces(event);
        eventValidationService.checkThatOccupiedSeatsArePlacedInMaximumPlaces(event, notUpdatedEvent);

        EventEntity createdEvent = eventRepository.save(
                eventEntityMapper.toEntity(event)
        );

        return eventEntityMapper.toDomain(createdEvent);
    }

    private void initializeFieldsForEventUpdate(Event event, Event notUpdatedEvent) {
        event.setId(notUpdatedEvent.getId());
        event.setOwner(notUpdatedEvent.getOwner());
        event.setOccupiedPlaces(notUpdatedEvent.getOccupiedPlaces());
    }

    @Override
    public List<Event> searchEvents(
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
    ) {
        List<EventEntity> eventEntities = eventRepository.searchEvents(
                name,
                placeMin,
                placeMax,
                dateStartAfter,
                dateStartBefore,
                costMin,
                costMax,
                durationMin,
                durationMax,
                locationId,
                eventStatus
        );

        return eventEntities
                .stream()
                .map(eventEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> getUserEvents(User user) {
        List<EventEntity> allByOwner = eventRepository.findAllByOwner(
                userEntityMapper.toEntity(user)
        );

        return allByOwner
                .stream()
                .map(eventEntityMapper::toDomain)
                .toList();
    }

    @Transactional
    @Override
    public void increaseOccupiedPlaces(Long eventId) {
        eventRepository.increaseOccupiedPlaces(eventId);
    }

    @Transactional
    @Override
    public void decreaseOccupiedPlaces(Long eventId) {
        eventRepository.decreaseOccupiedPlaces(eventId);
    }

    @Transactional
    @Override
    public void updateEventStatuses() {
        LocalDateTime currentTime = LocalDateTime.now();
        eventRepository.updateEventStatusWhenItStarted(currentTime);
        eventRepository.updateEventStatusWhenItIsOver(currentTime);
    }
}
