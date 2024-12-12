package dev.vudovenko.eventmanagement.eventRegistrations.exceptions;

public class AlreadyRegisteredForEventException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE
            = "You are already registered for event with id = %d. Duplicate registration is not allowed.";

    public AlreadyRegisteredForEventException(Long eventId) {
        super(MESSAGE_TEMPLATE.formatted(eventId));
    }
}
