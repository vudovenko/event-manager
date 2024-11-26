package dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages;

import lombok.Getter;

@Getter
public enum ExceptionHandlerMessages {

    ENTITY_NOT_FOUND("Entity not found"),
    VALIDATION_FAILED("Request validation failed"),
    SERVER_ERROR("Server error"),
    LOCATION_CAPACITY_IS_LOWER_THAN_IT_WAS("Location capacity is lower than it was");

    private final String message;

    ExceptionHandlerMessages(String message) {
        this.message = message;
    }
}
