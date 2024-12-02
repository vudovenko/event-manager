package dev.vudovenko.eventmanagement.users.exceptions;

public class LoginAlreadyTakenException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Login %s is already taken";

    public LoginAlreadyTakenException(String takenLogin) {
        super(MESSAGE_TEMPLATE.formatted(takenLogin));
    }
}
