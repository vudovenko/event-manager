package dev.vudovenko.eventmanagement.events.exceptions.exceptionHandlers;

import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.events.exceptions.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.format.DateTimeParseException;

@Log4j2
@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class EventExceptionHandler {

    @ExceptionHandler(value = DateTimeParseException.class)
    public ResponseEntity<ErrorMessageResponse> handleDateTimeParseException(
            DateTimeParseException e
    ) {
        log.error("Got date time parse exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.DATE_TIME_PARSE_EXCEPTION.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(value = DateEventInPastException.class)
    public ResponseEntity<ErrorMessageResponse> handleDateTimeParseException(
            DateEventInPastException e
    ) {
        log.error("Got date event in past exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.DATE_EVENT_IN_PAST.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(value = InsufficientSeatsException.class)
    public ResponseEntity<ErrorMessageResponse> handleInsufficientSeatsException(
            InsufficientSeatsException e
    ) {
        log.error("Got insufficient seats exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.INSUFFICIENT_SEATS.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(value = EventNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleEventNotFoundException(
            EventNotFoundException e
    ) {
        log.error("Got event not found exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.ENTITY_NOT_FOUND.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler(value = UserNotEventCreatorException.class)
    public ResponseEntity<ErrorMessageResponse> handleUserNotEventCreatorException(
            UserNotEventCreatorException e
    ) {
        log.error("Got user not event creator exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.USER_NOT_EVENT_CREATOR.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorDto);
    }

    @ExceptionHandler(value = CannotDeleteStartedEventException.class)
    public ResponseEntity<ErrorMessageResponse> handleCannotDeleteStartedEventException(
            CannotDeleteStartedEventException e
    ) {
        log.error("Got cannot delete started event exception", e);
        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.CANNOT_DELETE_STARTED_EVENT.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}
