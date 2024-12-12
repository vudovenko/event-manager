package dev.vudovenko.eventmanagement.eventRegistrations.exceptions.exceptionHandlers;


import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.AlreadyRegisteredForEventException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventRegistrationNotFoundException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventStatusNotAllowedForRegistrationCancellationException;
import dev.vudovenko.eventmanagement.eventRegistrations.exceptions.EventStatusNotAllowedForRegistrationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class EventRegistrationsExceptionHandler {

    @ExceptionHandler(value = EventRegistrationNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleEventRegistrationNotFoundException(
            EventRegistrationNotFoundException e
    ) {
        log.error("Got event registration not found exception", e);

        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.EVENT_REGISTRATION_NOT_FOUND.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(value = AlreadyRegisteredForEventException.class)
    public ResponseEntity<ErrorMessageResponse> handleAlreadyRegisteredForEventException(
            AlreadyRegisteredForEventException e
    ) {
        log.error("Got already registered for event exception", e);

        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.EVENT_REGISTRATION_ALREADY_EXISTS.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(value = EventStatusNotAllowedForRegistrationException.class)
    public ResponseEntity<ErrorMessageResponse> handleEventStatusNotAllowedForRegistrationException(
            EventStatusNotAllowedForRegistrationException e
    ) {
        log.error("Got event status not allowed for registration exception", e);

        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.EVENT_STATUS_NOT_ALLOWED_FOR_REGISTRATION.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(value = EventStatusNotAllowedForRegistrationCancellationException.class)
    public ResponseEntity<ErrorMessageResponse> handleEventStatusNotAllowedForRegistrationCancellationException(
            EventStatusNotAllowedForRegistrationCancellationException e
    ) {
        log.error("Got event status not allowed for registration cancellation exception", e);

        ErrorMessageResponse errorDto = ErrorMessageResponse.of(
                ExceptionHandlerMessages.EVENT_STATUS_NOT_ALLOWED_FOR_CANCELLATION.getMessage(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}
