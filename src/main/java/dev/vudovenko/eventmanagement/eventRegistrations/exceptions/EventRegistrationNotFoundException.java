package dev.vudovenko.eventmanagement.eventRegistrations.exceptions;

public class EventRegistrationNotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE
            = "Event registration for user with id %d and event with id %d not found";

    public EventRegistrationNotFoundException(Long userId, Long eventId) {
        super(MESSAGE_TEMPLATE.formatted(userId, eventId));
    }
}
