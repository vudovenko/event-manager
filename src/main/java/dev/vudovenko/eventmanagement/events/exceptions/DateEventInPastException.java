package dev.vudovenko.eventmanagement.events.exceptions;

import java.time.LocalDateTime;

public class DateEventInPastException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Date %s is in the past";

    public DateEventInPastException(LocalDateTime startDate) {
        super(MESSAGE_TEMPLATE.formatted(startDate));
    }
}