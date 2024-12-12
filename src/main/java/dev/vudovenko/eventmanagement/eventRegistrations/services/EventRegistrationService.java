package dev.vudovenko.eventmanagement.eventRegistrations.services;

import dev.vudovenko.eventmanagement.eventRegistrations.domain.EventRegistration;
import dev.vudovenko.eventmanagement.events.domain.Event;
import dev.vudovenko.eventmanagement.users.domain.User;

import java.util.List;

public interface EventRegistrationService {

    void registerForEvent(Long eventId, User user);

    EventRegistration findByUserIdAndEventId(Long userId, Long eventId);

    boolean isUserRegisteredForEvent(Long userId, Long eventId);

    void cancelRegistration(Long eventId, User user);

    List<Event> getEventsInWhichUserIsRegistered(User user);
}
