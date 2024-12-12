package dev.vudovenko.eventmanagement.eventRegistrations.services;

import dev.vudovenko.eventmanagement.eventRegistrations.domain.EventRegistration;
import dev.vudovenko.eventmanagement.users.domain.User;

public interface EventRegistrationService {

    void registerForEvent(Long eventId, User user);

    EventRegistration findByUserIdAndEventId(Long userId, Long eventId);

    boolean isUserRegisteredForEvent(Long userId, Long eventId);
}
