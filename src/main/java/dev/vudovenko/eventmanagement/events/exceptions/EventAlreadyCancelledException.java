package dev.vudovenko.eventmanagement.events.exceptions;

public class EventAlreadyCancelledException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "The %s event has already been canceled";

    public EventAlreadyCancelledException(String eventName) {
        super(MESSAGE_TEMPLATE.formatted(eventName));
    }
}
