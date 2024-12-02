package dev.vudovenko.eventmanagement.users.exceptions;

public class UserIdNotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "User with id %d not found";

    public UserIdNotFoundException(Long userId) {
        super(MESSAGE_TEMPLATE.formatted(userId));
    }
}
