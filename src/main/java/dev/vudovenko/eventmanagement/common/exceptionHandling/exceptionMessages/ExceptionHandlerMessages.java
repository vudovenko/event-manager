package dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages;

import lombok.Getter;

@Getter
public enum ExceptionHandlerMessages {

    ENTITY_NOT_FOUND("Entity not found"),
    VALIDATION_FAILED("Request validation failed"),
    SERVER_ERROR("Server error");

    private final String message;

    ExceptionHandlerMessages(String message) {
        this.message = message;
    }
}
