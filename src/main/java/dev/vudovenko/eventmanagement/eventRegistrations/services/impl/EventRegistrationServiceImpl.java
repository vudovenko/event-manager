package dev.vudovenko.eventmanagement.eventRegistrations.services.impl;

import dev.vudovenko.eventmanagement.eventRegistrations.repositories.EventRegistrationRepository;
import dev.vudovenko.eventmanagement.eventRegistrations.services.EventRegistrationService;
import dev.vudovenko.eventmanagement.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventRegistrationRepository eventRegistrationRepository;

    @Override
    public void registerForEvent(Long eventId, User user) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
