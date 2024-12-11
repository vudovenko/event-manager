package dev.vudovenko.eventmanagement.eventRegistrations.services;

import dev.vudovenko.eventmanagement.users.domain.User;

public interface EventRegistrationService {

    void registerForEvent(Long eventId, User user);
}
