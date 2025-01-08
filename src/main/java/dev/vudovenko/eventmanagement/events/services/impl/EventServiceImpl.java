package dev.vudovenko.eventmanagement.events.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.events.changes.mappers.EventChangeDtoMapper;
import dev.vudovenko.eventmanagement.events.changes.senders.EventChangeSender;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.events.entity.EventEntity;
import dev.vudovenko.eventmanagement.events.exceptions.EventNotFoundException;
import dev.vudovenko.eventmanagement.events.repositories.EventRepository;
import dev.vudovenko.eventmanagement.events.services.EventService;
import dev.vudovenko.eventmanagement.events.services.validations.EventValidationService;
import dev.vudovenko.eventmanagement.events.statuses.EventStatus;
import dev.vudovenko.eventmanagement.security.authentication.AuthenticationService;
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

    private final EventValidationService eventValidationService;
    private final AuthenticationService authenticationService;

    private final EventChangeSender eventChangeSender;

    private final EventRepository eventRepository;

    private final EntityMapper<Event, EventEntity> eventEntityMapper;
    private final EntityMapper<User, UserEntity> userEntityMapper;

    private final EventChangeDtoMapper eventChangeDtoMapper;

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

        eventChangeSender.sendEvent(
                eventChangeDtoMapper.toDto(
                        notUpdatedEvent,
                        event,
                        authenticationService.getCurrentAuthenticatedUserOrThrow().getId(),
                        getEventParticipants(eventId)
                )
        );

        return eventEntityMapper.toDomain(createdEvent);
    }

    private void initializeFieldsForEventUpdate(Event event, Event notUpdatedEvent) {
        event.setId(notUpdatedEvent.getId());
        event.setOwner(notUpdatedEvent.getOwner());
        event.setOccupiedPlaces(notUpdatedEvent.getOccupiedPlaces());
    }

    @Override
    public List<Long> getEventParticipants(Long eventId) {
        return eventRepository.getEventParticipants(eventId);
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
        List<EventEntity> startedEvents = eventRepository
                .findAllByDateLessThanEqualAndStatus(currentTime, EventStatus.WAIT_START);
        startedEvents.forEach(
                eventEntity -> {
                    eventEntity.setStatus(EventStatus.STARTED);
                    eventRepository.save(eventEntity);
                }
        );
        sendEventStatusChangesToKafka(
                startedEvents
                        .stream()
                        .map(eventEntityMapper::toDomain)
                        .toList(),
                EventStatus.WAIT_START
        );

        List<EventEntity> finishedEvents = eventRepository.findAllEventsToFinish(currentTime);
        finishedEvents.forEach(
                eventEntity -> {
                    eventEntity.setStatus(EventStatus.FINISHED);
                    eventRepository.save(eventEntity);
                }
        );
        sendEventStatusChangesToKafka(
                finishedEvents
                        .stream()
                        .map(eventEntityMapper::toDomain)
                        .toList(),
                EventStatus.STARTED
        );
    }

    private void sendEventStatusChangesToKafka(List<Event> events, EventStatus oldStatus) {
        events.forEach(
                event -> {
                    eventChangeSender.sendEvent(
                            eventChangeDtoMapper.toDto(
                                    event,
                                    oldStatus,
                                    getEventParticipants(event.getId())
                            )
                    );
                }
        );
    }
}
