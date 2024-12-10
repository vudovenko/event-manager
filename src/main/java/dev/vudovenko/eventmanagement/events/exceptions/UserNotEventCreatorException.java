package dev.vudovenko.eventmanagement.events.exceptions;

public class UserNotEventCreatorException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "The user with id = %d is not the creator of the event with id = %d";

    public UserNotEventCreatorException(Long userId, Long eventId) {
        super(MESSAGE_TEMPLATE.formatted(userId, eventId));
    }
}
