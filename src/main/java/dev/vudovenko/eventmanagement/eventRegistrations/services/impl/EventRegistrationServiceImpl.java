package dev.vudovenko.eventmanagement.eventRegistrations.services.impl;

import dev.vudovenko.eventmanagement.common.mappers.EntityMapper;
import dev.vudovenko.eventmanagement.eventRegistrations.domain.EventRegistration;
import dev.vudovenko.eventmanagement.eventRegistrations.entities.EventRegistrationEntity;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventRegistrationNotFoundException;
import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventRegistrationRepository eventRegistrationRepository;

    private final EntityMapper<EventRegistration, EventRegistrationEntity> eventRegistrationEntityMapper;

    @Override
    public void registerForEvent(Long eventId, User user) {
        throw new UnsupportedOperationException("Not implemented yet");
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
