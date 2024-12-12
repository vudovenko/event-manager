package dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages;

import lombok.Getter;

@Getter
public enum ExceptionHandlerMessages {
    // todo вынести в отдельные классы
    ENTITY_NOT_FOUND("Entity not found"),
    VALIDATION_FAILED("Request validation failed"),
    SERVER_ERROR("Server error"),
    LOCATION_CAPACITY_IS_LOWER_THAN_IT_WAS("Location capacity is lower than it was"),
    USERNAME_NOT_FOUND("Username not found"),
    USER_ID_NOT_FOUND("User id not found"),
    LOGIN_ALREADY_TAKEN("Login already taken"),
    FAILED_TO_AUTHENTICATE("Failed to authenticate"),
    FORBIDDEN("Forbidden"),
    DATE_TIME_PARSE_EXCEPTION("Date time parse exception"),
    DATE_EVENT_IN_PAST("Date event in past"),
    INSUFFICIENT_SEATS("Insufficient capacity"),
    USER_NOT_EVENT_CREATOR("User not event creator"),
    CANNOT_DELETE_STARTED_EVENT("Cannot delete started event"),
    EVENT_ALREADY_CANCELLED("Event already cancelled"),
    LOCATION_CAPACITY_EXCEEDED("Location capacity exceeded"),
    EVENT_OCCUPIED_PLACES_EXCEEDED_MAXIMUM_CAPACITY("Event occupied places exceeded maximum capacity"),
    OWNER_EVENT_NOT_FOUND("The owner for the event has not been found"),
    EVENT_REGISTRATION_NOT_FOUND("Event registration not found"),
    EVENT_REGISTRATION_ALREADY_EXISTS("Event registration already exists"),
    EVENT_STATUS_NOT_ALLOWED_FOR_REGISTRATION("Event status not allowed for registration");


    private final String message;

    ExceptionHandlerMessages(String message) {
        this.message = message;
    }
}
