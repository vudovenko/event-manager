package dev.vudovenko.eventmanagement.events.exceptions;

public class EventNotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Event with id %d not found";

    public EventNotFoundException(Long id) {
        super(MESSAGE_TEMPLATE.formatted(id));
    }
}