package dev.vudovenko.eventmanagement.eventRegistrations.exceptions;

import dev.vudovenko.eventmanagement.events.statuses.EventStatus;

public class EventStatusNotAllowedForRegistrationCancellationException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE
            = "The current event status does not allow registration cancellation. Event id: %d, status: %s";

    public EventStatusNotAllowedForRegistrationCancellationException(Long eventId, EventStatus eventStatus) {
        super(MESSAGE_TEMPLATE.formatted(eventId, eventStatus.name()));
    }
}

