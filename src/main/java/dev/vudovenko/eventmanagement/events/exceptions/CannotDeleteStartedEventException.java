package dev.vudovenko.eventmanagement.events.exceptions;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CannotDeleteStartedEventException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "The %s event was started by %s";

    public CannotDeleteStartedEventException(String eventName, LocalDateTime startDate) {
        super(MESSAGE_TEMPLATE.formatted(eventName, startDate.truncatedTo(ChronoUnit.SECONDS)));
    }
}
