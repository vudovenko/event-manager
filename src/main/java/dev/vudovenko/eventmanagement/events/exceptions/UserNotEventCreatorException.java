package dev.vudovenko.eventmanagement.events.exceptions;

public class UserNotEventCreatorException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "The user with the username %s is not the creator of the event %s";

    public UserNotEventCreatorException(String login, String eventName) {
        super(MESSAGE_TEMPLATE.formatted(login, eventName));
    }
}
