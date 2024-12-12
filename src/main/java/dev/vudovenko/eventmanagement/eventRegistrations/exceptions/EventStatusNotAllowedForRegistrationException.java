package dev.vudovenko.eventmanagement.eventRegistrations.exceptions;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;

public class EventStatusNotAllowedForRegistrationException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE
            = "Event registration for event with id %d is not allowed. Event status: %s";

    public EventStatusNotAllowedForRegistrationException(Long eventId, EventStatus eventStatus) {
        super(MESSAGE_TEMPLATE.formatted(eventId, eventStatus.name()));
    }
}
