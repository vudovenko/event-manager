package dev.vudovenko.eventmanagement.users.exceptions;

public class OwnerEventNotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "The owner for the event with id = %d was not found";

    public OwnerEventNotFoundException(Long eventId) {
        super(MESSAGE_TEMPLATE.formatted(eventId));
    }
}
